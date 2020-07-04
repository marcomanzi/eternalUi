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
                Grid("gridEnhancedColumns", SimpleTwoPropertiesGridBean::class, listOf("name", "surname", "thirdName"), gridConfiguration = GridConfiguration(
                        mapOf<String, UIComponent>(
                                Pair("name", Input("name", InputType.Text)),
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
    fun gridEnhancedColumnsDataProvider() = ListDataProvider(
            SimpleTwoPropertiesGridBean("Marco", "Manzi", "Superman"),
            SimpleTwoPropertiesGridBean("Francesco", "Manzi", "Spiderman"))
}

data class SimpleGridBean(val name: String): Identifiable {
    override fun getUiId(): String = name
}

data class SimpleTwoPropertiesGridBean(val name: String, val surname: String, val thirdName: String): Identifiable {
    override fun getUiId(): String = name
}

data class GridDomains(val gridSingle: SimpleGridBean? = null, val gridEnhancedColumns: SimpleTwoPropertiesGridBean? = null)