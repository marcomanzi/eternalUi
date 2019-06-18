package com.octopus.eternalUi.example.domain

import com.octopus.eternalUi.domain.db.Identifiable
import java.util.*
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.OneToOne

@Entity
data class User(val name: String,
                @OneToOne(cascade = [(CascadeType.ALL)], orphanRemoval = true, fetch = FetchType.EAGER)
val address: Address = Address()
) : AbstractJpaPersistable(), Identifiable


@Entity
data class Address(
        val street: String = "",
        val zipCode: String = "",
        val city: String = ""
) : AbstractJpaPersistable()