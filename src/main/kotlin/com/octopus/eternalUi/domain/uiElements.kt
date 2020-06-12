package com.octopus.eternalUi.domain

import com.octopus.eternalUi.domain.db.Message
import com.vaadin.flow.component.Component
import java.io.InputStream
import java.util.*
import kotlin.reflect.KClass

open class VerticalContainer(vararg children: UIComponent, _id: String = UUID.randomUUID().toString(), _cssClassName: String = ""): UIComponent(_id, _cssClassName, containedUIComponents = children.asList())
open class HorizontalContainer(vararg children: UIComponent, _id: String = UUID.randomUUID().toString(), _cssClassName: String = ""): UIComponent(_id, _cssClassName, containedUIComponents = children.asList())
open class TabsContainer(vararg children: Tab<*>, _id: String = UUID.randomUUID().toString(), _cssClassName: String = ""): UIComponent(_id, _cssClassName, containedUIComponents = children.asList())
open class Tab<T: Any>(val caption: String, val page: Page<T>, _id: String = UUID.randomUUID().toString(), _cssClassName: String = ""): UIComponent(_id, _cssClassName)
open class ModalWindow<T: Any>(val page: Page<T>, _id: String = UUID.randomUUID().toString(), val onClose: (T) -> Unit = {}, _cssClassName: String = ""): UIComponent(_id, _cssClassName)
open class ConfirmDialog(val message: String, val onOk: () -> Unit, val onCancel: () -> Unit = {}, val okMessage: String = "Ok", val cancelMessage: String = "Cancel", _cssClassName: String = "", _id: String = UUID.randomUUID().toString()): UIComponent(_id, _cssClassName)
open class File(val url: String, _id: String = UUID.randomUUID().toString()): UIComponent(_id)

enum class UserMessageType { INFO }
open class UserMessage(val message: String, val type: UserMessageType = UserMessageType.INFO): UIComponent(UUID.randomUUID().toString(), "")

fun captionFrom(id: String): String = UtilsUI.captionFromId(id)

data class Label(private val _id: String, private val _cssClassName: String = "", val caption: String = captionFrom(_id)): UIComponent(_id, _cssClassName)

enum class InputType { Text, TextArea, Password, Select, Date, Radio, Checkbox }
data class Input(private val _id: String, val type: InputType = InputType.Text, private val _cssClassName: String = "", val caption: String = captionFrom(_id)): UIComponent(_id, _cssClassName)
enum class InputNumberType { Double, Integer, BigDecimal, Currency }
data class InputNumber(private val _id: String, val type: InputNumberType = InputNumberType.Double, private val _cssClassName: String = "", val caption: String = captionFrom(_id),
                       val step: Number? = null, val min: Number? = null, val max: Number? = null): UIComponent(_id, _cssClassName)
data class Button(private val _id: String, private val _cssClassName: String = "", val caption: String = captionFrom(_id)): UIComponent(_id, _cssClassName)
data class DownloadButton(private val _id: String, private val _cssClassName: String = "", val caption: String = captionFrom(_id)): UIComponent(_id, _cssClassName)
data class InsideAppLink(private val _id: String, val uiViewClass: Class<out Component>, private val _cssClassName: String = "",
                         val caption: String = captionFrom(_id)): UIComponent(_id, _cssClassName)

data class Grid(private val _id: String, val elementType: KClass<out Any>, val columns: List<String> = listOf(), private val _cssClassName: String = "",
                val caption: String = captionFrom(_id), val gridConfiguration: GridConfiguration = GridConfiguration()): UIComponent(_id, _cssClassName)
data class GridConfiguration(val columnGenerators: Map<String, UIComponent> = mapOf(), val gridSelectionType: GridSelectionType = GridSelectionType.SINGLE)
enum class GridSelectionType { SINGLE, MULTI, NONE }


open class EmptyDomain

@Suppress("UNCHECKED_CAST")
abstract class Page<T : Any>(val uiView: UIComponent, val pageController: PageController<T> = PageController(), var pageDomain: PageDomain<T> = PageDomain(EmptyDomain() as T), var set: Boolean = false):
        UIComponent(UUID.randomUUID().toString(), pageDomain.javaClass.simpleName) {
    private val observers: MutableMap<String, (Any?) -> Unit> = mutableMapOf()
    fun hasValues(vararg ids: String): Boolean = ids.all { getFieldValue(it).let { value -> value != null && value.toString().isNotEmpty() } }
    fun anyValue(vararg ids: String): Boolean = ids.any { getFieldValue(it).let { value -> value != null && value.toString().isNotEmpty() } }
    fun toDebugString(): String = fields().map { "$it: ${getFieldValue(it)}" }.joinToString { it }.replace(",", "</br>")
    fun addFieldChangeObserver(fieldName: String, observer: (Any?) -> Unit) {
        observers[fieldName] = observer
    }

    fun getFieldValue(id: String): Any? {
        return try {
            val field = pageDomain.dataClass.javaClass.getDeclaredField(id)
            field.isAccessible = true
            field.get(pageDomain.dataClass)
        } catch (e: NoSuchFieldException) {
            null
        }
    }

    fun removeFieldValue(id: String): Any? {
        return try {
            val value = getFieldValue(id)
            setFieldValue(id, null)
            value
        } catch (e: NoSuchFieldException) {
            null
        }
    }

    fun setFieldValue(id: String, value: Any?) {
        val field = pageDomain.dataClass.javaClass.getDeclaredField(id)
        field.isAccessible = true
        observers[id]?.invoke(value)
        when (value) {
            null -> field.set(pageDomain.dataClass, value)
            is Optional<*> -> if (value.isPresent) field.set(pageDomain.dataClass, value.get()) else field.set(pageDomain.dataClass, null)
            is Message -> field.set(pageDomain.dataClass, value.message)
            else -> field.set(pageDomain.dataClass, value)
        }
    }

    fun fields(): List<String> = pageDomain.dataClass.javaClass.declaredFields.map { it.name }
}
