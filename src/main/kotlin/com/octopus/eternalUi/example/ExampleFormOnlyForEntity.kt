package com.octopus.eternalUi.example

import com.octopus.eternalUi.domain.*
import com.octopus.eternalUi.vaadinBridge.EternalUI
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.time.LocalDate

@Component
class ExampleFormOnlyForEntity(@Autowired var exampleFormOnlyForEntityController: ExampleFormOnlyForEntityController): Page<ExampleFormOnlyForEntityDomain>(
        VerticalContainer("exampleFormOnlyForEntityContainer",
                Label("Example Eternal UI Simple Form", "h1"),
                HorizontalContainer("firstLine", Input("name", InputType.Text), Input("surname", InputType.Text)),
                HorizontalContainer("secondLine", InputNumber("age", InputNumberType.Integer, min = 0, max = 100), Input("birthDate", InputType.Date)),
                HorizontalContainer("thirdLine", InputNumber("pocketAmount", InputNumberType.Currency), InputNumber("conversionRate", step = 2)),
                HorizontalContainer("forthLine", Input("description", InputType.TextArea)),
                Button("saveExampleForm", caption = "Example Save And Close Dialog")),
        exampleFormOnlyForEntityController,
        PageDomain(ExampleFormOnlyForEntityDomain())) {
    fun withEntity(exampleFormOnlyForEntityDomain: ExampleFormOnlyForEntityDomain) = apply {
        pageDomain = PageDomain(exampleFormOnlyForEntityDomain)
    }
}

@Service
class ExampleFormOnlyForEntityController(@Autowired var exampleFormOnlyForEntityBackend: ExampleFormOnlyForEntityBackend): PageController<ExampleFormOnlyForEntityDomain>(
        actions = mutableListOf(OnClickAction("saveExampleForm") { exampleFormOnlyForEntityBackend.saveAndCloseDialog(it) }))

data class ExampleFormOnlyForEntityDomain(val name: String = "", val surname: String = "", val age: Int = 18, val birthDate: LocalDate = LocalDate.now().minusYears(18), val description: String = "")

@Service
class ExampleFormOnlyForEntityBackend {
    fun saveAndCloseDialog(exampleFormOnlyForEntityDomain: ExampleFormOnlyForEntityDomain) = exampleFormOnlyForEntityDomain.apply {
        EternalUI.showInUI(UserMessage(exampleFormOnlyForEntityDomain.toString()))
        EternalUI.closeTopModalWindow()
    }
}