package com.octopus.eternalUi.example.domain

import com.octopus.eternalUi.domain.db.Identifiable

class UserUI: User(), Identifiable {
    override fun getUiId(): String = uiId.toString()
}