package com.octopus.eternalUi.vaadinBridge

import com.octopus.eternalUi.domain.*
import com.octopus.eternalUi.domain.db.DataProvider
import com.octopus.eternalUi.domain.db.DataProviderWrapper
import com.octopus.eternalUi.domain.db.Identifiable
import com.octopus.eternalUi.domain.db.Message
import com.octopus.eternalUi.vaadinBridge.vaadin15.ComponentHandler
import com.vaadin.flow.component.*
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.radiobutton.RadioButtonGroup
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.component.textfield.*
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.data.value.ValueChangeMode
import com.vaadin.flow.function.SerializableFunction
import com.vaadin.flow.router.RouterLink
import com.vaadin.flow.server.InputStreamFactory
import com.vaadin.flow.server.StreamResource
import java.util.*
import kotlin.concurrent.schedule
import kotlin.streams.asSequence
import com.vaadin.flow.component.grid.Grid as VaadinGrid
import com.vaadin.flow.component.html.Label as VaadinLabel

@Suppress("UNCHECKED_CAST")
class Vaadin15UiElementsHandler : VaadinElementsHandler {

    override fun debugButton(toDebugStringSupplier: () -> String): Component =
            com.vaadin.flow.component.button.Button("Debug").apply { addClickListener {
                Dialog(VaadinLabel().apply { element.setProperty("innerHTML", toDebugStringSupplier()) }).apply {
                    open()
                    isCloseOnEsc = true
                }}}

    override fun switchCaptionTo(component: Component, newCaption: String) {
        ComponentHandler.switchCaptionTo(component, newCaption)
    }

    override fun reloadPage() {
        UI.getCurrent().page.reload()
    }

    override fun addToParent(parent: Component, children: List<Component>): Component =
            if (parent is Tabs) {
                parent.apply { parent.selectedIndex = 0 }
            } else {
                parent.apply {
                    if (this is HorizontalLayout) {
                        this.add(*children.toTypedArray())
                        this.setVerticalComponentAlignment(FlexComponent.Alignment.END, *children.filterIsInstance<RadioButtonGroup<*>>().toTypedArray())
                        children.filterIsInstance<Checkbox>().toTypedArray().let { checkBoxes ->
                            checkBoxes.forEach { it.style.set("padding-bottom", "7px") }
                            this.setVerticalComponentAlignment(FlexComponent.Alignment.END, *checkBoxes)
                        }
                    } else {
                        (this as HasComponents).add(*children.toTypedArray())
                    }
                }
            }

    override fun removeFromContainer(container: HasComponents) {
        container.removeAll()
    }

    override fun addCssClass(component: Component, uiComponent: UIComponent) {
        if (component is HasStyle) (component as HasStyle).addClassName(uiComponent.javaClass.simpleName)
    }

    override fun addCssClass(component: Component, cssClassName: String) {
        if (cssClassName.isNotEmpty()) (component as HasStyle).addClassName(cssClassName)
    }

    private fun UIComponent.asInput() = this as Input
    private fun UIComponent.asInputNumber() = this as InputNumber
    private val objectToVaadinCreator: Map<Class<out UIComponent>, (UIComponent) -> Component > = mapOf(
            Pair(VerticalContainer::class.java, { _ -> VerticalLayout().apply { isSpacing = false; isMargin = false; setSizeFull() }}),
            Pair(HorizontalContainer::class.java, { _ -> HorizontalLayout().apply { isMargin = false; setSizeFull() } }),
            Pair(TabsContainer::class.java, { it -> Tabs().apply { setupTabs(it, this) } }),
            Pair(Input::class.java, { it -> enumToVaadinCreator[it.asInput().type]?.invoke(it)?: Div() }),
            Pair(InputNumber::class.java, { it -> enumToVaadinCreator[it.asInputNumber().type]?.invoke(it)?: Div() }),
            Pair(Label::class.java, { it -> VaadinLabel((it as Label).caption).apply {
                if (it.caption == "") height = "20px"
            }}),
            Pair(Button::class.java, { it -> com.vaadin.flow.component.button.Button((it as Button).caption) }),
            Pair(DownloadButton::class.java, { it -> downloadButton(it as DownloadButton) }),
            Pair(Grid::class.java, { it -> grid(it) }),
            Pair(InsideAppLink::class.java, { it -> RouterLink((it as InsideAppLink).caption, it.uiViewClass) })
    )

    private fun grid(it: UIComponent): Component = if ((it as Grid).elementType.java is Map<*, *>) {
        VaadinGrid(MutableMap::class.java).apply { setupGrid(this as VaadinGrid<Any>, it) }
    } else {
        VaadinGrid(it.elementType.java).apply { setupGrid(this as VaadinGrid<Any>, it) }
    }

    private val enumToVaadinCreator: Map<Enum<*>, (UIComponent) -> Component> = mapOf(
            Pair(InputType.Text, { it -> TextField(UtilsUI.captionFromId(it.asInput().caption)).apply { valueChangeMode = ValueChangeMode.EAGER } }),
            Pair(InputType.TextArea, { it -> TextArea(UtilsUI.captionFromId(it.asInput().caption)).apply { valueChangeMode = ValueChangeMode.EAGER } }),
            Pair(InputType.Password, { it -> PasswordField(UtilsUI.captionFromId(it.asInput().caption)).apply { valueChangeMode = ValueChangeMode.EAGER } }),
            Pair(InputType.Select, { it ->
                ComboBox<Message>(UtilsUI.captionFromId(it.asInput().caption)).apply {
                    isClearButtonVisible = true
                    isPreventInvalidInput = true
                }
            }),
            Pair(InputType.Date, { it -> DatePicker(UtilsUI.captionFromId(it.asInput().caption)).apply { isClearButtonVisible = true } }),
            Pair(InputType.Radio, { _ -> RadioButtonGroup<String>() }),
            Pair(InputType.Checkbox, { it -> Checkbox(UtilsUI.captionFromId(it.asInput().caption)) }),
            Pair(InputNumberType.Double, { it -> numberField(it.asInputNumber()) }),
            Pair(InputNumberType.Integer, { it -> integerField(it.asInputNumber()) }),
            Pair(InputNumberType.BigDecimal, { it -> BigDecimalField(UtilsUI.captionFromId(it.asInputNumber().caption)).apply { valueChangeMode = ValueChangeMode.EAGER } }),
            Pair(InputNumberType.Currency, { it -> BigDecimalField(UtilsUI.captionFromId(it.asInputNumber().caption)).apply { valueChangeMode = ValueChangeMode.EAGER
                addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT)
                prefixComponent = Icon(VaadinIcon.EURO)
            }})
    )

    override fun createFor(uiComponent: UIComponent): Component {
        return objectToVaadinCreator[uiComponent::class.java]?.invoke(uiComponent)?: Div()
    }

    private fun integerField(input: InputNumber): Component =
            IntegerField(UtilsUI.captionFromId(input.caption)).apply {
                valueChangeMode = ValueChangeMode.EAGER
                input.min?.let { min = it.toInt() }; input.max?.let { max = it.toInt() }
                input.step?.let {
                    step = it.toInt()
                    setHasControls(true)
                }
            }

    private fun numberField(input: InputNumber): Component =
            NumberField(UtilsUI.captionFromId(input.caption)).apply {
                valueChangeMode = ValueChangeMode.EAGER
                input.min?.let { min = it.toDouble() }; input.max?.let { max = it.toDouble() }
                input.step?.let {
                    step = it.toDouble()
                    setHasControls(true)
                }
            }

    private fun setupTabs(uiComponent: UIComponent, tabs: Tabs) {
        tabs.setSizeFull()
        uiComponent.containedUIComponents.forEach { tabs.add(com.vaadin.flow.component.tabs.Tab((it as Tab<*>).caption)) }
        tabs.addSelectedChangeListener {
            val tabToShow = uiComponent.containedUIComponents.first { ui -> (it.selectedTab.label == (ui as Tab<*>).caption) } as Tab<*>
            it.selectedTab.parent.ifPresent { container ->
                val tabsContainer = container.parent.get() as VerticalLayout
                tabsContainer.remove(tabsContainer.children.asSequence().last())
                addToParent(tabsContainer, EternalUI(tabToShow.page).prepareUI().mainPageComponentForUI())
            }
        }
    }

    override fun setCaption(componentById: Component, caption: String) {
        fun methodOrNull(component: Component, methodName: String) =
                component::class.java.methods.firstOrNull { it.name == methodName }
        methodOrNull(componentById, "setLabel")?: methodOrNull(componentById, "setText")?.invoke(componentById, caption)
    }

    override fun removeComponent(componentById: Component) {
        componentById.parent.map { parent -> (parent as HasComponents).remove(componentById) }
    }

    private fun setupGrid(grid: VaadinGrid<Any>, uiGrid: Grid) {
        grid.setSelectionMode(when(uiGrid.gridConfiguration.gridSelectionType) {
            GridSelectionType.SINGLE -> VaadinGrid.SelectionMode.SINGLE
            GridSelectionType.MULTI -> VaadinGrid.SelectionMode.MULTI
            else -> VaadinGrid.SelectionMode.NONE
        })
        uiGrid.columns.filter { c -> grid.columns.map { it.key }.contains(c).not() }.forEach { columnName ->
            grid.addColumn { (it as Map<String, *>)[columnName] }.apply {
                key = columnName
                setHeader(captionFrom(columnName))
            }
        }

        uiGrid.gridConfiguration.columnGenerators.keys.forEach {
            grid.removeColumnByKey(it)
            grid.addColumn(ComponentRenderer(SerializableFunction<Any, Component> { item: Any ->
                val component = uiGrid.gridConfiguration.columnGenerators[it]
                val ui = component as UIComponent
                ui.javaClass.methods.firstOrNull { method -> method.name == "setCaption" }?.invoke(ui, "")
                setupComponentForGrid(item, it, grid, createFor(ui))
            })).setKey(it).setHeader(captionFrom(it))
        }

        grid.columns.forEach {
            it.isSortable = false
            it.isVisible = uiGrid.columns.contains(it.key)
        }

        grid.setColumnOrder(grid.columns.sortedWith(columnsComparator(uiGrid.columns)) )

        grid.isHeightByRows = true
        grid.height = "${uiGrid.gridConfiguration.rowsToShow}"
    }

    private fun columnsComparator(columns: List<String>) = Comparator<com.vaadin.flow.component.grid.Grid.Column<Any>> { c1, c2 ->
        columns.indexOf(c1.key).compareTo(columns.indexOf(c2.key))
    }

    private val componentSetupForGrid: Map<Class<out Component>, (Any, String, VaadinGrid<Any>, Component) -> Unit > = mapOf(
            Pair(TextField::class.java, { item, key, _, it -> (it as TextField).apply {
                label = ""
                if (item is Map<*, *>) {
                    addValueChangeListener { v -> (item as MutableMap<String, Any?>)[key] = v.value }
                    setValue(item[key], this)
                } else {
                    val f = item.javaClass.getDeclaredField(key)
                    f.isAccessible = true
                    addValueChangeListener { v -> f.set(item, v.value) }
                    setValue(f.get(item), this)
                }
            } }),
            Pair(ComboBox::class.java, { item, key, _, it -> (it as ComboBox<Identifiable>).apply {
                label = ""
                if (item is Map<*, *>) {
                    addDataProviderToSelect(this, (item as MutableMap<String, Any?>)[key + "DataProvider"] as DataProvider<out Identifiable>)
                    addValueChangeListener { v -> item[key] = v.value }
                    setValue(item[key], this)
                } else {
                    val dataProvider = item.javaClass.getDeclaredField(key + "DataProvider")
                    dataProvider.isAccessible = true
                    addDataProviderToSelect(this, dataProvider.get(item) as DataProvider<out Identifiable>)

                    val f = item.javaClass.getDeclaredField(key)
                    f.isAccessible = true
                    addValueChangeListener { v -> f.set(item, v.value) }
                    setValue(f.get(item), this)
                }
            } })
    )

    private fun setupComponentForGrid(item: Any, key: String, grid: VaadinGrid<Any>, component: Component): Component = component.apply {
        componentSetupForGrid[component::class.java]?.invoke(item, key, grid, this)
    }

    private fun downloadButton(downloadButton: DownloadButton): Component {
        val download = Anchor("")
        download.element.setAttribute("download", true)
        download.add(com.vaadin.flow.component.button.Button(downloadButton.caption))
        return download
    }

    override fun getMainComponentFor(componentById: Component): Component =
            if (componentById is Anchor) componentById.children.findFirst().get()
            else componentById

    override fun cleanView(component: Component) {
        component.apply { (this as HasComponents).removeAll() }
    }

    override fun setValue(fieldValue: Any?, componentById: Component) = ComponentHandler.setValue(fieldValue, componentById)

    override fun getValue(componentById: Component): Any? =
            when(componentById) {
                is NumberField -> componentById.value?.toDouble()?:0.0
                is IntegerField -> componentById.value?.toInt()?:0
                is BigDecimalField -> componentById.value
                is VaadinGrid<*> -> if (componentById.selectedItems.isNotEmpty()) componentById.selectedItems.first() else null
                else -> (componentById as AbstractField<*, *>).getValue()
            }

    override fun addValueChangeListener(component: Component, listener: (Any?) -> Unit) {
        when(component) {
            is ComboBox<*> -> component.addValueChangeListener { if (it.oldValue?.toString()?:"" != it.value?.toString()?:"") listener.invoke(it.value) }
            is VaadinGrid<*> -> component.addSelectionListener { if (it.isFromClient) listener.invoke(it.allSelectedItems) }
            else -> (component as AbstractField<*, *>).addValueChangeListener { field ->
                field.value.let { newValue -> listener.invoke(newValue) } }
        }
    }

    override fun addOnChangeAction(component: Component, listener: (Any) -> Unit) {
        when(component) {
            is ComboBox<*> -> component.addValueChangeListener { if (it.oldValue?.toString()?:"" != it.value?.toString()?:"") listener.invoke(it.value) }
            is VaadinGrid<*> -> component.addSelectionListener { listener.invoke(it.allSelectedItems) }
            else -> (component as AbstractField<*, *>).addValueChangeListener { listener.invoke(it.value) }
        }
    }

    override fun enable(component: Component, condition: Boolean) {
        (component as HasEnabled).isEnabled = condition
    }

    override fun addClickAction(component: Component, action: () -> Unit) {
        when(component) {
            is VaadinGrid<*> -> component.addItemClickListener { action() }
            is ClickNotifier<*> -> (component as ClickNotifier<*>).addClickListener { action() }
            else -> println("Error on component ${component.id}")
        }
    }

    override fun <T : Any> addDownloadInputStream(action: DownloadAction<T>, domain: T, componentById: Component) {
        (componentById as Anchor).setHref(StreamResource(action.fileNameGenerator(domain), InputStreamFactory { action.onDataDomainInputStream(domain) }))
    }

    override fun addDataProviderTo(uiComponent: UIComponent, component: Component, dataProvider: DataProvider<out Identifiable>) {
        when(component) {
            is ComboBox<*> -> addDataProviderToSelect(component as ComboBox<Identifiable>, dataProvider)
            is RadioButtonGroup<*> -> addDataProviderToRadioButtonGroup(component as RadioButtonGroup<Identifiable>, dataProvider)
            is VaadinGrid<*> -> addDataProviderToGrid(component as VaadinGrid<Identifiable>, dataProvider)
        }
    }

    private fun addDataProviderToRadioButtonGroup(radioButtonGroup: RadioButtonGroup<Identifiable>, dataProvider: DataProvider<out Identifiable>) {
        radioButtonGroup.dataProvider = DataProviderWrapper<Identifiable>(dataProvider)
    }

    private fun addDataProviderToSelect(select: ComboBox<Identifiable>, dataProvider: DataProvider<out Identifiable>) {
        select.setDataProvider(DataProviderWrapper<Identifiable>(dataProvider))
    }

    private fun addDataProviderToGrid(grid: VaadinGrid<out Identifiable>, dataProvider: DataProvider<out Identifiable>) {
        grid.dataProvider = DataProviderWrapper<Identifiable>(dataProvider)
    }

    override fun refresh(component: Component) {
        when (component) {
            is ComboBox<*> -> component.dataProvider.refreshAll()
            is VaadinGrid<*> -> {
                component.dataProvider.refreshAll()
                val current = UI.getCurrent()
                Timer().schedule(100) { current.access { component.dataProvider.refreshAll() } }
            }
        }
    }

    override fun setReadOnly(componentById: Component, readOnly: Boolean) {
        (componentById as HasValueAndElement<*, *>).setReadOnly(readOnly)
    }

    override fun refresh(component: Component, identifiable: Identifiable) {
        when (component) {
            is ComboBox<*> -> (component as ComboBox<Identifiable>).dataProvider.refreshItem(identifiable)
            is VaadinGrid<*> -> (component as VaadinGrid<Identifiable>).dataProvider.refreshItem(identifiable)
        }
    }

    private val dialogKeyInSession = "dialogKeyInSession"
    private val modalWindowInSession = "modalWindowInSession"
    private val confirmDialogKeyInSession = "confirmDialogKeyInSession"
    override fun <T: Any> showModalWindow(modalWindow: ModalWindow<T>) {
        if (UI.getCurrent().session.getAttribute(dialogKeyInSession) == null) {
            Dialog().apply {
                val mainPageComponentForUI = EternalUI(modalWindow.page).prepareUI().mainPageComponentForUI()
                addCssClass(mainPageComponentForUI, modalWindow.cssClassName)
                add(mainPageComponentForUI)
                UI.getCurrent().session.setAttribute(dialogKeyInSession, this)
                UI.getCurrent().session.setAttribute(modalWindowInSession, modalWindow)
                addDialogCloseActionListener { closeTopModalWindow() }
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
                addDialogCloseActionListener { closeConfirmDialog(); confirmDialog.onCancel() }
                UI.getCurrent().session.setAttribute(confirmDialogKeyInSession, this)
                isCloseOnEsc = true
            }.open()
            UI.getCurrent().addShortcutListener(ShortcutEventListener { closeConfirmDialog(); confirmDialog.onCancel()  } , Key.ESCAPE)
        }
    }

    override fun closeTopModalWindow() {
        if (UI.getCurrent().session.getAttribute(dialogKeyInSession) != null) {
            (UI.getCurrent().session.getAttribute(dialogKeyInSession) as Dialog).close()
            UI.getCurrent().session.setAttribute(dialogKeyInSession, null)
            (UI.getCurrent().session.getAttribute(modalWindowInSession) as ModalWindow<Any>).apply {
                onClose(page.pageDomain.dataClass)
            }
            UI.getCurrent().session.setAttribute(modalWindowInSession, null)
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

    override fun showFileInDifferentTab(file: File) {
        // UI.getCurrent().page.executeJs("window.open('file:///Users/mmanzi/workspace/soldo/hybrid-projects/eternalUi/HELP.md', '_blank', '');");
    }

    override fun navigateTo(uiComponent: Class<out Component>) {
        UI.getCurrent().navigate(uiComponent)
    }

    override fun setInSession(key: String, value: Any) {
        UI.getCurrent().session.setAttribute(key, value)
    }

    override fun removeFromSession(domainSessionKey: String) {
        UI.getCurrent().session.setAttribute(domainSessionKey, null)
    }

    override fun getFromSession(key: String): Any? = UI.getCurrent().session.getAttribute(key)
}
