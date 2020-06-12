package com.octopus.eternalUi.domain

import com.octopus.eternalUi.vaadinBridge.EternalUI

class ConfirmDialogPage(confirmDialog: ConfirmDialog = ConfirmDialog("noMessage", {}),
                        confirmDialogController: ConfirmDialogController = ConfirmDialogController(confirmDialog)): Page<EmptyDomain>(
        VerticalContainer(
                Label(confirmDialog.message, "h1"),
                HorizontalContainer(
                        Button("okButton", "okButton", confirmDialog.message),
                        Button("cancelButton", "cancelButton", confirmDialog.cancelMessage)
                )),
        confirmDialogController,
        PageDomain(EmptyDomain())) {
}

class ConfirmDialogController(confirmDialog: ConfirmDialog): PageController<EmptyDomain>(
        mutableListOf(OnClickReader("okButton") { confirmDialog.onOk(); EternalUI.closeConfirmDialog() },
                OnClickReader("cancelButton") { confirmDialog.onCancel(); EternalUI.closeConfirmDialog() })
)