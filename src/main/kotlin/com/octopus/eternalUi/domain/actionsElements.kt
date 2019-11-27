package com.octopus.eternalUi.domain

abstract class Action<T: Any>(val onComponentId: String)
class OnClickAction<T: Any>(_onComponentId: String, val onDataDomainClassFunction: (T) -> T): Action<T>(_onComponentId)
class OnClickReader<T: Any>(_onComponentId: String, val onDataDomainClassReader: (T) -> Unit): Action<T>(_onComponentId)
class OnChangeAction<T: Any>(_onComponentId: String, val onDataDomainClassFunction: (T) -> T): Action<T>(_onComponentId)
class OnChangeReader<T: Any>(_onComponentId: String, val onDataDomainClassReader: (T) -> Unit): Action<T>(_onComponentId)