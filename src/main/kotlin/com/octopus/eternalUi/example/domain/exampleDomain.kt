package com.octopus.eternalUi.example.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class EntityBean(@Id val internalUUID: UUID = UUID.randomUUID(), @Column val publicId: String = "", val name: String = "", val searchField: String = "")

@Repository
interface EntityRepository: JpaSpecificationExecutor<EntityBean>, JpaRepository<EntityBean, UUID>