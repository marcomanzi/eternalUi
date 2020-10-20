package com.octopus.eternalUi.example

import com.octopus.eternalUi.domain.*
import com.octopus.eternalUi.domain.db.AbstractDataProvider

import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class SimpleFilteredGrid: Page(
        VerticalContainer(
                Label("Single Filtered Grid"),
                HorizontalContainer(Input("name"), Input("birth", InputType.Date)),
                Grid("gridFilteredSingle", SimpleHumanBean::class, listOf("name", "birth"), gridConfiguration = GridConfiguration().apply {
                    rowsToShow = 2
                })
        ),
        pageDomain = GridDomains()
) {

    fun gridFilteredSingleDataProvider() = UiDataProvider("gridFilteredSingle", object: AbstractDataProvider() {
        val elements = listOf(SimpleHumanBean("Marco", LocalDate.of(1983, 10, 17)), SimpleHumanBean("Francesco", LocalDate.of(2015, 11, 12)))
        override fun count(filters: MutableMap<String, Any>?): Int = elements.filter { elementsFilter(it, filters) }.count()

        override fun page(page: com.octopus.eternalUi.domain.db.Page?, filters: MutableMap<String, Any>?): MutableList<SimpleHumanBean> =
                elements.filter { elementsFilter(it, filters) }.toMutableList()

        override fun find(id: Any?): Any = id!!

    }, filterIds = *arrayOf("name", "birth"))

    private fun elementsFilter(sh: SimpleHumanBean, filters: MutableMap<String, Any>?) =
            sh.name.contains(filters?.get("name")?.toString() ?: "") &&
                    filters?.get("birth")?.let { return@let sh.birth == it as LocalDate }?: true

}

data class SimpleHumanBean(val name: String, val birth: LocalDate)
