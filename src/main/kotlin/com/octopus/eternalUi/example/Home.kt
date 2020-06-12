package com.octopus.eternalUi.example

import com.octopus.eternalUi.domain.*
import com.octopus.eternalUi.vaadinBridge.EternalUI
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.annotation.UIScope
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Route("")
@UIScope
@JsModule("./example-style.js")
class HomeView(@Autowired var home: Home): EternalUI<EmptyDomain>(home)

@Component
@UIScope
class Home(@Autowired var simpleInputs: SimpleInputs, @Autowired val simpleForm: SimpleForm): Page<EmptyDomain>(
        VerticalContainer(
                Label("Eternal UI", "h1"),
                HorizontalContainer( Label("Examples", "h2"), Button("activateDebugButton", caption = (if (debugModeActive) "Deactivate" else "Activate") + " Debug Button")),
                TabsContainer(
                        Tab("Simple inputs", simpleInputs),
                        Tab("Simple Form", simpleForm)
                )
        )) {

    fun activateDebugButtonClicked(ui: EternalUI<EmptyDomain>): EternalUI<EmptyDomain> = ui.apply {
        debugModeActive = !debugModeActive
        EternalUI.reloadPage()
    }
}