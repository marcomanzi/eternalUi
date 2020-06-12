package com.octopus.eternalUi.domain

import java.util.*

var debugModeActive: Boolean = Optional.ofNullable(System.getenv("UI-DEBUG")?.toBoolean()).orElseGet { false }

object UtilsUI {
    fun captionFromId(id: String): String = id.map { if (it.isUpperCase()) " $it" else it.toString() }.joinToString("").capitalize()
}
