package com.octopus.eternalUi.vaadinBridge

import com.octopus.eternalUi.domain.*
import com.octopus.eternalUi.domain.db.DataProviderWrapper
import com.octopus.eternalUi.domain.db.Identifiable
import com.octopus.eternalUi.domain.db.Message
import com.vaadin.flow.component.*
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.*
import com.vaadin.flow.data.value.ValueChangeMode
import com.vaadin.flow.router.RouterLink
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@Suppress("UNCHECKED_CAST")
class Vaadin14UiElementsHandler: VaadinElementsHandler {
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
                is InputNumber -> createFor(uiComponent)
                is Button -> com.vaadin.flow.component.button.Button(uiComponent.caption)
                is Grid -> com.vaadin.flow.component.grid.Grid(uiComponent.elementType.java).apply { setupGrid(this, uiComponent.columns) }
                is InsideAppLink -> RouterLink(uiComponent.caption, uiComponent.uiViewClass)
                else -> Div()
            }

    override fun addCssClass(component: Component, uiComponent: UIComponent) {
        if (component is HasStyle) (component as HasStyle).addClassName(uiComponent.javaClass.simpleName)
    }

    override fun addCssClass(component: Component, cssClassName: String) {
        if (cssClassName.isNotEmpty()) (component as HasStyle).addClassName(cssClassName)
    }

    private fun setupGrid(grid: com.vaadin.flow.component.grid.Grid<out Any>, columns: List<String>) {
        grid.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.SINGLE)
        grid.setColumns(*columns.toTypedArray())
        grid.columns.forEach { it.isSortable = false }
    }

    private fun createFor(input: Input): Component =
            when(input.type) {
                InputType.Text -> TextField(UtilsUI.captionFromId(input.caption)).apply { valueChangeMode = ValueChangeMode.EAGER }
                InputType.Password -> PasswordField(UtilsUI.captionFromId(input.caption)).apply { valueChangeMode = ValueChangeMode.EAGER }
                InputType.Select -> ComboBox<Message>(UtilsUI.captionFromId(input.caption)).apply {
                    isClearButtonVisible = true
                }
                InputType.Date -> DatePicker(UtilsUI.captionFromId(input.caption))
            }

    private fun createFor(input: InputNumber): Component {
        return when(input.type) {
            InputNumberType.Double -> NumberField(UtilsUI.captionFromId(input.caption)).apply {
                valueChangeMode = ValueChangeMode.EAGER
                input.min?.let { min = it.toDouble() }; input.max?.let { max = it.toDouble() }
                input.step?.let {
                    step = it.toDouble()
                    setHasControls(true)
                }
            }
            InputNumberType.Integer -> IntegerField(UtilsUI.captionFromId(input.caption)).apply {
                valueChangeMode = ValueChangeMode.EAGER
                input.min?.let { min = it.toInt() }; input.max?.let { max = it.toInt() }
                input.step?.let {
                    step = it.toInt()
                    setHasControls(true)
                }
            }
            InputNumberType.BigDecimal -> BigDecimalField(UtilsUI.captionFromId(input.caption)).apply { valueChangeMode = ValueChangeMode.EAGER }
            InputNumberType.Currency -> BigDecimalField(UtilsUI.captionFromId(input.caption)).apply { valueChangeMode = ValueChangeMode.EAGER
                addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT);
                prefixComponent = Icon(VaadinIcon.EURO)
            }
        }
    }

    override fun cleanView(component: Component) {
        component.apply { (this as HasComponents).removeAll() }
    }

    override fun setValue(fieldValue: Any?, componentById: Component) {
        when(componentById) {
            is TextField -> componentById.value = fieldValue.toString()
            is PasswordField -> componentById.value = fieldValue.toString()
            is DatePicker -> componentById.value = fieldValue as LocalDate
            is NumberField -> componentById.value = fieldValue as Double
            is IntegerField -> componentById.value = fieldValue as Int
            is BigDecimalField -> componentById.value = fieldValue as BigDecimal
            is ComboBox<*> -> if (fieldValue != null && fieldValue.toString() != "") componentById.value = Message(fieldValue.toString())
            is com.vaadin.flow.component.grid.Grid<*> ->
                if (fieldValue == null) componentById.deselectAll()
                else if (fieldValue is Optional<*> && fieldValue.isPresent) {
                    (componentById as com.vaadin.flow.component.grid.Grid<Any>).select(fieldValue.get())
                }
        }
    }

    override fun getValue(componentById: Component): Any? =
            when(componentById) {
                is NumberField -> componentById.value?.toDouble()?:0.0
                is IntegerField -> componentById.value?.toInt()?:0
                is BigDecimalField -> componentById.value
                is com.vaadin.flow.component.grid.Grid<*> -> if (componentById.selectedItems.isNotEmpty()) componentById.selectedItems.first() else null
                else -> (componentById as AbstractField<*, *>).getValue()
            }

    override fun addValueChangeListener(component: Component, listener: (Any) -> Unit) {
        when(component) {
            is ComboBox<*> -> component.addValueChangeListener { listener.invoke((it.value?:"").toString()) }
            is com.vaadin.flow.component.grid.Grid<*> -> component.addSelectionListener { listener.invoke(it.firstSelectedItem) }
            else -> (component as AbstractField<*, *>).addValueChangeListener { field ->
                field.value?.let { newValue -> listener.invoke(newValue) } }
        }
    }

    override fun addOnChangeAction(component: Component, listener: (Any) -> Unit) {
        when(component) {
            is ComboBox<*> -> component.addValueChangeListener { listener.invoke(it.value) }
            is com.vaadin.flow.component.grid.Grid<*> -> component.addSelectionListener { listener.invoke(it.firstSelectedItem) }
            else -> (component as AbstractField<*, *>).addValueChangeListener { listener.invoke(it.value) }
        }
    }

    override fun enable(component: Component, condition: Boolean) {
        (component as HasEnabled).isEnabled = condition
    }

    override fun addClickAction(component: Component, action: () -> Unit) {
        when(component) {
            is com.vaadin.flow.component.grid.Grid<*> -> component.addItemClickListener { action() }
            else -> (component as ClickNotifier<*>).addClickListener { action() }
        }
    }

    override fun addDataProviderTo(uiComponent: UIComponent, component: Component, dataProvider: com.octopus.eternalUi.domain.db.DataProvider<out Identifiable>) {
        when(component) {
            is ComboBox<*> -> addDataProviderToSelect(component as ComboBox<Identifiable>, dataProvider)
            is com.vaadin.flow.component.grid.Grid<*> -> addDataProviderToGrid(component as com.vaadin.flow.component.grid.Grid<Identifiable>, dataProvider)
        }
    }

    private fun addDataProviderToSelect(select: ComboBox<Identifiable>, dataProvider: com.octopus.eternalUi.domain.db.DataProvider<out Identifiable>) {
        select.setDataProvider(DataProviderWrapper<Identifiable>(dataProvider))
    }

    private fun addDataProviderToGrid(grid: com.vaadin.flow.component.grid.Grid<out Identifiable>, dataProvider: com.octopus.eternalUi.domain.db.DataProvider<out Identifiable>) {
        grid.dataProvider = DataProviderWrapper<Identifiable>(dataProvider)
    }

    override fun refresh(component: Component) {
        when (component) {
            is ComboBox<*> -> component.dataProvider.refreshAll()
            is com.vaadin.flow.component.grid.Grid<*> -> component.dataProvider.refreshAll()
        }
    }

    private val dialogKeyInSession = "LAST_OPENED_DIALOG"
    override fun <T: Any> showModalWindow(modalWindow: ModalWindow<T>) {
        Dialog().apply {
            add(EternalUI(modalWindow.page).prepareUI().mainPageComponentForUI())
            addDialogCloseActionListener { modalWindow.onClose(modalWindow.page.pageDomain.dataClass) }
            UI.getCurrent().session.setAttribute(dialogKeyInSession, this)
        }.open()
    }

    override fun closeTopModalWindow() {
        if (UI.getCurrent().session.getAttribute(dialogKeyInSession) != null) {
            (UI.getCurrent().session.getAttribute(dialogKeyInSession) as Dialog).close()
        }
    }

    override fun showUserMessage(userMessage: UserMessage) {
        Notification(userMessage.message).apply {
            // when(userMessage.type) //TODO
            duration = 2000
            open()
        }
    }
}
