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
    fun selectDataProvider() = ListDataProvider<Message>("", "addSelect", "removeSelect", "addTab", "removeTab")
    fun selectAddedAfterDataProvider() = ListDataProvider<Message>("selectChoice3", "selectChoice4")
    fun selectChanged(ui: EternalUI<DynamicLayoutData>): EternalUI<DynamicLayoutData> {
        when (ui.page.pageDomain.dataClass.select) {
            "addSelect" -> ui.addComponent("select", Input("selectAddedAfter", InputType.Select))
            "removeSelect" -> ui.removeByComponentId("selectAddedAfter")
            "addTab" -> ui.addComponent("select",
                    TabsContainer(Tab("First Tab", TabAdded()), Tab("Second Tab", TabAdded2()), _id = "tabAdded"))
            "removeTab" -> ui.removeByComponentId("tabAdded")
        }
        return ui
    }
}

class TabAdded: Page<EmptyDomain>(Label("Tab Content"))
class TabAdded2: Page<EmptyDomain>(Label("Tab Content 2"))

data class DynamicLayoutData(val select: String = "", val selectAddedAfter: String = "selectChoice4")

