package com.octopus.eternalUi.example

import com.octopus.eternalUi.domain.*
import com.octopus.eternalUi.vaadinBridge.VaadinActuator
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.annotation.UIScope
import com.vaadin.flow.theme.Theme
import com.vaadin.flow.theme.lumo.Lumo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Route("users")
@Theme(Lumo::class)
@UIScope
@JsModule("./example-style.js")
class UsersView(@Autowired var home: UserPage): VaadinActuator<EmptyDomain>(home)

@Component
class UserPage(@Autowired var userController: UserController): Page<EmptyDomain>(
        VerticalContainer("userContainer",
                Label("Welcome to the User Page", "h1"),
                InsideAppLink("home", HomeView::class.java)),
        userController)


@Service
class UserController : PageController<EmptyDomain>()