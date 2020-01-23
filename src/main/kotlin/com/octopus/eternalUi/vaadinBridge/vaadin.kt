package com.octopus.eternalUi.vaadinBridge

import com.octopus.eternalUi.domain.*
import com.octopus.eternalUi.domain.db.Identifiable
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver

interface VaadinElementsHandler {
    fun cleanView(component: Component)
    fun createFor(uiComponent: UIComponent): Component
    fun addToParent(parent: Component, children: List<Component>): Component
    fun addToParent(parent: Component, child: Component): Component = addToParent(parent, listOf(child))

    fun debugButton(toDebugStringSupplier: () -> String): Component
    fun setValue(fieldValue: Any?, componentById: Component)
    fun getValue(componentById: Component): Any?
    fun addValueChangeListener(component: Component, listener: (Any) -> Unit)
    fun addOnChangeAction(component: Component, listener: (Any) -> Unit)

    fun enable(component: Component, condition: Boolean)
    fun addClickAction(component: Component, action: () -> Unit)
    fun addDataProviderTo(uiComponent: UIComponent, component: Component, dataProvider: com.octopus.eternalUi.domain.db.DataProvider<out Identifiable>)
    fun refresh(component: Component)
    fun addCssClass(component: Component, uiComponent: UIComponent)
    fun addCssClass(component: Component, cssClassName: String)
    fun <T: Any> showModalWindow(modalWindow: ModalWindow<T>)
    fun showUserMessage(userMessage: UserMessage)
    fun closeTopModalWindow()
    fun navigateTo(uiComponent: Class<out Component>)
    fun setInSession(key: String, it: Any)
    fun getFromSession(key: String): Any?
    fun removeFromSession(domainSessionKey: String): Any
}

val elementsHandler = Vaadin14UiElementsHandler()
val domain_session_key = "domain_session_key"

open class EternalUI<T: Any>(var page: Page<T>): Div(), BeforeEnterObserver {

    private lateinit var uiComponentToVaadinComponent: Map<UIComponent, Component>

    fun prepareUI(): EternalUI<T> = apply {
        beforeEnter(null)
    }

    override fun beforeEnter(be: BeforeEnterEvent?) {
        elementsHandler.getFromSession(domain_session_key)?.let {
            page.pageDomain = PageDomain(it) as PageDomain<T>
            elementsHandler.removeFromSession(domain_session_key)
        }
        elementsHandler.cleanView(this)
        uiComponentToVaadinComponent = createMapUIComponentToVaadinComponent(page.uiView)
        setInContainerComponentsChildren()
        addMainUIToView()
        linkDomainToComponents()
        activateRestrictionsOnComponents()
        activateActionsOnComponents()
        activateDataProvidersOnComponents()
        linkDataProviderFiltersToComponents()
        applyStileApplierFunction()

        addDebugButton()
    }

    private fun applyStileApplierFunction() {
        uiComponentToVaadinComponent.keys.forEach {
            elementsHandler.addCssClass(getComponentById(it.id), it)
            elementsHandler.addCssClass(getComponentById(it.id), it.cssClassName)
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
        }
    }

    private fun addMainUIToView() {
        elementsHandler.addToParent(this, vaadinComponentForUi(page.uiView))
    }

    fun mainPageComponentForUI(): Component = vaadinComponentForUi(page.uiView)

    private fun vaadinComponentForUi(it: UIComponent): Component = uiComponentToVaadinComponent[it] ?: error("No Vaadin component found for uiComponent defined")

    private fun linkDomainToComponents() = page.fields().forEach { linkFieldToComponent(it) }

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
            val componentById = getComponentById(fieldName)
            elementsHandler.addValueChangeListener(componentById) { value ->
                page.pageController.dataProviders.forEach {
                    it.applyFilterValueToDataProvider(fieldName, value)
                    elementsHandler.refresh(getComponentById(it.forComponentId))
                }
            }
        } catch (e: NoSuchElementException) {
            throw RuntimeException("No Element found for $fieldName", e)
        }
    }

    private fun isThereComponentById(fieldName: String): Boolean = uiComponentToVaadinComponent.keys.any { it.id == fieldName}

    fun getComponentById(fieldName: String): Component = vaadinComponentForUi(uiComponentToVaadinComponent.keys.first { it.id == fieldName })

    fun getUIComponentById(fieldName: String): UIComponent = uiComponentToVaadinComponent.keys.first { it.id == fieldName }

    private fun activateRestrictionsOnComponents() {
        page.pageController.enabledRules.forEach { activateRuleOnPage(it) }
    }

    private fun activateRuleOnPage(rule: Rule<T>) {
        when(rule) {
            is EnabledRule -> elementsHandler.enable(getComponentById(rule.onComponentId), rule.condition(page))
        }
    }

    private fun activateActionsOnComponents() {
        page.pageController.actions.forEach { activateActionOnPage(it) }
    }

    private fun activateActionOnPage(action: Action<T>) {
        val refresher = refresher(action, page)
        when(action) {
            is OnClickAction -> elementsHandler.addClickAction(getComponentById(action.onComponentId))
            { refresher { applyNewDomainOnPage(action.onDataDomainClassFunction(page.pageDomain.dataClass)) } }
            is OnClickReader -> elementsHandler.addClickAction(getComponentById(action.onComponentId))
            { refresher { action.onDataDomainClassReader(page.pageDomain.dataClass) }}
            is OnChangeAction -> elementsHandler.addOnChangeAction(getComponentById(action.onComponentId))
            { refresher { applyNewDomainOnPage(action.onDataDomainClassFunction(page.pageDomain.dataClass)) }}
            is OnChangeReader-> elementsHandler.addOnChangeAction(getComponentById(action.onComponentId))
            { refresher { action.onDataDomainClassReader(page.pageDomain.dataClass) }}
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
            elementsHandler.addDataProviderTo(getUIComponentById(dataProvider.forComponentId), getComponentById(dataProvider.forComponentId), dataProvider.dataProvider)
        }
    }

    companion object {
        fun showInUI(uiComponent: UIComponent) {
            when (uiComponent) {
                is ModalWindow<*> -> elementsHandler.showModalWindow(uiComponent)
                is UserMessage -> elementsHandler.showUserMessage(uiComponent)
            }
        }

        fun navigateTo(uiComponent: Class<out Component>, domain: Any? = null) {
            domain?.let { elementsHandler.setInSession(domain_session_key, it) }
            elementsHandler.navigateTo(uiComponent)
        }

        fun closeTopModalWindow() {
            elementsHandler.closeTopModalWindow()
        }
    }

}
