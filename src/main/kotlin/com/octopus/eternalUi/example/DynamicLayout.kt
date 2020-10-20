package com.octopus.eternalUi.example

import com.octopus.eternalUi.domain.*
import com.octopus.eternalUi.domain.db.ListDataProvider
import com.octopus.eternalUi.vaadinBridge.EternalUI
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class DynamicLayout: Page(
        VerticalContainer(Input("select", InputType.Select)),
        pageDomain = DynamicLayoutData()
) {
    fun selectDataProvider() = ListDataProvider("", "addSelect", "removeSelect", "addTab", "removeTab")
    fun selectAddedAfterDataProvider() = ListDataProvider("selectChoice3", "selectChoice4")
    fun selectChanged(ui: EternalUI): EternalUI {
        when ((ui.page.pageDomain as DynamicLayoutData).select) {
            "addSelect" -> ui.addComponent("select", Input("selectAddedAfter", InputType.Select))
            "removeSelect" -> ui.removeByComponentId("selectAddedAfter")
            "addTab" -> ui.addComponent("select",
                    TabsContainer("tabAdded",
                            Tab("First Tab", object: Page(Label("Tab Content")) {}),
                            Tab("Second Tab", object: Page(Label("Tab Content 2")) {})))
            "removeTab" -> ui.removeByComponentId("tabAdded")
        }
        return ui
    }
}

data class DynamicLayoutData(val select: String = "", val selectAddedAfter: String = "selectChoice4")

