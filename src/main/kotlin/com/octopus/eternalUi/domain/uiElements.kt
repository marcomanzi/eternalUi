package com.octopus.eternalUi.domain

import com.vaadin.flow.component.Component
import java.util.*
import kotlin.reflect.KClass

open class VerticalContainer(_id: String, vararg children: UIComponent, _cssClassName: String = ""): UIComponent(_id, _cssClassName, containedUIComponents = children.asList())
open class HorizontalContainer(_id: String, vararg children: UIComponent, _cssClassName: String = ""): UIComponent(_id, _cssClassName, containedUIComponents = children.asList())

fun captionFrom(id: String): String = UtilsUI.captionFromId(id)

data class Label(private val _id: String, private val _cssClassName: String = "", val caption: String = captionFrom(_id)): UIComponent(_id, _cssClassName)

enum class InputType { Text, Password }
data class Input(private val _id: String, val type: InputType, private val _cssClassName: String = "", val caption: String = captionFrom(_id)): UIComponent(_id, _cssClassName)
data class Button(private val _id: String, private val _cssClassName: String = "", val caption: String = captionFrom(_id)): UIComponent(_id, _cssClassName)
data class InsideAppLink(private val _id: String, val uiViewClass: Class<out Component>, private val _cssClassName: String = "", val caption: String = captionFrom(_id)): UIComponent(_id, _cssClassName)

data class Grid(private val _id: String, val elementType: KClass<out Any>, val columns: List<String> = listOf(), private val _cssClassName: String = "", val caption: String = captionFrom(_id)): UIComponent(_id, _cssClassName)

open class EmptyDomain

@Suppress("UNCHECKED_CAST")
abstract class Page<T : Any>(val uiView: UIComponent, val pageController: PageController<T>, val pageDomain: PageDomain<T> = PageDomain(EmptyDomain() as T)):
        UIComponent(UUID.randomUUID().toString(), pageDomain.javaClass.simpleName) {
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
        if (value is Optional<*> && value.isPresent) {
            field.set(pageDomain.dataClass, value.get())
        } else {
            field.set(pageDomain.dataClass, value)
        }
    }

    fun fields(): List<String> = pageDomain.dataClass.javaClass.declaredFields.map { it.name }
}
