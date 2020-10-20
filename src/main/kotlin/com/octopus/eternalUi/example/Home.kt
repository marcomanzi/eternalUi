package com.octopus.eternalUi.example

import com.octopus.eternalUi.domain.*
import com.octopus.eternalUi.vaadinBridge.EternalUI
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.router.PreserveOnRefresh
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.annotation.UIScope
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Route("")
@UIScope
@JsModule("./example-style.js")
@PreserveOnRefresh
@Push
class HomeView(@Autowired var home: Home): EternalUI(home)

@Component
@UIScope
class Home(@Autowired var simpleInputs: SimpleInputs, @Autowired val simpleForm: SimpleForm,
           @Autowired val simpleListGrid: SimpleListGrid, @Autowired val filteredGrid: SimpleFilteredGrid,
           @Autowired val dynamicLayout: DynamicLayout, @Autowired val messagesAndOtherUtils: MessagesAndOtherUtils): Page(
        VerticalContainer(
                Label("Eternal UI", "h1"),
                HorizontalContainer( Label("Examples", "h2"), Button("activateDebugButton")),
                TabsContainer(
                        Tab("Simple inputs", simpleInputs),
                        Tab("Simple Form", simpleForm),
                        Tab("List Grid", simpleListGrid),
                        Tab("Filtered Grid", filteredGrid),
                        Tab("Dynamic Layout", dynamicLayout),
                        Tab("Other Utils", messagesAndOtherUtils)
                )
        ), beforeEnter = { it.page.pageDomain.apply {
    it.setCaptionTo("activateDebugButton", if (debugModeActive) "Deactivate Debug Button" else "Activate Debug Button")
}}) {
    fun activateDebugButtonClicked(ui: EternalUI): EternalUI = ui.apply {
        debugModeActive = !debugModeActive
        EternalUI.reloadPage()
    }
}