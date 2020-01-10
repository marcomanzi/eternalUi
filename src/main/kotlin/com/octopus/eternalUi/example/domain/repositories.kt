package com.octopus.eternalUi.example.domain

import com.octopus.eternalUi.domain.db.AbstractDataProvider
import com.octopus.eternalUi.domain.db.Message
import com.octopus.eternalUi.domain.db.Page
import org.springframework.beans.BeanUtils
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.util.*
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

@Repository
interface UserRepository: JpaSpecificationExecutor<User>, JpaRepository<User, UUID>

@Component
class UserDataProvider(private val userRepository: UserRepository): AbstractDataProvider<UserUI>() {

    private fun toUI(user: User): UserUI = UserUI().apply { BeanUtils.copyProperties(user, this) }

    override fun find(id: String?): UserUI = toUI(userRepository.findById(UUID.fromString(id!!)).get())

    override fun page(page: Page?, filters: MutableMap<String, String>?): MutableList<UserUI> {
        return userRepository.findAll(specificationFromFilters(filters), PageRequest.of(page!!.page, page.size)).content
                .map { toUI(it) }.toMutableList()
    }

    private fun specificationFromFilters(filters: MutableMap<String, String>?): (Root<User>, CriteriaQuery<*>, CriteriaBuilder) -> Predicate =
            { root, _, cb ->
                var pred = cb.and(cb.conjunction())
                filters?.get("searchName")?.let { pred = cb.and(pred, cb.like(cb.lower(root.get("name")), "%${it.toLowerCase()}%")) }
                pred
            }

    override fun count(filters: MutableMap<String, String>?): Int = userRepository.count(specificationFromFilters(filters)).toInt()
}

@Component
class CityDataProvider(): AbstractDataProvider<Message>() {
    val cities = listOf("Rome", "Milan").map { Message(it) }
    override fun count(filters: MutableMap<String, String>?): Int = cities.size
    override fun page(page: Page?, filters: MutableMap<String, String>?): MutableList<Message> = cities.toMutableList()
    override fun find(id: String?): Message = cities.first { it.message == id }
}