package com.octopus.eternalUi.domain


import com.octopus.eternalUi.domain.db.Message
import com.octopus.eternalUi.vaadinBridge.EternalUI
import com.vaadin.flow.component.Component
import java.util.*
import kotlin.reflect.KClass

open class VerticalContainer(vararg children: UIComponent, _id: String = UUID.randomUUID().toString(), _cssClassName: String = ""): UIComponent(_id, _cssClassName, containedUIComponents = children.asList().toMutableList()) {
    constructor(id: String, vararg children: Tab, cssClassName: String = ""): this(children = *children, _id = id, _cssClassName = cssClassName)
}
open class HorizontalContainer(vararg children: UIComponent, _id: String = UUID.randomUUID().toString(), _cssClassName: String = ""): UIComponent(_id, _cssClassName, containedUIComponents = children.asList().toMutableList()) {
    constructor(id: String, vararg children: Tab, cssClassName: String = ""): this(children = *children, _id = id, _cssClassName = cssClassName)
}
open class TabsContainer(vararg children: Tab, _id: String = UUID.randomUUID().toString(), _cssClassName: String = ""): UIComponent(_id, _cssClassName, containedUIComponents = children.asList().toMutableList()) {
    constructor(id: String, vararg children: Tab, cssClassName: String = ""): this(children = *children, _id = id, _cssClassName = cssClassName)
}
open class Tab(val caption: String, val page: Page, _id: String = UUID.randomUUID().toString(), _cssClassName: String = ""): UIComponent(_id, _cssClassName)
open class ModalWindow(val page: Page, _id: String = UUID.randomUUID().toString(), val onClose: (Any) -> Unit = {}, _cssClassName: String = ""): UIComponent(_id, _cssClassName)
open class ConfirmDialog(val message: String, val onOk: () -> Unit, val onCancel: () -> Unit = {}, val okMessage: String = "Ok", val cancelMessage: String = "Cancel", _cssClassName: String = "", _id: String = UUID.randomUUID().toString()): UIComponent(_id, _cssClassName)
open class File(val url: String, _id: String = UUID.randomUUID().toString()): UIComponent(_id)

enum class UserMessageType { INFO }
open class UserMessage(val message: String, val type: UserMessageType = UserMessageType.INFO): UIComponent(UUID.randomUUID().toString(), "")

fun captionFrom(id: String): String = UtilsUI.captionFromId(id)
fun idFrom(caption: String): String = UtilsUI.idFromCaption(caption)

data class Label(val caption: String, private val _cssClassName: String = "", private val _id: String = UUID.randomUUID().toString()): UIComponent(_id, _cssClassName)

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
                val caption: String = captionFrom(_id), val gridConfiguration: GridConfig = GridConfiguration()): UIComponent(_id, _cssClassName)
abstract class GridConfig(var columnGenerators: Map<String, Any> = mapOf(), open var gridSelectionType: GridSelectionType = GridSelectionType.SINGLE, open var rowsToShow: Int = 10)
data class GridConfiguration(var _columnGenerators: Map<String, UIComponent> = mapOf()): GridConfig(_columnGenerators as Map<String, Any>)
//data class DomainDrivenGridConfiguration<T: Any>(var _columnGenerators: Map<String, (T) -> UIComponent> = mapOf()): GridConfig(_columnGenerators as Map<String, Any>)
enum class GridSelectionType { SINGLE, MULTI, NONE }


open class EmptyDomain

@Suppress("UNCHECKED_CAST")
abstract class Page(val uiView: UIComponent, var pageDomain: Any = EmptyDomain(),
                             var set: Boolean = false, var beforeEnter: ((EternalUI) -> Any)? = null
):
        UIComponent(UUID.randomUUID().toString(), pageDomain.javaClass.simpleName) {
    private val observers: MutableMap<String, (Any?) -> Unit> = mutableMapOf()
    internal val controller: PageController = PageController()
    fun <T: Any> domain(): T = pageDomain as T
    fun hasValues(vararg ids: String): Boolean = ids.all { getFieldValue(it).let { value -> value != null && value.toString().isNotEmpty() } }
    fun anyValue(vararg ids: String): Boolean = ids.any { getFieldValue(it).let { value -> value != null && value.toString().isNotEmpty() } }
    fun toDebugString(): String = fields().map { "$it: ${getFieldValue(it)}" }.joinToString { it }.replace(",", "</br>")
    fun addFieldChangeObserver(fieldName: String, observer: (Any?) -> Unit) {
        observers[fieldName] = observer
    }

    fun getFieldValue(id: String): Any? {
        return try {
            val field = pageDomain.javaClass.getDeclaredField(id)
            field.isAccessible = true
            field.get(pageDomain)
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
        val field = pageDomain.javaClass.getDeclaredField(id)
        field.isAccessible = true
        observers[id]?.invoke(value)
        if (field.type.name.toUpperCase().contains("SET")) {
            if (value is Collection<*>) {
                field.set(pageDomain, value)
            } else {
                field.set(pageDomain, listOf(value))
            }
        } else {
            when (value) {
                null -> field.set(pageDomain, value)
                is Optional<*> -> if (value.isPresent) field.set(pageDomain, value.get()) else field.set(pageDomain, null)
                is Message -> field.set(pageDomain, value.message)
                is Collection<*> -> if (value.isEmpty()) field.set(pageDomain, null) else field.set(pageDomain, value.first())
                else -> field.set(pageDomain, value)
            }
        }
        setInMetadata(id, value)
    }

    private fun setInMetadata(id: String, value: Any?) {
        try {
            val metadata = pageDomain.javaClass.getDeclaredField("metadata").let {
                it.isAccessible = true
                return@let it.get(pageDomain) as MutableMap<String, Any?>
            }
            metadata[id] = value
        } catch (ex: Exception) {}
    }

    fun fields(): List<String> = pageDomain.javaClass.declaredFields.map { it.name }
}

internal class PageController(val actions: MutableList<Action> = mutableListOf(),
                                      val enabledRules: MutableList<Rule> = mutableListOf(),
                                      val uiDataProviders: MutableList<UiDataProvider> = mutableListOf())
