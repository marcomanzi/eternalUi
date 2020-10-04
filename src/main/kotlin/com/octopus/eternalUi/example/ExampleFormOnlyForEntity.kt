package com.octopus.eternalUi.example

import com.octopus.eternalUi.domain.*
import com.octopus.eternalUi.vaadinBridge.EternalUI
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.time.LocalDate

@Component
class ExampleFormOnlyForEntity(): Page<ExampleFormOnlyForEntityDomain>(
        VerticalContainer(
                Label("Example Eternal UI Simple Form", "h1"),
                HorizontalContainer(Input("name", InputType.Text), Input("surname", InputType.Text)),
                HorizontalContainer(InputNumber("age", InputNumberType.Integer, min = 0, max = 100), Input("birthDate", InputType.Date)),
                HorizontalContainer(InputNumber("pocketAmount", InputNumberType.Currency), InputNumber("conversionRate", step = 2)),
                HorizontalContainer(Input("description", InputType.TextArea)),
                Button("saveExampleForm", caption = "Example Save And Close Dialog")),
        PageDomain(ExampleFormOnlyForEntityDomain())) {
    fun withEntity(exampleFormOnlyForEntityDomain: ExampleFormOnlyForEntityDomain) = apply {
        pageDomain = PageDomain(exampleFormOnlyForEntityDomain)
    }

    fun saveExampleFormClicked(domain: ExampleFormOnlyForEntityDomain): ExampleFormOnlyForEntityDomain = domain.apply {
        EternalUI.showInUI(UserMessage(domain.toString()))
        EternalUI.closeTopModalWindow()
    }
}


data class ExampleFormOnlyForEntityDomain(val name: String = "", val surname: String = "", val age: Int = 18, val birthDate: LocalDate = LocalDate.now().minusYears(18), val description: String = "")

