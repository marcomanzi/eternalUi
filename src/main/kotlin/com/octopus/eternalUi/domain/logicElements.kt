package com.octopus.eternalUi.domain

class AndRule<T: Any>(vararg val rules: Rule<T>): Rule<T>
class OrRule<T: Any>(vararg val rules: Rule<T>): Rule<T>
class WasInteractedWith<T: Any>(val interactedComponentId: String): Rule<T>
class NoRule<T: Any>: Rule<T>