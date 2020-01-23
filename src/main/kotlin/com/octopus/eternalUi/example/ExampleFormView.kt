package com.octopus.eternalUi.example

 import com.octopus.eternalUi.domain.*
 import com.octopus.eternalUi.vaadinBridge.EternalUI
 import com.vaadin.flow.component.dependency.JsModule
 import com.vaadin.flow.router.Route
 import com.vaadin.flow.spring.annotation.UIScope
 import org.springframework.beans.factory.annotation.Autowired
 import org.springframework.stereotype.Component
 import org.springframework.stereotype.Service

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
                Button("openDialog", caption = "Example Button that open a dialog"),
                Button("openDialogWithValues", caption = "Example Button that open a dialog With starting values"),
                Button("navigateToHome", caption = "Example Button to navigate to Home")),
        exampleFormController,
        PageDomain(ExampleFormDomain()))

@Service
class ExampleFormController(@Autowired var exampleFormBackend: ExampleFormBackend): PageController<ExampleFormDomain>(
        actions = listOf(OnClickAction("openDialog") { exampleFormBackend.openDialog(it) },
                OnClickAction("openDialogWithValues") { exampleFormBackend.openDialogWithValues(it) },
                OnClickAction("navigateToHome") { exampleFormBackend.navigateToHome(it) }))

data class ExampleFormDomain(val name: String = "")

@Service
class ExampleFormBackend {
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
}