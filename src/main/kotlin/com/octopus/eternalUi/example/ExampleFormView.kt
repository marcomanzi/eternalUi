package com.octopus.eternalUi.example

 import com.octopus.eternalUi.domain.*
 import com.octopus.eternalUi.domain.db.Identifiable
 import com.octopus.eternalUi.domain.db.ListDataProvider
 import com.octopus.eternalUi.vaadinBridge.EternalUI
 import com.vaadin.flow.component.dependency.JsModule
 import com.vaadin.flow.router.Route
 import com.vaadin.flow.spring.annotation.UIScope
 import org.springframework.beans.factory.annotation.Autowired
 import org.springframework.core.io.ClassPathResource
 import org.springframework.stereotype.Component
 import org.springframework.stereotype.Service
 import java.io.FileInputStream
 import java.util.*

@Route("exampleUI")
@UIScope
@JsModule("./example-style.js")
class ExampleFormView(@Autowired var exampleForm: ExampleForm): EternalUI<ExampleFormDomain>(exampleForm)

@Component
@UIScope
class ExampleForm(@Autowired var exampleFormController: ExampleFormController): Page<ExampleFormDomain>(
        VerticalContainer("exampleFormContainer",
                Label("Example Eternal UI Application", "h1"),
                Label("A simple UI with a grid, and a CRUD on the grid entity", "h3"),
                Grid("listBasedGrid", GridBean::class, listOf("name", "surname")),
                Button("addElementToGrid", caption = "Example Button that add a bean to the grid"),
                Button("openDialog", caption = "Example Button that open a dialog"),
                Button("openDialogWithValues", caption = "Example Button that open a dialog With starting values"),
                Button("openConfirmDialog", caption = "Example Button that open a confirm dialog"),
                Button("navigateToHome", caption = "Example Button to navigate to Home"),
                DownloadButton("downloadCsv")),
        exampleFormController,
        PageDomain(ExampleFormDomain()))

@Service
class ExampleFormController(@Autowired var exampleFormBackend: ExampleFormBackend): PageController<ExampleFormDomain>(
        dataProviders = listOf(DataProvider("listBasedGrid", exampleFormBackend.listDataProvider)),
        actions = listOf(
                OnClickUIAction("addElementToGrid") { exampleFormBackend.addElementToGrid(it) },
                OnClickAction("openDialog") { exampleFormBackend.openDialog(it) },
                OnClickAction("openDialogWithValues") { exampleFormBackend.openDialogWithValues(it) },
                OnClickAction("openConfirmDialog") { exampleFormBackend.openConfirmDialog(it) },
                OnClickAction("navigateToHome") { exampleFormBackend.navigateToHome(it) },
                DownloadAction("downloadCsv", "test.csv") { ClassPathResource("testFile.csv").inputStream }))

data class ExampleFormDomain(val name: String = "")

data class GridBean(val name: String, val surname: String): Identifiable {
    override fun getUiId(): String = UUID.randomUUID().toString()
}

@Service
class ExampleFormBackend {
    val listDataProvider: ListDataProvider<GridBean> = ListDataProvider(GridBean("Marco", "Manzi"), GridBean("Francesco", "Manzi"))
    @Autowired lateinit var entityFormOnlyForEntity: ExampleFormOnlyForEntity

    fun openDialog(exampleFormDomain: ExampleFormDomain) = exampleFormDomain.apply {
        EternalUI.showInUI(ModalWindow("modalExample1", entityFormOnlyForEntity.withEntity(ExampleFormOnlyForEntityDomain()), _cssClassName = "exampleDialogCssClass"))
    }

    fun openDialogWithValues(exampleFormDomain: ExampleFormDomain) = exampleFormDomain.apply {
        EternalUI.showInUI(ModalWindow("modalExample2", entityFormOnlyForEntity.withEntity(ExampleFormOnlyForEntityDomain("Test Start Name"))))
    }

    fun navigateToHome(exampleFormDomain: ExampleFormDomain): ExampleFormDomain = exampleFormDomain.apply {
        EternalUI.navigateTo(HomeView::class.java, HomeDomain("Test Input From Example View"))
    }

    fun openConfirmDialog(it: ExampleFormDomain): ExampleFormDomain = it.apply {
        EternalUI.showInUI(ConfirmDialog("This is a test confirm dialog", { EternalUI.showInUI(UserMessage("You clicked ok")) }, { EternalUI.showInUI(UserMessage("You clicked cancel")) }))
    }

    fun addElementToGrid(it: EternalUI<ExampleFormDomain>): EternalUI<ExampleFormDomain> = it.refreshItemsAfterAction("listBasedGrid") {
        listDataProvider.elements.add(GridBean("New" + UUID.randomUUID().toString(), "Surname"))
    }
}