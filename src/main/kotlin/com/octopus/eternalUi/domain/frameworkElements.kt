package com.octopus.eternalUi.domain

import com.octopus.eternalUi.domain.db.Identifiable
import com.octopus.eternalUi.vaadinBridge.EternalUI
import java.util.*

interface UIDomain
interface Rule<T: Any>

abstract class UIComponent(val id: String, var cssClassName: String = "", val containedUIComponents : MutableList<UIComponent> = mutableListOf(),
                           val metadata: Map<String, Any> = mapOf()) {
    var styleApplyer: ((EternalUI<*>) -> Unit)? = null
    fun getUIComponentById(id: String) = (containedUIComponents + this).first { it.id == id }
    fun setStyle(styleApplier: (EternalUI<*>) -> Unit) {
        this.styleApplyer = styleApplier
    }
    fun getUIComponentsIds(): List<String> {
        fun getUIComponentIdsRecursive(uiComponent: UIComponent): List<String> =
                uiComponent.containedUIComponents.flatMap { getUIComponentIdsRecursive(it) } + uiComponent.id
        return getUIComponentIdsRecursive(this)
    }
}

open class PageDomain<T: Any>(val dataClass: T):UIDomain

open class UiDataProvider<T: Identifiable>(val forComponentId: String = "", val dataProvider: com.octopus.eternalUi.domain.db.DataProvider<T>,
                                           val refreshRule: Rule<out Identifiable> = NoRule(), vararg val filterIds: String,
                                           val id: String = UUID.randomUUID().toString(), var toApply:Boolean = true) {
    fun applyFilterValueToDataProvider(filterId: String, filterValue: Any?) = dataProvider.addFilter(filterId, filterValue)

    companion object {
        fun <T: Identifiable> definition(dataProvider: com.octopus.eternalUi.domain.db.DataProvider<T>, vararg filterIds: String):UiDataProvider<T> =
                UiDataProvider("", dataProvider, NoRule(), *filterIds)

        fun <T: Identifiable> definition(dataProvider: com.octopus.eternalUi.domain.db.DataProvider<T>, refreshRule: Rule<T>, vararg filterIds: String):UiDataProvider<T> =
                UiDataProvider("", dataProvider, refreshRule, *filterIds)
    }
}