package com.octopus.eternalUi.domain

import com.octopus.eternalUi.vaadinBridge.EternalUI
import java.io.InputStream
import java.util.*

abstract class Action(val onComponentId: String, val id: String = UUID.randomUUID().toString(), var toApply: Boolean = true)
class OnClickUIAction(_onComponentId: String, val _id: String = UUID.randomUUID().toString(), val onUIFunction: (EternalUI) -> EternalUI): Action(_onComponentId, _id)
class OnClickAction(_onComponentId: String, val _id: String = UUID.randomUUID().toString(), val onDataDomainClassFunction: (Any) -> Any): Action(_onComponentId, _id)
class OnClickReader(_onComponentId: String, val _id: String = UUID.randomUUID().toString(), val onDataDomainClassReader: (Any) -> Unit): Action(_onComponentId, _id)
class OnChangeUIAction(_onComponentId: String, val _id: String = UUID.randomUUID().toString(), val onUIFunction: (EternalUI) -> EternalUI): Action(_onComponentId, _id)
class OnChangeAction(_onComponentId: String, val _id: String = UUID.randomUUID().toString(), val onDataDomainClassFunction: (Any) -> Any): Action(_onComponentId, _id)
class OnChangeReader(_onComponentId: String, val _id: String = UUID.randomUUID().toString(), val onDataDomainClassReader: (Any) -> Unit): Action(_onComponentId, _id)
class DownloadAction(_onComponentId: String, val fileNameGenerator: (Any) -> String, val onDataDomainInputStream: (Any) -> InputStream, val _id: String = UUID.randomUUID().toString()): Action(_onComponentId, _id)
