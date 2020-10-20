package com.octopus.eternalUi.domain

class AndRule(vararg val rules: Rule): Rule
class OrRule(vararg val rules: Rule): Rule
class WasInteractedWith(val interactedComponentId: String): Rule
class NoRule: Rule