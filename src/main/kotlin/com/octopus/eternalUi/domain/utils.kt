package com.octopus.eternalUi.domain

val debugModeActive = false

object UtilsUI {
    fun captionFromId(id: String): String = id.map { if (it.isUpperCase()) " $it" else it }.joinToString("").capitalize()
}