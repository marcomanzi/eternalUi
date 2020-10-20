package com.octopus.eternalUi.example

import com.octopus.eternalUi.domain.*
import com.octopus.eternalUi.vaadinBridge.EternalUI
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class MessagesAndOtherUtils: Page(
        VerticalContainer(Button("showModalWindow"), Button("showConfirmDialog")),
        pageDomain = EmptyDomain()
) {
    fun showModalWindowClicked(ui: EternalUI):EternalUI = ui.apply {
        EternalUI.showInUI(ModalWindow(SimplePage(), onClose = {
            EternalUI.showInUI(UserMessage("Closed called"))
        }))
    }

    fun showConfirmDialogClicked(ui: EternalUI):EternalUI = ui.apply {
        EternalUI.showInUI(ConfirmDialog("Confirm Me", onOk = {
            EternalUI.showInUI(UserMessage("Ok called"))
        }, onCancel = {
            EternalUI.showInUI(UserMessage("Cancel called"))
        }))
    }
}

class SimplePage: Page(
        VerticalContainer(Label("Simple Page"), Button("closeTopModalWindow"))) {
    fun closeTopModalWindowClicked(ui: EternalUI):EternalUI = ui.apply {
        EternalUI.closeTopModalWindow()
    }
}