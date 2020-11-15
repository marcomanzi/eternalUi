package com.octopus.eternalUi.vaadinBridge

import com.octopus.eternalUi.domain.*
import com.octopus.eternalUi.domain.db.AbstractDataProvider
import com.octopus.eternalUi.domain.db.AbstractDomainAwareDataProvider
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import java.io.InputStream
import java.lang.reflect.Method
import java.util.*

interface VaadinElementsHandler {
    fun cleanView(component: Component)
    fun createFor(uiComponent: UIComponent, pageDomain: Any?): Component
    fun addToParent(parent: Component, children: List<Component>): Component
    fun addToParent(parent: Component, child: Component): Component = addToParent(parent, listOf(child))
    fun removeFromContainer(container: HasComponents)

    fun debugButton(toDebugStringSupplier: () -> String): Component
    fun setValue(fieldValue: Any?, componentById: Component)
    fun getValue(componentById: Component): Any?
    fun addValueChangeListener(component: Component, listener: (Any?) -> Unit)
    fun addOnChangeAction(component: Component, listener: (Any) -> Unit)

    fun enable(component: Component, condition: Boolean)
    fun addClickAction(component: Component, action: () -> Unit)
    fun addDataProviderTo(uiComponent: UIComponent, component: Component, dataProvider: com.octopus.eternalUi.domain.db.DataProvider)
    fun refresh(component: Component)
    fun refresh(component: Component, identifiable: Any)
    fun addCssClass(component: Component, uiComponent: UIComponent)
    fun addCssClass(component: Component, cssClassName: String)
    fun showModalWindow(modalWindow: ModalWindow)
    fun showUserMessage(userMessage: UserMessage)
    fun closeTopModalWindow()
    fun navigateTo(uiComponent: Class<out Component>)
    fun setInSession(key: String, value: Any)
    fun getFromSession(key: String): Any?
    fun removeFromSession(domainSessionKey: String): Any
    fun showConfirmDialog(confirmDialog: ConfirmDialog)
    fun closeConfirmDialog()
    fun addDownloadInputStream(action: DownloadAction, domain: Any, componentById: Component)
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
open class EternalUI(var page: Page): Div(), BeforeEnterObserver {

    private lateinit var uiComponentToVaadinComponent: Map<UIComponent, Component>

    fun prepareUI(): EternalUI = apply {
        beforeEnter(null)
    }

    open fun customBehaviourOnEnter(eternalUI: EternalUI) {
        page.beforeEnter?.let { beforeEnter ->
            page.pageDomain = beforeEnter(eternalUI.page)
        }
    }

    override fun beforeEnter(be: BeforeEnterEvent?) {
        customBehaviourOnEnter(this)
        setActionsAndDataProvidersToApply()
        elementsHandler.getFromSession(domain_session_key)?.let {
            page.pageDomain = it
            elementsHandler.removeFromSession(domain_session_key)
        }
        elementsHandler.cleanView(this)
        uiComponentToVaadinComponent = createMapUIComponentToVaadinComponent(page.uiView, page.pageDomain)
        setInContainerChildrenComponentForAllComponents()
        addMainUIToView()
        addActionsDataProvidersFromMethodsAndNamingConvention(page)
        setupConnectionsBetweenBackendAndUI()

        addDebugButton()
    }

    private fun setActionsAndDataProvidersToApply() {
        page.controller.uiDataProviders.forEach { it.toApply = true }
        page.controller.actions.forEach { it.toApply = true }
    }

    private fun setActionsAndDataProvidersToApply(uiComponent: UIComponent) {
        val ids = uiComponent.getUIComponentsIds()
        page.controller.uiDataProviders.filter { ids.contains(it.forComponentId) }.forEach { it.toApply = true }
        page.controller.actions.filter { ids.contains(it.onComponentId) }.forEach { it.toApply = true }
    }

    fun addComponent(nearComponentId: String, uiComponent: UIComponent) {
        setActionsAndDataProvidersToApply(uiComponent)
        val componentToAdd = if (uiComponent is TabsContainer) {
            VerticalContainer(uiComponent)
        } else { uiComponent }
        val toAddComponent = createMapUIComponentToVaadinComponent(componentToAdd, page.pageDomain)
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
        page.controller.uiDataProviders.removeIf { isThereComponentById(it.forComponentId).not() }
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
        if (debugModeActive && page.pageDomain !is EmptyDomain) elementsHandler.addToParent(vaadinComponentForUi(page.uiView), elementsHandler.debugButton { page.toDebugString() })
    }

    private fun createMapUIComponentToVaadinComponent(uiComponent: UIComponent, pageDomain: Any): Map<UIComponent, Component> {
        val map = mapOf(Pair(uiComponent, elementsHandler.createFor(uiComponent, pageDomain)))
        return if (uiComponent.containedUIComponents.isNotEmpty())
            map + uiComponent.containedUIComponents.map { createMapUIComponentToVaadinComponent(it, page.pageDomain) }
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
                elementsHandler.addToParent(vaadinComponentForUi(uiComponent).parent.get(), EternalUI((uiComponent.containedUIComponents.first() as Tab).page).prepareUI().mainPageComponentForUI())
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

    private fun linkDataProviderFiltersToComponents() = page.controller.uiDataProviders.forEach { it.filterIds.forEach { filterId -> linkFieldToFilters(filterId) } }

    private fun linkFieldToFilters(fieldName: String) {
        try {
            if (isThereComponentById(fieldName)) {
                val componentById = getComponentById(fieldName)
                elementsHandler.addValueChangeListener(componentById) { value ->
                    page.controller.uiDataProviders.forEach {
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
        page.controller.uiDataProviders.forEach { it.filterIds.forEach { filterId ->
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
        page.controller.enabledRules.forEach { activateRuleOnPage(it) }
    }

    private fun activateRuleOnPage(rule: Rule) {
        when(rule) {
            is EnabledRule<*> -> elementsHandler.enable(elementsHandler.getMainComponentFor(getComponentById(rule.onComponentId)), rule.condition(page))
        }
    }

    private fun activateActionsOnComponents() {
        page.controller.actions.forEach { activateActionOnPage(it) }
    }

    private fun addActionsDataProvidersFromMethodsAndNamingConvention(page: Page) =
            uiComponentToVaadinComponent.keys.forEach { addActionsDataProvidersFromMethodsAndNamingConvention(page, it) }

    private fun addActionsDataProvidersFromMethodsAndNamingConvention(page: Page, uiComponent: UIComponent) {
        val controller = page.controller
        val domain = page.pageDomain.javaClass
        createActionsDataProviderFromMethods(controller, uiComponent, page, domain)
        createActionsDataProviderFromMethods(page, uiComponent, page, domain)
    }

    private fun createActionsDataProviderFromMethods(controller: Any, it: UIComponent, page: Page, domain: Class<*>) {
        fun MutableList<UiDataProvider>.addWithCheck(toAdd: UiDataProvider) { if (this.none { it.id == toAdd.id }) this.add(toAdd) }
        fun MutableList<Action>.addWithCheck(toAdd: Action) { if (this.none { it.id == toAdd.id }) this.add(toAdd) }
        fun methodActionDataProviderId(m: Method, componentId: String) = "${m.name}-$componentId"
        controller.javaClass.methods.firstOrNull { m -> m.name == it.id + "DataProvider" }?.let { m ->
            page.controller.uiDataProviders.addWithCheck(when (m.returnType.name) {
                UiDataProvider::class.java.name -> (m.invoke(controller) as UiDataProvider).let { dp ->
                    UiDataProvider(it.id, dp.dataProvider, dp.refreshRule, *dp.filterIds, methodActionDataProviderId(m, it.id))
                }
                else -> UiDataProvider(it.id, m.invoke(controller) as AbstractDataProvider, id = methodActionDataProviderId(m, it.id))
            })
        }
        controller.javaClass.methods.firstOrNull { m -> m.name == it.id + "Clicked" }?.let { m ->
            page.controller.actions.addWithCheck(when {
                it is DownloadButton -> (m.invoke(controller, page.pageDomain) as Pair<(Any) -> String, (Any) -> InputStream>).let { pair ->
                    DownloadAction(it.id, pair.first, pair.second, methodActionDataProviderId(m, it.id))
                }
                (m.parameterTypes.isNotEmpty() && (m.parameterTypes[0]?.name?:"").endsWith("EternalUI")) -> OnClickUIAction(it.id, methodActionDataProviderId(m, it.id)) { ui -> m.invoke(controller, ui) as EternalUI }
                m.returnType.name == domain.name -> OnClickAction(it.id, methodActionDataProviderId(m, it.id)) { ui -> m.invoke(controller, ui) as Any }
                else -> OnClickReader(it.id, methodActionDataProviderId(m, it.id)) { ui -> m.invoke(controller, ui) }
            })
        }
        controller.javaClass.methods.firstOrNull { m -> m.name == it.id + "Changed" }?.let { m ->
            page.controller.actions.addWithCheck(when {
                (m.parameterTypes.isNotEmpty() && (m.parameterTypes[0]?.name?:"").endsWith("EternalUI")) -> OnChangeUIAction(it.id, methodActionDataProviderId(m, it.id)) { ui -> m.invoke(controller, ui) as EternalUI }
                m.returnType.name == domain.name -> OnChangeAction(it.id, methodActionDataProviderId(m, it.id)) { ui -> m.invoke(controller, ui) }
                else -> OnChangeReader(it.id, methodActionDataProviderId(m, it.id)) { ui -> m.invoke(controller, ui) }
            })
        }
    }

    private fun activateActionOnPage(action: Action) {
        if (action.toApply) {
            val refresher = refresher(action, page)
            when(action) {
                is OnClickUIAction -> elementsHandler.addClickAction(getComponentById(action.onComponentId))
                { refresher { applyNewDomainOnPage(action.onUIFunction(this).page.pageDomain) } }
                is OnClickAction -> elementsHandler.addClickAction(getComponentById(action.onComponentId))
                { refresher { applyNewDomainOnPage(action.onDataDomainClassFunction(page.pageDomain)) } }
                is OnClickReader -> elementsHandler.addClickAction(getComponentById(action.onComponentId))
                { refresher { action.onDataDomainClassReader(page.pageDomain) }}
                is OnChangeUIAction -> elementsHandler.addOnChangeAction(getComponentById(action.onComponentId))
                { refresher { applyNewDomainOnPage(action.onUIFunction(this).page.pageDomain) } }
                is OnChangeAction -> elementsHandler.addOnChangeAction(getComponentById(action.onComponentId))
                { refresher { applyNewDomainOnPage(action.onDataDomainClassFunction(page.pageDomain)) }}
                is OnChangeReader-> elementsHandler.addOnChangeAction(getComponentById(action.onComponentId))
                { refresher { action.onDataDomainClassReader(page.pageDomain) }}
                is DownloadAction-> elementsHandler.addDownloadInputStream(action, page.pageDomain, getComponentById(action.onComponentId))
            }
            action.toApply = false
        }
    }

    private fun refresher(action: Action, page: Page): (() -> Unit) ->  Unit {

        fun ruleIsTrue(rule: Rule): Boolean {
            return when (rule) {
                is AndRule -> rule.rules.all { ruleIsTrue(it) }
                is OrRule -> rule.rules.any { ruleIsTrue(it) }
                is WasInteractedWith -> rule.interactedComponentId == action.onComponentId
                else -> false
            }
        }

        fun  refreshComponentsForDataProvider(it: UiDataProvider) {
            if (ruleIsTrue(it.refreshRule)) elementsHandler.refresh(getComponentById(it.forComponentId))
        }

        fun refreshAfter(f: () -> Unit) {
            f.invoke()
            page.controller.uiDataProviders.forEach { refreshComponentsForDataProvider(it) }
        }
        return { refreshAfter(it) }
    }

    fun refresh(componentId: String) {
        elementsHandler.refresh(getComponentById(componentId))
    }

    fun refresh(componentId: String, identifiable: Any?) {
        if (identifiable != null)
            elementsHandler.refresh(getComponentById(componentId), identifiable)
        else
            elementsHandler.refresh(getComponentById(componentId))
    }

    fun refreshItemsAfterAction(vararg componentIdsToRefresh: String, function: (EternalUI) -> Unit): EternalUI = this.apply {
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
        page.controller.uiDataProviders.filter { it.toApply }.forEach { dataProvider ->
            if (dataProvider.dataProvider is AbstractDomainAwareDataProvider) dataProvider.dataProvider.domain = page.pageDomain
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

    fun setNotReadOnly(componentId: String) = elementsHandler.setReadOnly(getComponentById(componentId), false)

    companion object {
        val keysInSession = mutableListOf<String>()

        fun showInUI(uiComponent: UIComponent) {
            when (uiComponent) {
                is ModalWindow -> elementsHandler.showModalWindow(uiComponent)
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
