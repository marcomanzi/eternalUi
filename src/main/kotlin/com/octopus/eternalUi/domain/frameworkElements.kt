package com.octopus.eternalUi.domain

import com.octopus.eternalUi.domain.db.Identifiable
import com.octopus.eternalUi.vaadinBridge.EternalUI

interface UIDomain
interface UIBackend
interface UIPresenter
interface Rule<T: Any>

abstract class UIComponent(val id: String, var cssClassName: String, val containedUIComponents : List<UIComponent> = listOf(), val metadata: Map<String, Any> = mapOf()) {
    var styleApplyer: ((EternalUI<*>) -> Unit)? = null
    fun getUIComponentById(id: String) = (containedUIComponents + this).first { it.id == id }
    fun setStyle(styleApplier: (EternalUI<*>) -> Unit) {
        this.styleApplyer = styleApplier
    }
}

open class PageController<T: Any>(val actions: MutableList<Action<T>> = mutableListOf(),
                                  val enabledRules: MutableList<Rule<T>> = mutableListOf(),
                                  val dataProviders: MutableList<DataProvider<out Identifiable>> = mutableListOf()): UIPresenter

open class PageDomain<T: Any>(val dataClass: T):UIDomain

open class PageBackend<T: Any>: UIBackend

open class DataProvider<T: Identifiable>(val forComponentId: String, val dataProvider: com.octopus.eternalUi.domain.db.DataProvider<T>,
                                         val refreshRule: Rule<T> = NoRule(), vararg val filterIds: String) {
    fun applyFilterValueToDataProvider(filterId: String, filterValue: Any) = dataProvider.addFilter(filterId, filterValue)
}