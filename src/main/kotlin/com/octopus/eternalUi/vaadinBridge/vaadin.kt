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
    fun setInSession(key: String, it: Any)
    fun getFromSession(key: String): Any?
    fun removeFromSession(domainSessionKey: String): Any
    fun showConfirmDialog(confirmDialog: ConfirmDialog)
    fun closeConfirmDialog()
    fun <T: Any> addDownloadInputStream(action: DownloadAction<T>, domain: T, componentById: Component)
    fun getMainComponentFor(componentById: Component): Component
}

val elementsHandler = Vaadin14UiElementsHandler()
const val domain_session_key = "domain_session_key"
@Suppress("UNCHECKED_CAST")
open class EternalUI<T: Any>(var page: Page<T>): Div(), BeforeEnterObserver {

    private lateinit var uiComponentToVaadinComponent: Map<UIComponent, Component>

    fun prepareUI(): EternalUI<T> = apply {
        beforeEnter(null)
    }

    open fun customBehaviourOnEnter(eternalUI: EternalUI<T>) {}

    override fun beforeEnter(be: BeforeEnterEvent?) {
        elementsHandler.getFromSession(domain_session_key)?.let {
            page.pageDomain = PageDomain(it) as PageDomain<T>
            elementsHandler.removeFromSession(domain_session_key)
        }
        elementsHandler.cleanView(this)
        uiComponentToVaadinComponent = createMapUIComponentToVaadinComponent(page.uiView)
        setInContainerComponentsChildren()
        addMainUIToView()
        addConnectionBetweenControllerAndDomainBasedOnConvention(page)
        activateDataProvidersOnComponents()
        linkDataProviderFiltersToDomain()
        linkDomainToComponents()
        activateRestrictionsOnComponents()
        activateActionsOnComponents()
        linkDataProviderFiltersToComponents()
        applyStileApplierFunction()

        addDebugButton()
        customBehaviourOnEnter(this)
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
        if (debugModeActive) elementsHandler.addToParent(vaadinComponentForUi(page.uiView), elementsHandler.debugButton { page.toDebugString() })
    }

    private fun createMapUIComponentToVaadinComponent(uiComponent: UIComponent): Map<UIComponent, Component> {
        val map = mapOf(Pair(uiComponent, elementsHandler.createFor(uiComponent)))
        return if (uiComponent.containedUIComponents.isNotEmpty())
            map + uiComponent.containedUIComponents.map { createMapUIComponentToVaadinComponent(it) }
                    .reduce { m1, m2 -> m1 + m2}
        else map
    }

    private fun setInContainerComponentsChildren() {
        fun addVaadinComponentsToContainer(uiComponent: UIComponent) {
            elementsHandler.addToParent(vaadinComponentForUi(uiComponent), uiComponent.containedUIComponents.map { vaadinComponentForUi(it) })
        }

        uiComponentToVaadinComponent.keys.filter { it.containedUIComponents.isNotEmpty() }.forEach {
            addVaadinComponentsToContainer(it)
            if (it is TabsContainer) {
                elementsHandler.addToParent(vaadinComponentForUi(it).parent.get(), EternalUI((it.containedUIComponents.first() as Tab<*>).page).prepareUI().mainPageComponentForUI())
            }
        }
    }

    private fun addMainUIToView() {
        elementsHandler.addToParent(this, vaadinComponentForUi(page.uiView))
    }

    fun mainPageComponentForUI(): Component = vaadinComponentForUi(page.uiView)

    private fun vaadinComponentForUi(it: UIComponent): Component = uiComponentToVaadinComponent[it] ?: error("No Vaadin component found for uiComponent defined")

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

    private fun linkDataProviderFiltersToComponents() = page.pageController.dataProviders.forEach { it.filterIds.forEach { filterId -> linkFieldToFilters(filterId) } }

    private fun linkFieldToFilters(fieldName: String) {
        try {
            if (isThereComponentById(fieldName)) {
                val componentById = getComponentById(fieldName)
                elementsHandler.addValueChangeListener(componentById) { value ->
                    page.pageController.dataProviders.forEach {
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
        page.pageController.dataProviders.forEach { it.filterIds.forEach { filterId ->
            page.getFieldValue(filterId)?.let { value -> it.applyFilterValueToDataProvider(filterId, value) }
        }}
    }

    fun isThereComponentById(fieldName: String): Boolean = uiComponentToVaadinComponent.keys.any { it.id == fieldName}

    fun getComponentById(fieldName: String): Component = vaadinComponentForUi(uiComponentToVaadinComponent.keys.first { it.id == fieldName })

    fun getUIComponentById(fieldName: String): UIComponent = uiComponentToVaadinComponent.keys.first { it.id == fieldName }

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

    private fun addConnectionBetweenControllerAndDomainBasedOnConvention(page: Page<T>) {
        uiComponentToVaadinComponent.keys.forEach {
            val controller = page.pageController
            val domain = page.pageDomain.dataClass.javaClass
            controller.javaClass.methods.firstOrNull { m -> m.name == it.id + "DataProvider"}?.let { m ->
                page.pageController.dataProviders.add(DataProvider(it.id, m.invoke(controller) as AbstractDataProvider<out Identifiable>))
            }
            controller.javaClass.methods.firstOrNull { m -> m.name == it.id + "Clicked"}?.let { m ->
                page.pageController.actions.add(when {
                    m.parameterTypes[0].name.endsWith("EternalUI") -> OnClickUIAction(it.id) { ui -> m.invoke(controller, ui) as EternalUI<T> }
                    m.returnType.javaClass.name == domain.name -> OnClickAction(it.id) { ui -> m.invoke(controller, ui) as T }
                    else -> OnClickReader(it.id) { ui -> m.invoke(controller, ui) }
                })
            }
            controller.javaClass.methods.firstOrNull { m -> m.name == it.id + "Changed"}?.let { m ->
                page.pageController.actions.add(when {
                    m.returnType.javaClass.name == domain.name -> OnChangeAction(it.id) { ui -> m.invoke(controller, ui) as T }
                    else -> OnChangeReader(it.id) { ui -> m.invoke(controller, ui) as T }
                })
            }
//                class OnClickUIAction<T: Any>(_onComponentId: String, val onUIFunction: (EternalUI<T>) -> EternalUI<T>): Action<T>(_onComponentId)
//                class OnClickAction<T: Any>(_onComponentId: String, val onDataDomainClassFunction: (T) -> T): Action<T>(_onComponentId)
//                class OnClickReader<T: Any>(_onComponentId: String, val onDataDomainClassReader: (T) -> Unit): Action<T>(_onComponentId)
//                class OnChangeAction<T: Any>(_onComponentId: String, val onDataDomainClassFunction: (T) -> T): Action<T>(_onComponentId)
//                class OnChangeReader<T: Any>(_onComponentId: String, val onDataDomainClassReader: (T) -> Unit): Action<T>(_onComponentId)
//                class DownloadAction<T: Any>(_onComponentId: String, val fileName: String, val onDataDomainInputStream: (T) -> InputStream): Action<T>(_onComponentId)


        }

    }

    private fun activateActionOnPage(action: Action<T>) {
        val refresher = refresher(action, page)
        when(action) {
            is OnClickUIAction -> elementsHandler.addClickAction(getComponentById(action.onComponentId))
            { refresher { applyNewDomainOnPage(action.onUIFunction(this).page.pageDomain.dataClass) } }
            is OnClickAction -> elementsHandler.addClickAction(getComponentById(action.onComponentId))
            { refresher { applyNewDomainOnPage(action.onDataDomainClassFunction(page.pageDomain.dataClass)) } }
            is OnClickReader -> elementsHandler.addClickAction(getComponentById(action.onComponentId))
            { refresher { action.onDataDomainClassReader(page.pageDomain.dataClass) }}
            is OnChangeAction -> elementsHandler.addOnChangeAction(getComponentById(action.onComponentId))
            { refresher { applyNewDomainOnPage(action.onDataDomainClassFunction(page.pageDomain.dataClass)) }}
            is OnChangeReader-> elementsHandler.addOnChangeAction(getComponentById(action.onComponentId))
            { refresher { action.onDataDomainClassReader(page.pageDomain.dataClass) }}
            is DownloadAction-> elementsHandler.addDownloadInputStream(action, page.pageDomain.dataClass, getComponentById(action.onComponentId))
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

        fun <T: Identifiable> refreshComponentsForDataProvider(it: DataProvider<T>) {
            if (ruleIsTrue(it.refreshRule)) elementsHandler.refresh(getComponentById(it.forComponentId))
        }

        fun refreshAfter(f: () -> Unit) {
            f.invoke()
            page.pageController.dataProviders.forEach { refreshComponentsForDataProvider(it) }
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

    private fun applyNewDomainOnPage(dataClass: Any) {
        dataClass.javaClass.declaredFields.forEach { declaredField ->
            declaredField.isAccessible = true
            val newValue = declaredField.get(dataClass)
            if (newValue != page.getFieldValue(declaredField.name)) {
                page.setFieldValue(declaredField.name, newValue)
            }
        }
    }

    private fun activateDataProvidersOnComponents() {
        page.pageController.dataProviders.forEach { dataProvider ->
            if (dataProvider.dataProvider is AbstractDomainAwareDataProvider<*>) dataProvider.dataProvider.domain = page.pageDomain.dataClass
            elementsHandler.addDataProviderTo(getUIComponentById(dataProvider.forComponentId), getComponentById(dataProvider.forComponentId), dataProvider.dataProvider)
        }
    }

    companion object {
        val keysInSession = mutableListOf<String>()

        fun showInUI(uiComponent: UIComponent) {
            when (uiComponent) {
                is ModalWindow<*> -> elementsHandler.showModalWindow(uiComponent)
                is UserMessage -> elementsHandler.showUserMessage(uiComponent)
                is ConfirmDialog -> elementsHandler.showConfirmDialog(uiComponent)
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
    }

}
