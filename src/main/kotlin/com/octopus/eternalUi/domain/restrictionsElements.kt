package com.octopus.eternalUi.domain

class EnabledRule<T: Any>(val onComponentId: String, val condition: (Page<T>) -> Boolean): Rule<T>