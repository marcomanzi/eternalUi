package com.octopus.eternalUi.vaadinBridge

import com.octopus.eternalUi.domain.*
import com.octopus.eternalUi.domain.db.AbstractDataProvider
import com.octopus.eternalUi.domain.db.AbstractDomainAwareDataProvider
import com.octopus.eternalUi.domain.db.Identifiable
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import java.io.InputStream
import java.lang.reflect.Method
import java.util.*
import kotlin.NoSuchElementException

interface VaadinElementsHandler {
    fun cleanView(component: Component)
    fun createFor(uiComponent: UIComponent): Component
    fun addToParent(parent: Component, children: List<Component>): Component
    fun addToParent(parent: Component, child: Component): Component = addToParent(parent, listOf(child))
    fun removeFromContainer(container: HasComponents)

    fun debugButton(toDebugStringSupplier: () -> String): Component
    fun setValue(fieldValue: Any?, componentById: Component)
    fun getValue(componentById: Component): Any?
    fun addValueChangeListener(component: Component, listener: (Any) -> Unit)
    fun addOnChangeAction(component: Component, listener: (Any) -> Unit)

    fun enable(component: Component, condition: Boolean)
    fun addClickAction(component: Component, action: () -> Unit)
    fun addDataProviderTo(uiComponent: UIComponent, component: Component, dataProvider: com.octopus.eternalUi.domain.db.DataProvider<out Identifiable>)
    fun refresh(component: Component)
    fun refresh(component: Component, identifiable: Identifiable)
    fun addCssClass(component: Component, uiComponent: UIComponent)
    fun addCssClass(component: Component, cssClassName: String)
    fun <T: Any> showModalWindow(modalWindow: ModalWindow<T>)
    fun showUserMessage(userMessage: UserMessage)
    fun closeTopModalWindow()
    fun navigateTo(uiComponent: Class<out Component>)
    fun setInSession(key: String, value: Any)
    fun getFromSession(key: String): Any?
    fun removeFromSession(domainSessionKey: String): Any
    fun showConfirmDialog(confirmDialog: ConfirmDialog)
    fun closeConfirmDialog()
    fun <T: Any> addDownloadInputStream(action: DownloadAction<T>, domain: T, componentById: Component)
    fun getMainComponentFor(componentById: Component): Component
    fun switchCaptionTo(component: Component, newCaption: String)
    fun showFileInDifferentTab(file: File)
    fun reloadPage()
    fun setCaption(componentById: Component, caption: String)
    fun removeComponent(componentById: Component)
    fun setReadOnly(componentById: Component, readOnly: Boolean): Any
}

val elementsHandler = Vaadin15UiElementsHandler()
const val domain_session_key = "domain_session_key"
@Suppress("UNCHECKED_CAST")
open class EternalUI<T: Any>(var page: Page<T>): Div(), BeforeEnterObserver {

    private lateinit var uiComponentToVaadinComponent: Map<UIComponent, Component>

    fun prepareUI(): EternalUI<T> = apply {
        beforeEnter(null)
    }

    open fun customBehaviourOnEnter(eternalUI: EternalUI<T>) {
        page.beforeEnter?.let { beforeEnter ->
            page.pageDomain = PageDomain(beforeEnter(eternalUI))
        }
    }

    override fun beforeEnter(be: BeforeEnterEvent?) {
        setActionsAndDataProvidersToApply()
        elementsHandler.getFromSession(domain_session_key)?.let {
            page.pageDomain = PageDomain(it) as PageDomain<T>
            elementsHandler.removeFromSession(domain_session_key)
        }
        elementsHandler.cleanView(this)
        uiComponentToVaadinComponent = createMapUIComponentToVaadinComponent(page.uiView)
        setInContainerChildrenComponentForAllComponents()
        addMainUIToView()
        addActionsDataProvidersFromMethodsAndNamingConvention(page)
        setupConnectionsBetweenBackendAndUI()

        addDebugButton()
        customBehaviourOnEnter(this)
    }

    private fun setActionsAndDataProvidersToApply() {
        page.pageController.uiDataProviders.forEach { it.toApply = true }
        page.pageController.actions.forEach { it.toApply = true }
    }

    private fun setActionsAndDataProvidersToApply(uiComponent: UIComponent) {
        val ids = uiComponent.getUIComponentsIds()
        page.pageController.uiDataProviders.filter { ids.contains(it.forComponentId) }.forEach { it.toApply = true }
        page.pageController.actions.filter { ids.contains(it.onComponentId) }.forEach { it.toApply = true }
    }

    fun addComponent(nearComponentId: String, uiComponent: UIComponent) {
        setActionsAndDataProvidersToApply(uiComponent)
        val componentToAdd = if (uiComponent is TabsContainer) {
            VerticalContainer(uiComponent)
        } else { uiComponent }
        val toAddComponent = createMapUIComponentToVaadinComponent(componentToAdd)
        uiComponentToVaadinComponent = uiComponentToVaadinComponent + toAddComponent
        toAddComponent.keys.forEach { setInContainerChildrenComponent(it) }
        elementsHandler.addToParent(vaadinComponentForUi(getUIComponentById(nearComponentId)).parent.get(),
                vaadinComponentForUi(componentToAdd))
        toAddComponent.keys.forEach { addActionsDataProvidersFromMethodsAndNamingConvention(page, it) }
        setupConnectionsBetweenBackendAndUI()
    }

    fun removeByComponentId(componentId: String) {
        if (isThereComponentById(componentId)) {
            val uiComponentById = getUIComponentById(componentId)

            if (uiComponentById is TabsContainer) {
                getComponentById(componentId).parent.ifPresent { p ->
                    elementsHandler.removeComponent(p)
                }
            }
            uiComponentById.containedUIComponents.forEach {
                elementsHandler.removeComponent(getComponentById(it.id))
            }
            elementsHandler.removeComponent(getComponentById(componentId))
        }
    }

    private fun setupConnectionsBetweenBackendAndUI() {
        removeActionsDataProvidersWithoutUIComponent()
        activateDataProvidersOnComponents()
        linkDataProviderFiltersToDomain()
        linkDomainToComponents()
        activateRestrictionsOnComponents()
        activateActionsOnComponents()
        linkDataProviderFiltersToComponents()
        applyStileApplierFunction()
    }

    private fun removeActionsDataProvidersWithoutUIComponent() {
        page.pageController.uiDataProviders.removeIf { isThereComponentById(it.forComponentId).not() }
    }

    private fun applyStileApplierFunction() {
        uiComponentToVaadinComponent.keys.forEach {
            val component = elementsHandler.getMainComponentFor(getComponentById(it.id))
            elementsHandler.addCssClass(component, it)
            elementsHandler.addCssClass(component, it.cssClassName)
            it.styleApplyer?.invoke(this)
        }
    }

    private fun addDebugButton() {
        if (debugModeActive && page.pageDomain.dataClass !is EmptyDomain) elementsHandler.addToParent(vaadinComponentForUi(page.uiView), elementsHandler.debugButton { page.toDebugString() })
    }

    private fun createMapUIComponentToVaadinComponent(uiComponent: UIComponent): Map<UIComponent, Component> {
        val map = mapOf(Pair(uiComponent, elementsHandler.createFor(uiComponent)))
        return if (uiComponent.containedUIComponents.isNotEmpty())
            map + uiComponent.containedUIComponents.map { createMapUIComponentToVaadinComponent(it) }
                    .reduce { m1, m2 -> m1 + m2}
        else map
    }

    private fun setInContainerChildrenComponentForAllComponents() {
        uiComponentToVaadinComponent.keys.forEach {
            setInContainerChildrenComponent(it)
        }
    }

    private fun setInContainerChildrenComponent(uiComponent: UIComponent) {
        fun addVaadinComponentsToContainer(uiComponent: UIComponent) {
            elementsHandler.addToParent(vaadinComponentForUi(uiComponent), uiComponent.containedUIComponents.map { vaadinComponentForUi(it) })
        }
        if (uiComponent.containedUIComponents.isNotEmpty()) {
            addVaadinComponentsToContainer(uiComponent)
            if (uiComponent is TabsContainer) {
                elementsHandler.addToParent(vaadinComponentForUi(uiComponent).parent.get(), EternalUI((uiComponent.containedUIComponents.first() as Tab<*>).page).prepareUI().mainPageComponentForUI())
            }
        }
    }

    private fun addMainUIToView() {
        elementsHandler.addToParent(this, vaadinComponentForUi(page.uiView))
    }

    fun mainPageComponentForUI(): Component = vaadinComponentForUi(page.uiView)

    private fun vaadinComponentForUi(it: UIComponent): Component = uiComponentToVaadinComponent[it] ?: error("No Vaadin component found for uiComponent defined")

    private fun uiComponentForVaadin(it: Component): UIComponent = uiComponentToVaadinComponent.entries.firstOrNull { e -> e.value == it  }?.key ?: error("No Vaadin component found for uiComponent defined")

    private fun linkDomainToComponents() {
        page.fields().forEach { linkFieldToComponent(it) }
        linkFieldsToSession()

    }

    private fun linkFieldsToSession() {
        keysInSession.forEach { fieldName ->
            if (getFromSession(fieldName) != null && isThereComponentById(fieldName)) {
                val componentForField = getComponentById(fieldName)
                elementsHandler.setValue(getFromSession(fieldName), componentForField)
            }
        }
    }

    private fun linkFieldToComponent(fieldName: String) {
        fun valueShouldBeSetOnComponent(component: Component) = page.getFieldValue(fieldName) != null && page.getFieldValue(fieldName) != elementsHandler.getValue(component)

        if (isThereComponentById(fieldName)) {
            val componentForField = getComponentById(fieldName)
            page.addFieldChangeObserver(fieldName) { fieldValue -> elementsHandler.setValue(fieldValue, componentForField) }
            elementsHandler.addValueChangeListener(componentForField) { value ->
                page.setFieldValue(fieldName, value)
                activateRestrictionsOnComponents()
            }
            if (valueShouldBeSetOnComponent(componentForField)) {
                elementsHandler.setValue(page.getFieldValue(fieldName), componentForField)
            }
        }
    }

    private fun linkDataProviderFiltersToComponents() = page.pageController.uiDataProviders.forEach { it.filterIds.forEach { filterId -> linkFieldToFilters(filterId) } }

    private fun linkFieldToFilters(fieldName: String) {
        try {
            if (isThereComponentById(fieldName)) {
                val componentById = getComponentById(fieldName)
                elementsHandler.addValueChangeListener(componentById) { value ->
                    page.pageController.uiDataProviders.forEach {
                        it.applyFilterValueToDataProvider(fieldName, value)
                        elementsHandler.refresh(getComponentById(it.forComponentId))
                    }
                }
            }
        } catch (e: NoSuchElementException) {
            throw RuntimeException("No Element found for $fieldName", e)
        }
    }

    private fun linkDataProviderFiltersToDomain() {
        page.pageController.uiDataProviders.forEach { it.filterIds.forEach { filterId ->
            page.getFieldValue(filterId)?.let { value -> it.applyFilterValueToDataProvider(filterId, value) }
        }}
    }

    fun isThereComponentById(fieldName: String): Boolean = uiComponentToVaadinComponent.keys.any { it.id == fieldName}

    fun getComponentById(fieldName: String): Component = vaadinComponentForUi(uiComponentToVaadinComponent.keys.first { it.id == fieldName })

    fun getUIComponentById(fieldName: String): UIComponent = uiComponentToVaadinComponent.keys.first { it.id == fieldName }

    fun getContainerUIComponentByChildId(fieldName: String): Optional<UIComponent> = getComponentById(fieldName).parent.map { parent ->
        uiComponentForVaadin(parent)
    }

    private fun activateRestrictionsOnComponents() {
        page.pageController.enabledRules.forEach { activateRuleOnPage(it) }
    }

    private fun activateRuleOnPage(rule: Rule<T>) {
        when(rule) {
            is EnabledRule -> elementsHandler.enable(elementsHandler.getMainComponentFor(getComponentById(rule.onComponentId)), rule.condition(page))
        }
    }

    private fun activateActionsOnComponents() {
        page.pageController.actions.forEach { activateActionOnPage(it) }
    }

    private fun addActionsDataProvidersFromMethodsAndNamingConvention(page: Page<T>) =
            uiComponentToVaadinComponent.keys.forEach { addActionsDataProvidersFromMethodsAndNamingConvention(page, it) }

    private fun addActionsDataProvidersFromMethodsAndNamingConvention(page: Page<T>, uiComponent: UIComponent) {
        val controller = page.pageController
        val domain = page.pageDomain.dataClass.javaClass
        createActionsDataProviderFromMethods(controller, uiComponent, page, domain)
        createActionsDataProviderFromMethods(page, uiComponent, page, domain)
    }

    private fun createActionsDataProviderFromMethods(controller: Any, it: UIComponent, page: Page<T>, domain: Class<T>) {
        fun MutableList<UiDataProvider<out Identifiable>>.addWithCheck(toAdd: UiDataProvider<out Identifiable>) { if (this.none { it.id == toAdd.id }) this.add(toAdd) }
        fun MutableList<Action<T>>.addWithCheck(toAdd: Action<T>) { if (this.none { it.id == toAdd.id }) this.add(toAdd) }
        fun methodActionDataProviderId(m: Method, componentId: String) = "${m.name}-$componentId"
        controller.javaClass.methods.firstOrNull { m -> m.name == it.id + "DataProvider" }?.let { m ->
            page.pageController.uiDataProviders.addWithCheck(when {
                m.returnType.name == UiDataProvider::class.java.name -> (m.invoke(controller) as UiDataProvider<out Identifiable>).let { dp ->
                    UiDataProvider(it.id, dp.dataProvider, dp.refreshRule, *dp.filterIds, methodActionDataProviderId(m, it.id))
                }
                else -> UiDataProvider(it.id, m.invoke(controller) as AbstractDataProvider<out Identifiable>, id = methodActionDataProviderId(m, it.id))
            })
        }
        controller.javaClass.methods.firstOrNull { m -> m.name == it.id + "Clicked" }?.let { m ->
            page.pageController.actions.addWithCheck(when {
                it is DownloadButton -> (m.invoke(controller, page.pageDomain.dataClass) as Pair<(T) -> String, (T) -> InputStream>).let { pair ->
                    DownloadAction(it.id, pair.first, pair.second, methodActionDataProviderId(m, it.id))
                }
                m.parameterTypes[0].name.endsWith("EternalUI") -> OnClickUIAction(it.id, methodActionDataProviderId(m, it.id)) { ui -> m.invoke(controller, ui) as EternalUI<T> }
                m.returnType.name == domain.name -> OnClickAction(it.id, methodActionDataProviderId(m, it.id)) { ui -> m.invoke(controller, ui) as T }
                else -> OnClickReader(it.id, methodActionDataProviderId(m, it.id)) { ui -> m.invoke(controller, ui) }
            })
        }
        controller.javaClass.methods.firstOrNull { m -> m.name == it.id + "Changed" }?.let { m ->
            page.pageController.actions.addWithCheck(when {
                m.parameterTypes[0].name.endsWith("EternalUI") -> OnChangeUIAction(it.id, methodActionDataProviderId(m, it.id)) { ui -> m.invoke(controller, ui) as EternalUI<T> }
                m.returnType.name == domain.name -> OnChangeAction(it.id, methodActionDataProviderId(m, it.id)) { ui -> m.invoke(controller, ui) as T }
                else -> OnChangeReader(it.id, methodActionDataProviderId(m, it.id)) { ui -> m.invoke(controller, ui) as T }
            })
        }
    }

    private fun activateActionOnPage(action: Action<T>) {
        if (action.toApply) {
            val refresher = refresher(action, page)
            when(action) {
                is OnClickUIAction -> elementsHandler.addClickAction(getComponentById(action.onComponentId))
                { refresher { applyNewDomainOnPage(action.onUIFunction(this).page.pageDomain.dataClass) } }
                is OnClickAction -> elementsHandler.addClickAction(getComponentById(action.onComponentId))
                { refresher { applyNewDomainOnPage(action.onDataDomainClassFunction(page.pageDomain.dataClass)) } }
                is OnClickReader -> elementsHandler.addClickAction(getComponentById(action.onComponentId))
                { refresher { action.onDataDomainClassReader(page.pageDomain.dataClass) }}
                is OnChangeUIAction -> elementsHandler.addOnChangeAction(getComponentById(action.onComponentId))
                { refresher { applyNewDomainOnPage(action.onUIFunction(this).page.pageDomain.dataClass) } }
                is OnChangeAction -> elementsHandler.addOnChangeAction(getComponentById(action.onComponentId))
                { refresher { applyNewDomainOnPage(action.onDataDomainClassFunction(page.pageDomain.dataClass)) }}
                is OnChangeReader-> elementsHandler.addOnChangeAction(getComponentById(action.onComponentId))
                { refresher { action.onDataDomainClassReader(page.pageDomain.dataClass) }}
                is DownloadAction-> elementsHandler.addDownloadInputStream(action, page.pageDomain.dataClass, getComponentById(action.onComponentId))
            }
            action.toApply = false
        }
    }

    private fun refresher(action: Action<T>, page: Page<T>): (() -> Unit) ->  Unit {

        fun <T: Identifiable> ruleIsTrue(rule: Rule<T>): Boolean {
            return when (rule) {
                is AndRule -> rule.rules.all { ruleIsTrue(it) }
                is OrRule -> rule.rules.any { ruleIsTrue(it) }
                is WasInteractedWith -> rule.interactedComponentId == action.onComponentId
                else -> false
            }
        }

        fun <T: Identifiable> refreshComponentsForDataProvider(it: UiDataProvider<T>) {
            if (ruleIsTrue(it.refreshRule)) elementsHandler.refresh(getComponentById(it.forComponentId))
        }

        fun refreshAfter(f: () -> Unit) {
            f.invoke()
            page.pageController.uiDataProviders.forEach { refreshComponentsForDataProvider(it) }
        }
        return { refreshAfter(it) }
    }

    fun refresh(componentId: String) {
        elementsHandler.refresh(getComponentById(componentId))
    }

    fun refresh(componentId: String, identifiable: Identifiable?) {
        if (identifiable != null)
            elementsHandler.refresh(getComponentById(componentId), identifiable)
        else
            elementsHandler.refresh(getComponentById(componentId))
    }

    fun refreshItemsAfterAction(vararg componentIdsToRefresh: String, function: (EternalUI<T>) -> Unit): EternalUI<T> = this.apply {
        function(this)
        componentIdsToRefresh.forEach { refresh(it) }
    }

    fun applyNewDomainOnPage(dataClass: Any) {
        dataClass.javaClass.declaredFields.forEach { declaredField ->
            declaredField.isAccessible = true
            val newValue = declaredField.get(dataClass)
            if (newValue != page.getFieldValue(declaredField.name)) {
                page.setFieldValue(declaredField.name, newValue)
            }
        }
    }

    private fun activateDataProvidersOnComponents() {
        page.pageController.uiDataProviders.filter { it.toApply }.forEach { dataProvider ->
            if (dataProvider.dataProvider is AbstractDomainAwareDataProvider<*>) dataProvider.dataProvider.domain = page.pageDomain.dataClass
            elementsHandler.addDataProviderTo(getUIComponentById(dataProvider.forComponentId), getComponentById(dataProvider.forComponentId), dataProvider.dataProvider)
            dataProvider.toApply = false
        }
    }

    fun switchCaptionTo(componentId: String, newCaption: String) {
        elementsHandler.switchCaptionTo(getComponentById(componentId), newCaption)
    }

    fun setCaptionTo(componentId: String, caption: String) {
        elementsHandler.setCaption(getComponentById(componentId), caption)
    }

    fun setReadOnly(componentId: String) = elementsHandler.setReadOnly(getComponentById(componentId), true)

    fun setNotReadOnly(componentId: String) = elementsHandler.setReadOnly(getComponentById(componentId), true)

    companion object {
        val keysInSession = mutableListOf<String>()

        fun showInUI(uiComponent: UIComponent) {
            when (uiComponent) {
                is ModalWindow<*> -> elementsHandler.showModalWindow(uiComponent)
                is UserMessage -> elementsHandler.showUserMessage(uiComponent)
                is ConfirmDialog -> elementsHandler.showConfirmDialog(uiComponent)
                is File -> elementsHandler.showFileInDifferentTab(uiComponent)
            }
        }

        fun navigateTo(uiComponent: Class<out Component>, domain: Any? = null) {
            domain?.let { elementsHandler.setInSession(domain_session_key, it) }
            elementsHandler.navigateTo(uiComponent)
        }

        fun closeTopModalWindow() {
            elementsHandler.closeTopModalWindow()
        }

        fun closeConfirmDialog() {
            elementsHandler.closeConfirmDialog()
        }

        fun setInSession(key: String, value: Any) {
            elementsHandler.setInSession(key, value)
        }

        fun removeFromSession(key: String) {
            elementsHandler.removeFromSession(key)
        }

        fun getFromSession(key: String) = elementsHandler.getFromSession(key)

        fun reloadPage() {
            elementsHandler.reloadPage()
        }
    }

}
