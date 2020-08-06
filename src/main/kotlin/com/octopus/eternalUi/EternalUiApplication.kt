package com.octopus.eternalUi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
open class EternalUiApplication

fun main(args: Array<String>) {
    runApplication<EternalUiApplication>(*args)
}