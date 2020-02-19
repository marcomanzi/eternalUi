package com.octopus.eternalUi.example

import com.octopus.eternalUi.domain.EmptyDomain
import com.octopus.eternalUi.domain.Page
import com.octopus.eternalUi.domain.Tab
import com.octopus.eternalUi.domain.TabsContainer
import com.octopus.eternalUi.vaadinBridge.EternalUI
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.annotation.UIScope
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Route("homeUsers")
@UIScope
@JsModule("./example-style.js")
class HomeUsersTabsView(@Autowired var home: HomeUsersTabs): EternalUI<EmptyDomain>(home)

@Component
@UIScope
class HomeUsersTabs(@Autowired var home: Home, @Autowired var userPage: UserPage): Page<EmptyDomain>(TabsContainer("homeUsersTabs", Tab("home", home), Tab("users", userPage)))