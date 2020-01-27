package com.octopus.eternalUi.domain

open class EnabledRule<T: Any>(val onComponentId: String, val condition: (Page<T>) -> Boolean): Rule<T>
class AllHasValueEnableRule<T: Any>(onComponentId: String, vararg ids: String): EnabledRule<T>(onComponentId, { page -> page.anyValue(*ids) })
class AnyHasValueEnableRule<T: Any>(onComponentId: String, vararg ids: String): EnabledRule<T>(onComponentId, { page -> page.anyValue(*ids) })