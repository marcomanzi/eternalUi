package com.octopus.eternalUi.domain

import com.octopus.eternalUi.vaadinBridge.EternalUI
import java.io.InputStream

abstract class Action<T: Any>(val onComponentId: String)
class OnClickUIAction<T: Any>(_onComponentId: String, val onUIFunction: (EternalUI<T>) -> EternalUI<T>): Action<T>(_onComponentId)
class OnClickAction<T: Any>(_onComponentId: String, val onDataDomainClassFunction: (T) -> T): Action<T>(_onComponentId)
class OnClickReader<T: Any>(_onComponentId: String, val onDataDomainClassReader: (T) -> Unit): Action<T>(_onComponentId)
class OnChangeAction<T: Any>(_onComponentId: String, val onDataDomainClassFunction: (T) -> T): Action<T>(_onComponentId)
class OnChangeReader<T: Any>(_onComponentId: String, val onDataDomainClassReader: (T) -> Unit): Action<T>(_onComponentId)
class DownloadAction<T: Any>(_onComponentId: String, val fileNameGenerator: (T) -> String, val onDataDomainInputStream: (T) -> InputStream): Action<T>(_onComponentId)
