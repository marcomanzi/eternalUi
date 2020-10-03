package com.octopus.eternalUi.domain

import com.octopus.eternalUi.vaadinBridge.EternalUI

class ConfirmDialogPage(val confirmDialog: ConfirmDialog = ConfirmDialog("noMessage", {})): Page<EmptyDomain>(
        VerticalContainer(
                Label(confirmDialog.message, "h1"),
                HorizontalContainer(
                        Button("okButton", "okButton", confirmDialog.message),
                        Button("cancelButton", "cancelButton", confirmDialog.cancelMessage)
                )),
        pageDomain = PageDomain(EmptyDomain())) {
    fun okButtonClicked(domain: EmptyDomain): Unit {
        confirmDialog.onOk()
        EternalUI.closeConfirmDialog()
    }

    fun cancelButtonClicked(domain: EmptyDomain): Unit {
        confirmDialog.onCancel()
        EternalUI.closeConfirmDialog()
    }
}