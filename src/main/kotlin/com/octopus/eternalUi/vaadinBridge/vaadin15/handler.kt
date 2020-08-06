package com.octopus.eternalUi.vaadinBridge.vaadin15

import com.octopus.eternalUi.domain.db.Message
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.datepicker.DatePicker
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridSingleSelectionModel
import com.vaadin.flow.component.radiobutton.RadioButtonGroup
import com.vaadin.flow.component.textfield.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

class ComponentHandler {

    companion object {
        fun switchCaptionTo(component: Component, newCaption: String) {
            when (component) {
                is com.vaadin.flow.component.button.Button -> component.text = newCaption
                is TextField -> component.label = newCaption
                is TextArea -> component.label = newCaption
                is PasswordField -> component.label = newCaption
                is ComboBox<*> -> component.label = newCaption
                is DatePicker -> component.label = newCaption
                is RadioButtonGroup<*> -> component.label = newCaption
                is Checkbox -> component.label = newCaption
            }
        }

        fun setValue(fieldValue: Any?, componentById: Component) {
            when (componentById) {
                is TextField -> componentById.value = fieldValue?.toString()
                is TextArea -> componentById.value = fieldValue?.toString()
                is PasswordField -> componentById.value = fieldValue?.toString()
                is DatePicker -> componentById.value = fieldValue?.let { return@let it as LocalDate}
                is NumberField -> componentById.value = fieldValue?.let { return@let it as Double}
                is IntegerField -> componentById.value = fieldValue?.let { return@let it as Int}
                is BigDecimalField -> componentById.value = fieldValue?.let { return@let it as BigDecimal}
                is ComboBox<*> -> if (fieldValue != null && fieldValue.toString() != "") componentById.value = Message(fieldValue.toString())
                is Grid<*> -> setValueOnGrid(componentById, fieldValue)
                is RadioButtonGroup<*> -> componentById.value = Message(fieldValue.toString())
                is Checkbox -> componentById.value = fieldValue.toString().toBoolean()
            }
        }

        private fun setValueOnGrid(componentById: Grid<*>, fieldValue: Any?) {
            val grid = componentById as Grid<Any?>
            when {
                fieldValue == null -> componentById.deselectAll()
                fieldValue is Optional<*> && fieldValue.isPresent -> grid.select(fieldValue.get())
                fieldValue is Collection<*> -> fieldValue.iterator().forEach { grid.select(it) }
                else -> grid.select(fieldValue)
            }
        }
    }
}
