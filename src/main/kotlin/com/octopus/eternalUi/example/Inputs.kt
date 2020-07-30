package com.octopus.eternalUi.example

import com.octopus.eternalUi.domain.*
import com.octopus.eternalUi.domain.db.ListDataProvider
import com.octopus.eternalUi.domain.db.Message
import com.octopus.eternalUi.vaadinBridge.EternalUI
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.spring.annotation.UIScope
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class SimpleInputs: Page<EmptyDomain>(
        VerticalContainer(
                HorizontalContainer(Input("text"), Input("textArea", InputType.TextArea), Input("password", InputType.Password), Input("select", InputType.Select), Input("date", InputType.Date)),
                HorizontalContainer(Input("radio", InputType.Radio), Input("select", InputType.Checkbox))),
        pageDomain = PageDomain(EmptyDomain())
) {
    fun selectDataProvider() = ListDataProvider<Message>("selectChoice1", "selectChoice2")
    fun radioDataProvider() = ListDataProvider<Message>("radioChoice1", "radioChoice2")
}

@Component @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class SimpleForm: Page<SimpleFormData>(
        VerticalContainer(
                HorizontalContainer(Input("name"), Input("surname"), Input("age", InputType.Date),
                Input("preferredFood", InputType.Radio), Input("newUser", InputType.Checkbox)),
                Button("showFormData", _cssClassName = "red"),
                Button("prefillForm")
        ),
        pageDomain = PageDomain(SimpleFormData())
) {
    fun preferredFoodDataProvider() = ListDataProvider<Message>("meat", "fish", "vegetables")

    fun showFormDataClicked(simpleFormData: SimpleFormData): SimpleFormData = simpleFormData.apply {
        EternalUI.showInUI(UserMessage(this.toString()))
    }

    fun prefillFormClicked(simpleFormData: SimpleFormData): SimpleFormData = SimpleFormData("TestName", "TestSurname",
            LocalDate.of(1983, 10, 17), "meat", true)
}

data class SimpleFormData(val name: String = "", val surname: String = "", val age: LocalDate = LocalDate.of(1983, 1, 1),
                          val preferredFood: String = "", val newUser: Boolean = false, val metadata: MutableMap<String, Any?> = mutableMapOf())

