package com.octopus.eternalUi.example

 import com.octopus.eternalUi.domain.*
import com.octopus.eternalUi.example.domain.*
import com.octopus.eternalUi.example.user.UserDomain
 import com.octopus.eternalUi.vaadinBridge.EternalUI
 import com.vaadin.flow.spring.annotation.UIScope
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Component
class ExampleFormOnlyForEntity(@Autowired var exampleFormOnlyForEntityController: ExampleFormOnlyForEntityController): Page<ExampleFormOnlyForEntityDomain>(
        VerticalContainer("exampleFormOnlyForEntityContainer",
                Label("Example Eternal UI Simple Form", "h1"),
                Input("name", InputType.Text),
                Button("saveExampleForm", caption = "Example Save And Close Dialog")),
        exampleFormOnlyForEntityController,
        PageDomain(ExampleFormOnlyForEntityDomain()))

@Service
class ExampleFormOnlyForEntityController(@Autowired var exampleFormOnlyForEntityBackend: ExampleFormOnlyForEntityBackend): PageController<ExampleFormOnlyForEntityDomain>(
        actions = listOf(OnClickAction("saveExampleForm") { exampleFormOnlyForEntityBackend.saveAndCloseDialog(it) }))

data class ExampleFormOnlyForEntityDomain(val name: String = "")

@Service
class ExampleFormOnlyForEntityBackend {
    fun saveAndCloseDialog(exampleFormOnlyForEntityDomain: ExampleFormOnlyForEntityDomain) = exampleFormOnlyForEntityDomain.apply {
        EternalUI.closeTopModalWindow()
    }
}