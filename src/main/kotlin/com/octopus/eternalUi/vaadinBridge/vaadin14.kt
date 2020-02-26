package com.octopus.eternalUi.vaadinBridge

import com.octopus.eternalUi.domain.*
import com.octopus.eternalUi.domain.db.DataProviderWrapper
import com.octopus.eternalUi.domain.db.Identifiable
import com.octopus.eternalUi.domain.db.Message
import com.vaadin.flow.component.*
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.component.textfield.*
import com.vaadin.flow.data.value.ValueChangeMode
import com.vaadin.flow.router.RouterLink
import com.vaadin.flow.server.InputStreamFactory
import com.vaadin.flow.server.StreamResource
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
            if (parent is Tabs) {
                parent.apply { parent.selectedIndex = 1 }
            } else {
                parent.apply { (this as HasComponents).add(*children.toTypedArray()) }
            }

    override fun createFor(uiComponent: UIComponent): Component =
            when(uiComponent) {
                is VerticalContainer -> VerticalLayout().apply { isSpacing = false; isMargin = false; setSizeFull() }
                is HorizontalContainer -> HorizontalLayout().apply { isMargin = false; setSizeFull() }
                is TabsContainer -> Tabs().apply { setupTabs(uiComponent, this) }
                is com.octopus.eternalUi.domain.Label -> Label(uiComponent.caption)
                is Input -> createFor(uiComponent)
                is InputNumber -> createFor(uiComponent)
                is Button -> com.vaadin.flow.component.button.Button(uiComponent.caption)
                is DownloadButton -> downloadButton(uiComponent)
                is Grid -> com.vaadin.flow.component.grid.Grid(uiComponent.elementType.java).apply { setupGrid(this, uiComponent.columns) }
                is InsideAppLink -> RouterLink(uiComponent.caption, uiComponent.uiViewClass)
                else -> Div()
            }

    private fun setupTabs(uiComponent: UIComponent, tabs: Tabs) {
        tabs.setSizeFull()
        uiComponent.containedUIComponents.forEach { tabs.add(Tab(captionFrom(it.id))) }
        tabs.addSelectedChangeListener {
            val tabToShow = uiComponent.containedUIComponents.first { ui -> (it.selectedTab.label == captionFrom(ui.id)) } as com.octopus.eternalUi.domain.Tab<*>
            tabs.parent.ifPresent { parent ->
                addToParent(parent, EternalUI(tabToShow.page).prepareUI().mainPageComponentForUI())
            }
        }
    }

    private fun downloadButton(downloadButton: DownloadButton): Component {
        val download = Anchor("")
        download.element.setAttribute("download", true)
        download.add(com.vaadin.flow.component.button.Button(downloadButton.caption))
        return download
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

    override fun getMainComponentFor(componentById: Component): Component =
            if (componentById is Anchor) componentById.children.findFirst().get()
            else componentById

    private fun createFor(input: Input): Component =
            when(input.type) {
                InputType.Text -> TextField(UtilsUI.captionFromId(input.caption)).apply { valueChangeMode = ValueChangeMode.EAGER }
                InputType.TextArea -> TextArea(UtilsUI.captionFromId(input.caption)).apply { valueChangeMode = ValueChangeMode.EAGER }
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
            is TextArea -> componentById.value = fieldValue.toString()
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

    override fun <T : Any> addDownloadInputStream(action: DownloadAction<T>, domain: T, componentById: Component) {
        (componentById as Anchor).setHref(StreamResource(action.fileName, InputStreamFactory { action.onDataDomainInputStream(domain) }))
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

    override fun refresh(component: Component, identifiable: Identifiable) {
        when (component) {
            is ComboBox<*> -> (component as ComboBox<Identifiable>).dataProvider.refreshItem(identifiable)
            is com.vaadin.flow.component.grid.Grid<*> -> (component as com.vaadin.flow.component.grid.Grid<Identifiable>).dataProvider.refreshItem(identifiable)
        }
    }

    private val dialogKeyInSession = "LAST_OPENED_DIALOG"
    private val confirmDialogKeyInSession = "LAST_OPENED_CONFIRM_DIALOG"
    override fun <T: Any> showModalWindow(modalWindow: ModalWindow<T>) {
        if (UI.getCurrent().session.getAttribute(dialogKeyInSession) == null) {
            Dialog().apply {
                val mainPageComponentForUI = EternalUI(modalWindow.page).prepareUI().mainPageComponentForUI()
                addCssClass(mainPageComponentForUI, modalWindow.cssClassName)
                add(mainPageComponentForUI)
                addDialogCloseActionListener { modalWindow.onClose(modalWindow.page.pageDomain.dataClass) }
                UI.getCurrent().session.setAttribute(dialogKeyInSession, this)
                isCloseOnEsc = true
            }.open()
            UI.getCurrent().addShortcutListener(ShortcutEventListener { closeTopModalWindow() } , Key.ESCAPE)
        }
    }

    override fun showConfirmDialog(confirmDialog: ConfirmDialog) {
        if (UI.getCurrent().session.getAttribute(confirmDialogKeyInSession) == null) {
            Dialog().apply {
                val mainPageComponentForUI = EternalUI(ConfirmDialogPage(confirmDialog)).prepareUI().mainPageComponentForUI()
                addCssClass(mainPageComponentForUI, confirmDialog.cssClassName)
                add(mainPageComponentForUI)
                addDialogCloseActionListener { confirmDialog.onCancel() }
                UI.getCurrent().session.setAttribute(confirmDialogKeyInSession, this)
                isCloseOnEsc = true
            }.open()
        }
    }

    override fun closeTopModalWindow() {
        if (UI.getCurrent().session.getAttribute(dialogKeyInSession) != null) {
            (UI.getCurrent().session.getAttribute(dialogKeyInSession) as Dialog).close()
            UI.getCurrent().session.setAttribute(dialogKeyInSession, null)
        }
    }

    override fun closeConfirmDialog() {
        if (UI.getCurrent().session.getAttribute(confirmDialogKeyInSession) != null) {
            (UI.getCurrent().session.getAttribute(confirmDialogKeyInSession) as Dialog).close()
            UI.getCurrent().session.setAttribute(confirmDialogKeyInSession, null)
        }
    }

    override fun showUserMessage(userMessage: UserMessage) {
        Notification(userMessage.message).apply {
            // when(userMessage.type) //TODO
            duration = 2000
            open()
        }
    }

    override fun navigateTo(uiComponent: Class<out Component>) {
        UI.getCurrent().navigate(uiComponent)
    }

    override fun setInSession(key: String, value: Any) {
        UI.getCurrent().session.setAttribute(key, value)
    }

    override fun removeFromSession(key: String) {
        UI.getCurrent().session.setAttribute(key, null)
    }

    override fun getFromSession(key: String): Any? {
        return UI.getCurrent().session.getAttribute(key)
    }
}
