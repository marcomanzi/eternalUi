package com.octopus.eternalUi.example.domain

import com.octopus.eternalUi.domain.db.Identifiable
import java.util.*
import javax.persistence.*

@Entity
data class User(
        @Id val id: UUID = UUID.randomUUID(),
        var name: String,
        @OneToOne(cascade = [(CascadeType.ALL)], orphanRemoval = true, fetch = FetchType.EAGER)
        val address: Address = Address()
) : AbstractJpaPersistable(), Identifiable


@Entity
data class Address(
        var street: String = "",
        var zipCode: String = "",
        var city: String = ""
) : AbstractJpaPersistable()