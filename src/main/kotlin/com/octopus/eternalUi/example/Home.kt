package com.octopus.eternalUi.example

import com.octopus.eternalUi.domain.*
import com.octopus.eternalUi.domain.InputType.*
import com.octopus.eternalUi.example.domain.User
import com.octopus.eternalUi.example.domain.UserDataProvider
import com.octopus.eternalUi.example.domain.UserRepository
import com.octopus.eternalUi.vaadinBridge.VaadinActuator
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.annotation.UIScope
import com.vaadin.flow.theme.Theme
import com.vaadin.flow.theme.lumo.Lumo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Route("")
@Theme(Lumo::class)
@UIScope
class HomeView(@Autowired var home: Home): VaadinActuator<HomeDomain>(home)

@Component
class Home(@Autowired var homeController: HomeController): Page<HomeDomain>(
        VerticalContainer("homeContainer",
                InsideAppLink("users", UsersView::class.java),
                Label("User Search"),
                UserSearchForm(),
                Grid("usersGrid", User::class, listOf("name", "address")),
                Input("name", Text),
                HorizontalContainer("actions", Button("Save"), Button("save1000Users", caption = "Save  1000 Users"))),
        homeController,
        PageDomain(HomeDomain()))

class UserSearchForm: HorizontalContainer("searchForm", Input("searchName", Text, "Name"))

@Service
class HomeController(@Autowired var homeBackend: HomeBackend): PageController<HomeDomain>(
        actions = listOf(OnClickAction("Save") { homeBackend.saveUser(it) },
                OnClickReader("save1000Users") { it.apply { homeBackend.save1000Users() } }),
        enabledRules = listOf(EnabledRule("Save") { page -> page.hasValues("name")}),
        dataProviders = listOf(DataProvider("usersGrid", homeBackend.userDataProvider,
                OrRule(WasInteractedWith("Save"), WasInteractedWith("save1000Users")) ,
                "searchName"))
)

data class HomeDomain(val name: String = "")

@Service
class HomeBackend {
    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var userDataProvider: UserDataProvider

    fun saveUser(homeDomain: HomeDomain): HomeDomain {
        userRepository.save(User(homeDomain.name))
        return HomeDomain()
    }

    fun userListAsString(): String = userRepository.findAll().joinToString("\n") { it.toString() }

    fun save1000Users() {
        userRepository.saveAll((1 .. 1000).map { User("Test $it" ) })
    }
}
