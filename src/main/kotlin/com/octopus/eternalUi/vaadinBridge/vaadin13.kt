package com.octopus.eternalUi.vaadinBridge

import com.octopus.eternalUi.domain.*
import com.octopus.eternalUi.domain.db.DataProviderWrapper
import com.octopus.eternalUi.domain.db.Identifiable
import com.vaadin.flow.component.*
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.PasswordField
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.value.ValueChangeMode
import com.vaadin.flow.router.RouterLink

class Vaadin13UiElementsHandler: VaadinElementsHandler {
    override fun debugButton(toDebugStringSupplier: () -> String): Component =
            com.vaadin.flow.component.button.Button("Debug").apply { addClickListener {
                Dialog(Label().apply { element.setProperty("innerHTML", toDebugStringSupplier()) }).apply { open()
                }}}

    override fun addToParent(parent: Component, children: List<Component>): Component =
            parent.apply { (this as HasComponents).add(*children.toTypedArray()) }

    override fun createFor(uiComponent: UIComponent): Component =
            when(uiComponent) {
                is VerticalContainer -> VerticalLayout().apply { isSpacing = false; isMargin = false; setSizeFull() }
                is HorizontalContainer -> HorizontalLayout().apply { isMargin = false; setSizeFull() }
                is com.octopus.eternalUi.domain.Label -> Label(uiComponent.caption)
                is Input -> createFor(uiComponent)
                is Button -> com.vaadin.flow.component.button.Button(uiComponent.caption)
                is Grid -> com.vaadin.flow.component.grid.Grid(uiComponent.elementType.java).apply { setupGrid(this, uiComponent.columns) }
                is InsideAppLink -> RouterLink(uiComponent.caption, uiComponent.uiViewClass)
                else -> Div()
            }

    override fun addCssClass(component: Component, uiComponent: UIComponent) {
        (component as HasStyle).addClassName("base.css.${uiComponent.javaClass.simpleName}")
    }

    private fun setupGrid(grid: com.vaadin.flow.component.grid.Grid<out Any>, columns: List<String>) {
        grid.setColumns(*columns.toTypedArray())
        grid.columns.forEach { it.isSortable = false }
    }

    private fun createFor(input: Input): Component =
            when(input.type) {
                InputType.Text -> TextField(UtilsUI.captionFromId(input.caption)).apply { valueChangeMode = ValueChangeMode.EAGER }
                InputType.Password -> PasswordField(UtilsUI.captionFromId(input.caption)).apply { valueChangeMode = ValueChangeMode.EAGER }
            }

    override fun cleanView(component: Component) {
        component.apply { (this as HasComponents).removeAll() }
    }

    override fun setValue(fieldValue: Any, componentById: Component) {
        when(componentById) {
            is TextField -> componentById.value = fieldValue.toString()
            is PasswordField -> componentById.value = fieldValue.toString()
        }
    }

    override fun addValueChangeListener(component: Component, listener: (String) -> Unit) {
        when(component) {
            is TextField -> component.addValueChangeListener { listener.invoke(it.value) }
            is PasswordField -> component.addValueChangeListener { listener.invoke(it.value) }
        }
    }

    override fun addOnChangeAction(component: Component, listener: (String) -> Unit) {
        when(component) {
            is TextField -> component.addValueChangeListener { listener.invoke(it.value) }
            is PasswordField -> component.addValueChangeListener { listener.invoke(it.value) }
        }
    }

    override fun enable(component: Component, condition: Boolean) {
        (component as HasEnabled).isEnabled = condition
    }

    override fun addClickAction(component: Component, action: () -> Unit) {
        (component as ClickNotifier<*>).addClickListener { action() }
    }

    override fun addDataProviderTo(uiComponent: UIComponent, component: Component, dataProvider: com.octopus.eternalUi.domain.db.DataProvider<out Identifiable>) {
        when(component) {
            is com.vaadin.flow.component.grid.Grid<*> -> addDataProviderToGrid(component as com.vaadin.flow.component.grid.Grid<Identifiable>, dataProvider)
        }
    }

    private fun addDataProviderToGrid(grid: com.vaadin.flow.component.grid.Grid<out Identifiable>, dataProvider: com.octopus.eternalUi.domain.db.DataProvider<out Identifiable>) {
        grid.dataProvider = DataProviderWrapper<Identifiable>(dataProvider)
    }

    override fun refresh(component: Component) {
        when (component) {
            is com.vaadin.flow.component.grid.Grid<*> -> component.dataProvider.refreshAll()
        }
    }
}