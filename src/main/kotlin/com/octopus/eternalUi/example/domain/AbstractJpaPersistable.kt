package com.octopus.eternalUi.example.domain

import org.springframework.data.util.ProxyUtils
import java.util.*
import javax.persistence.Id
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class AbstractJpaPersistable {

    companion object {
        private val serialVersionUID = -5554308939380869754L
    }

    @Id
    private var id: UUID = UUID.randomUUID()

    fun getId(): String {
        return id.toString()
    }

    override fun equals(other: Any?): Boolean {
        other ?: return false

        if (this === other) return true

        if (javaClass != ProxyUtils.getUserClass(other)) return false

        other as AbstractJpaPersistable

        return this.getId() == other.getId()
    }

    override fun hashCode(): Int {
        return 31
    }

    override fun toString() = "Entity of type ${this.javaClass.name} with id: $id"

}
