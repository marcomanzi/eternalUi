package com.octopus.eternalUi.example

import com.octopus.eternalUi.domain.*
import com.octopus.eternalUi.domain.db.Identifiable
import com.octopus.eternalUi.domain.db.ListDataProvider
import com.octopus.eternalUi.domain.db.Message
import com.octopus.eternalUi.vaadinBridge.EternalUI
import com.vaadin.flow.spring.annotation.UIScope
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

@Component @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class SimpleListGrid: Page<GridDomains>(
        VerticalContainer(
                Label("Single Select Grid"),
                Grid("gridSingle", SimpleGridBean::class, listOf("name"), gridConfiguration = GridConfiguration().apply {
                    rowsToShow = 2
                }),
                Label(""),
                Label("Multi Select Grid"),
                Grid("gridMulti", SimpleGridBean::class, listOf("name"), gridConfiguration = GridConfiguration().apply {
                    gridSelectionType = GridSelectionType.MULTI
                    rowsToShow = 2
                }),
                Label(""),
                Label("No Select Grid"),
                Grid("gridNone", SimpleGridBean::class, listOf("name"), gridConfiguration = GridConfiguration().apply {
                    gridSelectionType = GridSelectionType.NONE
                    rowsToShow = 2
                }),
                Label(""),
                Label("Grid with enhanced columns"),
                HorizontalContainer(Button("addColumn"), Button("removeColumn")),
                Grid("gridEnhancedColumns", SimpleThreePropertiesGridBean::class, listOf("name", "middleName", "surname"), gridConfiguration = GridConfiguration(
                        mapOf<String, UIComponent>(
                                Pair("surname", Input("surname", InputType.Text)))
                ).apply {
                    rowsToShow = 2
                    gridSelectionType = GridSelectionType.MULTI
                }),
                Label(""),
                Label("Grid with a maps as backend"),
                Grid("gridWithMapAsBackend", Map::class, listOf("name", "middleName", "surname"), gridConfiguration = GridConfiguration(
                        mapOf<String, UIComponent>(
                                Pair("surname", Input("surname", InputType.Text)))
                ).apply {
                    rowsToShow = 2
                })
        ),
        pageDomain = PageDomain(GridDomains())
) {

    fun gridSingleDataProvider() = ListDataProvider(SimpleGridBean("Marco"), SimpleGridBean("Francesco"))
    fun gridMultiDataProvider() = ListDataProvider(SimpleGridBean("Marco"), SimpleGridBean("Francesco"))
    fun gridNoneDataProvider() = ListDataProvider(SimpleGridBean("Marco"), SimpleGridBean("Francesco"))
    private val enhancedColumnsDataProvider = ListDataProvider(
            SimpleThreePropertiesGridBean("Marco", "Manzi", "midName"),
            SimpleThreePropertiesGridBean("Francesco", "Manzi", "midName"))

    private val gridWithMapAsBackendDataProvider = ListDataProvider(
            mutableMapOf(Pair("name", "Marco"), Pair("surname", "Manzi"), Pair("middleName", "midName")),
            mutableMapOf(Pair("name", "Francesco"), Pair("surname", "Manzi"), Pair("middleName", "midName")))


    fun gridEnhancedColumnsDataProvider() = enhancedColumnsDataProvider
    fun gridWithMapAsBackendDataProvider() = gridWithMapAsBackendDataProvider
    fun addColumnClicked(ui: EternalUI<GridDomains>): EternalUI<GridDomains> = ui.apply {
        enhancedColumnsDataProvider.addElement(SimpleThreePropertiesGridBean("", "", ""))
        ui.refresh("gridEnhancedColumns")
    }
    fun removeColumnClicked(ui: EternalUI<GridDomains>): EternalUI<GridDomains> = ui.apply {
        page.pageDomain.dataClass.gridEnhancedColumns?.let { list ->
            list.forEach { enhancedColumnsDataProvider.removeElement(it) }
        }
        ui.refresh("gridEnhancedColumns")
    }
}
data class SimpleGridBean(val name: String): Identifiable {
    override fun getUiId(): String = name
}

data class SimpleTwoPropertiesGridBean(val name: String, val surname: String): Identifiable {
    override fun getUiId(): String = name
}

data class SimpleThreePropertiesGridBean(val name: String, val surname: String, val middleName: String, val uuid: String = UUID.randomUUID().toString()): Identifiable {
    override fun getUiId(): String = uuid
}

data class GridDomains(val gridSingle: SimpleGridBean? = null,
                       val gridMulti: Set<SimpleGridBean>? = null,
                       val gridEnhancedColumns: Set<SimpleThreePropertiesGridBean>? = null,
                       val gridWithMapAsBackend: MutableMap<String, Any?>? = null,
                       val metadata: MutableMap<String, Any?>? = null)