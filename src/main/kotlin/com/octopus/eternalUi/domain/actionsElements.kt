package com.octopus.eternalUi.domain

import com.octopus.eternalUi.vaadinBridge.EternalUI
import java.io.InputStream
import java.util.*

abstract class Action<T: Any>(val onComponentId: String, val id: String = UUID.randomUUID().toString(), var toApply: Boolean = true)
class OnClickUIAction<T: Any>(_onComponentId: String, val _id: String = UUID.randomUUID().toString(), val onUIFunction: (EternalUI<T>) -> EternalUI<T>): Action<T>(_onComponentId, _id)
class OnClickAction<T: Any>(_onComponentId: String, val _id: String = UUID.randomUUID().toString(), val onDataDomainClassFunction: (T) -> T): Action<T>(_onComponentId, _id)
class OnClickReader<T: Any>(_onComponentId: String, val _id: String = UUID.randomUUID().toString(), val onDataDomainClassReader: (T) -> Unit): Action<T>(_onComponentId, _id)
class OnChangeUIAction<T: Any>(_onComponentId: String, val _id: String = UUID.randomUUID().toString(), val onUIFunction: (EternalUI<T>) -> EternalUI<T>): Action<T>(_onComponentId, _id)
class OnChangeAction<T: Any>(_onComponentId: String, val _id: String = UUID.randomUUID().toString(), val onDataDomainClassFunction: (T) -> T): Action<T>(_onComponentId, _id)
class OnChangeReader<T: Any>(_onComponentId: String, val _id: String = UUID.randomUUID().toString(), val onDataDomainClassReader: (T) -> Unit): Action<T>(_onComponentId, _id)
class DownloadAction<T: Any>(_onComponentId: String, val fileNameGenerator: (T) -> String, val onDataDomainInputStream: (T) -> InputStream, val _id: String = UUID.randomUUID().toString()): Action<T>(_onComponentId, _id)
