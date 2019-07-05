package com.octopus.eternalUi.domain

import com.octopus.eternalUi.domain.db.Identifiable
import com.octopus.eternalUi.vaadinBridge.VaadinActuator
import com.vaadin.flow.component.Component

interface UIDomain
interface UIPresenter
interface Rule<T: Any>
abstract class Action<T: Any>(val onComponentId: String)

class OnClickAction<T: Any>(_onComponentId: String, val onDataDomainClassFunction: (T) -> T): Action<T>(_onComponentId)
class OnClickReader<T: Any>(_onComponentId: String, val onDataDomainClassReader: (T) -> Unit): Action<T>(_onComponentId)
class OnChangeAction<T: Any>(_onComponentId: String, val onDataDomainClassFunction: (T) -> T): Action<T>(_onComponentId)
class OnChangeReader<T: Any>(_onComponentId: String, val onDataDomainClassReader: (T) -> Unit): Action<T>(_onComponentId)

class AndRule<T: Any>(vararg val rules: Rule<T>): Rule<T>
class OrRule<T: Any>(vararg val rules: Rule<T>): Rule<T>
class EnabledRule<T: Any>(val onComponentId: String, val condition: (Page<T>) -> Boolean): Rule<T>
class WasInteractedWith<T: Any>(val interactedComponentId: String): Rule<T>
class NoRule<T: Any>: Rule<T>

abstract class UIComponent(val id: String, val cssClassName: String, val containedUIComponents : List<UIComponent> = listOf(), val metadata: Map<String, out Any> = mapOf()) {
    var styleApplyer: ((VaadinActuator<*>) -> Unit)? = null
    fun getUIComponentById(id: String) = (containedUIComponents + this).first { it.id == id }
    fun setStyle(styleApplier: (VaadinActuator<*>) -> Unit) {
        this.styleApplyer = styleApplier
    }
}

open class PageController<T: Any>(val actions: List<Action<T>> = listOf(),
                                  val enabledRules: List<Rule<T>> = listOf(),
                                  val dataProviders: List<DataProvider<out Identifiable>> = listOf()): UIPresenter

open class PageDomain<T: Any>(val dataClass: T):UIDomain

open class DataProvider<T: Identifiable>(val forComponentId: String, val dataProvider: com.octopus.eternalUi.domain.db.DataProvider<T>, val refreshRule: Rule<T> = NoRule(), vararg val filterIds: String) {
    fun applyFilterValueToDataProvider(filtedId: String, filterValue: String) {
        dataProvider.addFilter(filtedId, filterValue)
    }
}