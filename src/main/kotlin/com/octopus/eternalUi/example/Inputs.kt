package com.octopus.eternalUi.example

import com.octopus.eternalUi.domain.*
import com.octopus.eternalUi.domain.db.ListDataProvider
import com.octopus.eternalUi.vaadinBridge.EternalUI
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class SimpleInputs: Page(
    VerticalContainer(
        HorizontalContainer(Input("text"), Input("textArea", InputType.TextArea), Input("password", InputType.Password), Input("select", InputType.Select), Input("date", InputType.Date)),
        HorizontalContainer(Input("radio", InputType.Radio), Input("select", InputType.Checkbox))),
    pageDomain = EmptyDomain()
) {
    fun selectDataProvider() = ListDataProvider("selectChoice1", "selectChoice2")
    fun radioDataProvider() = ListDataProvider("radioChoice1", "radioChoice2")
}

@Component @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class SimpleForm: Page(
    VerticalContainer(
        HorizontalContainer(Label("Name: "), Anchor("testAnchor", caption = "\$name")),
        HorizontalContainer(Input("name"), Input("surname"), Input("age", InputType.Date),
            Input("preferredFood", InputType.Radio), Input("newUser", InputType.Checkbox)),
        Button("showFormData", _cssClassName = "red"),
        Button("prefillForm")
    ),
    pageDomain = SimpleFormData(),
    beforeEnter = { SimpleFormData("Initial Name") }
) {
    fun preferredFoodDataProvider() = ListDataProvider("meat", "fish", "vegetables")

    fun testAnchorClicked(sf: SimpleFormData): SimpleFormData = sf.apply {
        EternalUI.showInUI(UserMessage("Name is $name"))
    }

    fun showFormDataClicked(simpleFormData: SimpleFormData): SimpleFormData = simpleFormData.apply {
        EternalUI.showInUI(UserMessage(this.toString()))
    }

    fun prefillFormClicked(simpleFormData: SimpleFormData): SimpleFormData = SimpleFormData("TestName", "TestSurname",
        LocalDate.of(1983, 10, 17), "meat", true)
}

data class SimpleFormData(val name: String = "", val surname: String = "", val age: LocalDate = LocalDate.of(1983, 1, 1),
                          val preferredFood: String = "", val newUser: Boolean = false, val metadata: MutableMap<String, Any?> = mutableMapOf())

