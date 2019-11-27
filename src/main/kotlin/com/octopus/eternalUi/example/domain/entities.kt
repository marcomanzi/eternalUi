package com.octopus.eternalUi.example.domain

import java.util.*
import javax.persistence.*

@Entity
open class User(
        @Id val id: UUID = UUID.randomUUID(),
        var name: String = "",
        @OneToOne(cascade = [(CascadeType.ALL)], orphanRemoval = true, fetch = FetchType.EAGER)
        val address: Address = Address()
) : AbstractJpaPersistable()

@Entity
class Address(
        var street: String = "",
        var zipCode: String = "",
        var city: String = ""
) : AbstractJpaPersistable()