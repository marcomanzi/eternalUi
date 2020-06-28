package com.octopus.eternalUi.example

import com.octopus.eternalUi.domain.*
import com.octopus.eternalUi.domain.db.ListDataProvider
import com.octopus.eternalUi.domain.db.Message
import com.octopus.eternalUi.vaadinBridge.EternalUI
import com.octopus.eternalUi.vaadinBridge.domain_session_key
import com.vaadin.flow.spring.annotation.UIScope
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class DynamicLayout: Page<DynamicLayoutData>(
        VerticalContainer(Input("select", InputType.Select)),
        pageDomain = PageDomain(DynamicLayoutData())
) {
    fun selectDataProvider() = ListDataProvider<Message>("selectChoice1", "selectChoice2")
    fun selectAddedAfterDataProvider() = ListDataProvider<Message>("selectChoice3", "selectChoice4")
    fun selectChanged(ui: EternalUI<DynamicLayoutData>): EternalUI<DynamicLayoutData> {
        if (!ui.page.pageDomain.dataClass.addedSecondComponent)
            ui.getContainerUIComponentByChildId("select").map { containerUIComponent ->
                containerUIComponent.containedUIComponents.add(Input("selectAddedAfter", InputType.Select))
            }

        ui.page.pageDomain.dataClass.addedSecondComponent = true
        EternalUI.showInUI(UserMessage("Adding Select for selection ${ui.page.pageDomain.dataClass.select}"))
        EternalUI.setInSession(domain_session_key, ui.page.pageDomain.dataClass)
        ui.beforeEnter(null)
        return ui
    }
}

data class DynamicLayoutData(val select: String = "selectChoice1", val selectAddedAfter: String = "selectChoice3", var addedSecondComponent: Boolean = false)

