package com.octopus.eternalUi.domain

import com.vaadin.flow.component.Component
import java.util.*
import kotlin.reflect.KClass

open class VerticalContainer(_id: String, vararg children: UIComponent): UIComponent(_id, containedUIComponents = children.asList())
open class HorizontalContainer(_id: String, vararg children: UIComponent): UIComponent(_id, containedUIComponents = children.asList())

fun captionFrom(id: String): String = UtilsUI.captionFromId(id)

data class Label(private val _id: String, val caption: String = captionFrom(_id)): UIComponent(_id)

enum class InputType { Text, Password }
data class Input(private val _id: String, val type: InputType, val caption: String = captionFrom(_id)): UIComponent(_id)
data class Button(private val _id: String, val caption: String = captionFrom(_id)): UIComponent(_id)
data class InsideAppLink(private val _id: String, val uiViewClass: Class<out Component>, val caption: String = captionFrom(_id)): UIComponent(_id)

data class Grid(private val _id: String, val elementType: KClass<out Any>, val columns: List<String> = listOf(), val caption: String = captionFrom(_id)): UIComponent(_id)

open class EmptyDomain

abstract class Page<T : Any>(val uiView: UIComponent, val pageController: PageController<T>, val pageDomain: PageDomain<T> = PageDomain(EmptyDomain() as T)): UIComponent(UUID.randomUUID().toString()) {
    private val observers: MutableMap<String, (Any) -> Unit> = mutableMapOf()
    fun hasValues(vararg ids: String): Boolean = ids.all { getFieldValue(it).let { value -> value != null && value.toString().isNotEmpty() } }
    fun toDebugString(): String = fields().map { "$it: ${getFieldValue(it)}" }.joinToString { it }.replace(",", "</br>")
    fun addFieldChangeObserver(fieldName: String, observer: (Any) -> Unit) {
        observers[fieldName] = observer
    }

    fun getFieldValue(id: String): Any? {
        val field = pageDomain.dataClass.javaClass.getDeclaredField(id)
        field.isAccessible = true
        return field.get(pageDomain.dataClass)
    }

    fun setFieldValue(id: String, value: Any) {
        val field = pageDomain.dataClass.javaClass.getDeclaredField(id)
        field.isAccessible = true
        observers[id]?.invoke(value)
        field.set(pageDomain.dataClass, value)
    }
    fun fields(): List<String> = pageDomain.dataClass.javaClass.declaredFields.map { it.name }
}
