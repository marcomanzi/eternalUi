package com.octopus.eternalUi.domain

val debugModeActive = true

object UtilsUI {
    fun captionFromId(id: String): String = id.map { if (it.isUpperCase()) " $it" else it.toString() }.joinToString("").capitalize()
}