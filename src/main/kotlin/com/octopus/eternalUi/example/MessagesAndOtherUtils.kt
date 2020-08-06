package com.octopus.eternalUi.example

import com.octopus.eternalUi.domain.*
import com.octopus.eternalUi.vaadinBridge.EternalUI
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class MessagesAndOtherUtils: Page<EmptyDomain>(
        VerticalContainer(Button("showModalWindow"), Button("showConfirmDialog")),
        pageDomain = PageDomain(EmptyDomain())
) {
    fun showModalWindowClicked(ui: EternalUI<EmptyDomain>):EternalUI<EmptyDomain> = ui.apply {
        EternalUI.showInUI(ModalWindow(SimplePage(), onClose = {
            EternalUI.showInUI(UserMessage("Closed called"))
        }))
    }

    fun showConfirmDialogClicked(ui: EternalUI<EmptyDomain>):EternalUI<EmptyDomain> = ui.apply {
        EternalUI.showInUI(ConfirmDialog("Confirm Me", onOk = {
            EternalUI.showInUI(UserMessage("Ok called"))
        }, onCancel = {
            EternalUI.showInUI(UserMessage("Cancel called"))
        }))
    }
}

class SimplePage: Page<EmptyDomain>(
        VerticalContainer(Label("Simple Page"), Button("closeTopModalWindow"))) {
    fun closeTopModalWindowClicked(ui: EternalUI<EmptyDomain>):EternalUI<EmptyDomain> = ui.apply {
        EternalUI.closeTopModalWindow()
    }
}