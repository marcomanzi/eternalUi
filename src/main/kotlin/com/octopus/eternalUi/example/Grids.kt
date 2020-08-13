package com.octopus.eternalUi.example

import com.octopus.eternalUi.domain.*
import com.octopus.eternalUi.domain.db.DataProvider
import com.octopus.eternalUi.domain.db.Identifiable
import com.octopus.eternalUi.domain.db.ListDataProvider
import com.octopus.eternalUi.example.SimpleListGrid.MessageEnhanced
import com.octopus.eternalUi.vaadinBridge.EternalUI
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
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
                Grid("gridEnhancedColumns", WithDataProviderThreePropertiesGridBean::class, listOf("name", "middleName", "surname"), gridConfiguration = GridConfiguration(
                        mapOf<String, UIComponent>(
                                Pair("surname", Input("surname", InputType.Text)),
                                Pair("middleName", Input("middleName", InputType.Select)))
                ).apply {
                    rowsToShow = 2
                    gridSelectionType = GridSelectionType.MULTI
                }),
                Label(""),
                Label("Grid with a maps as backend"),
                HorizontalContainer(Button("addColumnToMapGrid")),
                Grid("gridWithMapAsBackend", Map::class, listOf("middleName", "surname"), gridConfiguration = GridConfiguration(
                        mapOf<String, UIComponent>(
                                Pair("surname", Input("surname", InputType.Text)),
                                Pair("middleName", Input("middleName", InputType.Select)))
                ).apply {
                    rowsToShow = 2
                })
        ),
        pageDomain = PageDomain(GridDomains())
) {

    fun gridSingleDataProvider() = ListDataProvider(SimpleGridBean("Marco"), SimpleGridBean("Francesco"))
    fun gridMultiDataProvider() = ListDataProvider(SimpleGridBean("Marco"), SimpleGridBean("Francesco"))
    fun gridNoneDataProvider() = ListDataProvider(SimpleGridBean("Marco"), SimpleGridBean("Francesco"))
    data class MessageEnhanced(val message: String, val id: String = UUID.randomUUID().toString()): Identifiable {
        override fun getUiId(): kotlin.String {
            return id
        }

        override fun toString(): String {
            return message
        }
    }
    fun middleNameDataProvider(): ListDataProvider<out Identifiable> = ListDataProvider(MessageEnhanced("Middle1"), MessageEnhanced("Middle2"))
    private val enhancedColumnsDataProvider = ListDataProvider(
            WithDataProviderThreePropertiesGridBean("Marco", "Manzi", "Middle1Bean"),
            WithDataProviderThreePropertiesGridBean("Francesco", "Manzi", "Middle2Bean"))

    private val gridWithMapAsBackendDataProvider = ListDataProvider(
            mutableMapOf(Pair("name", "Marco"), Pair("surname", "Manzi"), Pair("middleName", "Middle1"), Pair("middleNameDataProvider", middleNameDataProvider())),
            mutableMapOf(Pair("name", "Francesco"), Pair("surname", "Manzi"), Pair("middleName", "Middle1"), Pair("middleNameDataProvider", middleNameDataProvider())))


    fun gridEnhancedColumnsDataProvider() = enhancedColumnsDataProvider
    fun gridWithMapAsBackendDataProvider() = gridWithMapAsBackendDataProvider
    fun addColumnClicked(ui: EternalUI<GridDomains>): EternalUI<GridDomains> = ui.apply {
        enhancedColumnsDataProvider.addElement(WithDataProviderThreePropertiesGridBean("", "", ""))
        ui.refresh("gridEnhancedColumns")
    }
    fun removeColumnClicked(ui: EternalUI<GridDomains>): EternalUI<GridDomains> = ui.apply {
        page.pageDomain.dataClass.gridEnhancedColumns?.let { list ->
            list.forEach { enhancedColumnsDataProvider.removeElement(it) }
        }
        page.pageDomain = PageDomain(page.pageDomain.dataClass.copy(gridEnhancedColumns = null))
        ui.refresh("gridEnhancedColumns")
    }
    fun addColumnToMapGridClicked(ui: EternalUI<GridDomains>): EternalUI<GridDomains> = ui.apply {
        gridWithMapAsBackendDataProvider.addElement(mutableMapOf(Pair("name", ""), Pair("surname", ""), Pair("middleName", ""), Pair("middleNameDataProvider", middleNameDataProvider())))
        ui.refresh("gridWithMapAsBackend")
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

data class WithDataProviderThreePropertiesGridBean(val name: String, val surname: String, val middleName: MessageEnhanced,
                                                   val middleNameDataProvider: DataProvider<out Identifiable> = ListDataProvider(MessageEnhanced("Middle1Bean"), MessageEnhanced("Middle2Bean")),
                                                   val uuid: String = UUID.randomUUID().toString()): Identifiable {
    override fun getUiId(): String = uuid

    constructor(name: String, surname: String, middleName: String): this(name, surname, MessageEnhanced(middleName), uuid = UUID.randomUUID().toString())
}

data class GridDomains(val gridSingle: SimpleGridBean? = null,
                       val gridMulti: Set<SimpleGridBean>? = null,
                       val gridEnhancedColumns: MutableSet<WithDataProviderThreePropertiesGridBean>? = null,
                       val gridWithMapAsBackend: MutableMap<String, Any?>? = null,
                       val metadata: MutableMap<String, Any?>? = null)