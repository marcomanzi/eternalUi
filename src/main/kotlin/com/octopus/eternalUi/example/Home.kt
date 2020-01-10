package com.octopus.eternalUi.example

import com.octopus.eternalUi.domain.*
import com.octopus.eternalUi.domain.InputType.Select
import com.octopus.eternalUi.domain.InputType.Text
import com.octopus.eternalUi.example.domain.*
import com.octopus.eternalUi.example.user.UserDomain
import com.octopus.eternalUi.example.user.UserForm
import com.octopus.eternalUi.vaadinBridge.EternalUI
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.annotation.UIScope
import com.vaadin.flow.theme.Theme
import com.vaadin.flow.theme.lumo.Lumo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Route("")
@UIScope
//class TestView: Div(), BeforeEnterObserver {
//    override fun beforeEnter(event: BeforeEnterEvent?) {
//        Dialog().apply {
//            add(ComboBox<String>("Test").apply { setItems("1", "2", "3") })
//            add(com.vaadin.flow.component.select.Select<String>("Test").apply { setItems("1", "2", "3") })
//        }.open()
//    }
//
//}
@JsModule("./example-style.js")
class HomeView(@Autowired var home: Home): EternalUI<HomeDomain>(home)

@Component
@UIScope
class Home(@Autowired var homeController: HomeController): Page<HomeDomain>(
        VerticalContainer("homeContainer",
                InsideAppLink("users", UsersView::class.java),
                Label("User Search", "h1"),
                UserSearchForm(),
                Grid("usersGrid", UserUI::class, listOf("name", "address")),
                HorizontalContainer("userFormLine1", Input("name", Text), Input("surname", Text), Input("surname2", Text), Input("age", Text)),
                HorizontalContainer("userFormLine2", Input("vatNumber", Text), Input("city", Select), Input("country", Text)),
                HorizontalContainer("actions", Button("Save"), Button("save1000Users", caption = "Save 1000 Users"),
                        Button("downloadFile", caption = "Download File"))),
        homeController,
        PageDomain(HomeDomain()))

class UserSearchForm: HorizontalContainer("searchForm", Input("searchName", Text, "Name"))

@Service
class HomeController(@Autowired var homeBackend: HomeBackend): PageController<HomeDomain>(
        actions = listOf(OnClickAction("Save") { homeBackend.saveUser(it) },
                OnClickReader("save1000Users") { it.apply { homeBackend.save1000Users() }},
                OnClickAction("usersGrid") { homeBackend.showUserForm(it)},
                OnClickReader("downloadFile") { homeBackend.openFile(it)}),
        enabledRules = listOf(EnabledRule("Save") { page -> page.hasValues("name")}),
        dataProviders = listOf(DataProvider("usersGrid", homeBackend.userDataProvider,
                OrRule(WasInteractedWith("Save"), WasInteractedWith("save1000Users")),"searchName"),
                DataProvider("city", homeBackend.cityDataProvider))
)

data class HomeDomain(val name: String = "", val surname: String = "", val country: String = "", val usersGrid: User? = null, val city: String = "")

@Service
class HomeBackend {
    @Autowired lateinit var userRepository: UserRepository
    @Autowired lateinit var userDataProvider: UserDataProvider
    @Autowired lateinit var cityDataProvider: CityDataProvider
    @Autowired lateinit var userForm: UserForm

    fun saveUser(homeDomain: HomeDomain): HomeDomain {
        userRepository.save(User(name = homeDomain.name))
        return HomeDomain(city = "Rome")
    }

    fun save1000Users() {
        userRepository.saveAll((1 .. 1000).map { User(name = "Test $it" ) })
    }

    fun showUserForm(homeDomain: HomeDomain): HomeDomain {
        homeDomain.usersGrid?.let {
            EternalUI.showInUI(ModalWindow("modalUserForm", userForm.withUser(UserDomain(id = it.id, name = it.name))))
            EternalUI.showInUI(UserMessage(it.name))
            return HomeDomain(it.name, "Pippo", "EU", it)
        }
        return homeDomain
    }

    fun openFile(homeDomain: HomeDomain) =
            UI.getCurrent().page.executeJs("window.open('file:///Users/mmanzi/workspace/soldo/hybrid-projects/eternalUi/HELP.md', '_blank', '');");

}