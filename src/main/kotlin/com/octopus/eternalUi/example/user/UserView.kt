package com.octopus.eternalUi.example.user

import com.octopus.eternalUi.domain.*
import com.octopus.eternalUi.example.domain.UserRepository
import com.octopus.eternalUi.vaadinBridge.EternalUI
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.util.*

@Scope("prototype")
@Component
class UserView(@Autowired val userForm: UserForm): EternalUI<UserDomain>(userForm)

@Component
class UserForm(@Autowired val userFormController: UserFormController): Page<UserDomain>(
        VerticalContainer("userFormContainer", Label("user"), Input("name"), Button("Save")),
        userFormController,
        PageDomain(UserDomain())) {
    fun withUser(userDomain: UserDomain): UserForm = apply { pageDomain = PageDomain(userDomain) }
}

@Service
class UserFormController(@Autowired val userFormBackend: UserFormBackend): PageController<UserDomain> (
        actions = mutableListOf(OnClickAction("Save", { userFormBackend.save(it) }))
)

data class UserDomain(val id: UUID = UUID.randomUUID(), val name: String = "")

@Service
class UserFormBackend(@Autowired val userRepository: UserRepository) {
    fun save(ud: UserDomain): UserDomain = ud.apply {
        userRepository.findById(ud.id).ifPresent {
            userRepository.save(it.apply {
                this.name = ud.name
            })
        }
    }
}