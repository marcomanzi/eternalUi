package com.octopus.eternalUi.example.domain

import com.octopus.eternalUi.domain.db.AbstractDataProvider
import com.octopus.eternalUi.domain.db.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root

@Repository
interface UserRepository: JpaSpecificationExecutor<User>, JpaRepository<User, String>

@Component
class UserDataProvider(val userRepository: UserRepository): AbstractDataProvider<User>() {

    override fun find(id: String?): User = userRepository.findById(id!!).get()

    override fun page(page: Page?, filters: MutableMap<String, String>?): MutableList<User> {
        return userRepository.findAll(specificationFromFilters(filters), PageRequest.of(page!!.page, page.size)).content
    }

    private fun specificationFromFilters(filters: MutableMap<String, String>?): (Root<User>, CriteriaQuery<*>, CriteriaBuilder) -> Predicate =
            { root, _, cb ->
                var pred = cb.and(cb.conjunction())
                filters?.get("searchName")?.let { pred = cb.and(pred, cb.like(cb.lower(root.get("name")), "%${it.toLowerCase()}%")) }
                pred
            }

    override fun count(filters: MutableMap<String, String>?): Int = userRepository.count(specificationFromFilters(filters)).toInt()
}