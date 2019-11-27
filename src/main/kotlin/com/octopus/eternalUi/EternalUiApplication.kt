package com.octopus.eternalUi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class EternalUiApplication

fun main(args: Array<String>) {
    runApplication<EternalUiApplication>(*args)
}