package com.octopus.eternalUi.domain

import com.octopus.eternalUi.vaadinBridge.EternalUI
import java.util.*

interface Rule

abstract class UIComponent(val id: String, var cssClassName: String = "", val containedUIComponents : MutableList<UIComponent> = mutableListOf(),
                           val metadata: Map<String, Any> = mapOf()) {
    var styleApplyer: ((EternalUI) -> Unit)? = null
    fun getUIComponentById(id: String) = (containedUIComponents + this).first { it.id == id }
    fun setStyle(styleApplier: (EternalUI) -> Unit) {
        this.styleApplyer = styleApplier
    }
    fun getUIComponentsIds(): List<String> {
        fun getUIComponentIdsRecursive(uiComponent: UIComponent): List<String> =
                uiComponent.containedUIComponents.flatMap { getUIComponentIdsRecursive(it) } + uiComponent.id
        return getUIComponentIdsRecursive(this)
    }
}

open class UiDataProvider(val forComponentId: String = "", val dataProvider: com.octopus.eternalUi.domain.db.DataProvider,
                                           val refreshRule: Rule = NoRule(), vararg val filterIds: String,
                                           val id: String = UUID.randomUUID().toString(), var toApply:Boolean = true) {
    fun applyFilterValueToDataProvider(filterId: String, filterValue: Any?) = dataProvider.addFilter(filterId, filterValue)

    companion object {
        fun definition(dataProvider: com.octopus.eternalUi.domain.db.DataProvider, vararg filterIds: String):UiDataProvider =
                UiDataProvider("", dataProvider, NoRule(), *filterIds)

        fun definition(dataProvider: com.octopus.eternalUi.domain.db.DataProvider, refreshRule: Rule, vararg filterIds: String):UiDataProvider =
                UiDataProvider("", dataProvider, refreshRule, *filterIds)
    }
}