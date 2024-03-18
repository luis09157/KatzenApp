package com.example.katzen.Validator

import android.widget.EditText
import android.widget.Toast
import com.example.katzen.Model.Producto
import java.text.SimpleDateFormat
import java.util.*

class ValidadorProducto {

    companion object {
        fun validarYCrearProducto(editTextNombre: EditText, editTextPrecioVenta: EditText, editTextCosto: EditText, editTextFecha: EditText, editTextMetodoPago: EditText): Producto? {
            val nombre = editTextNombre.text.toString()
            val precioVentaString = editTextPrecioVenta.text.toString()
            val costoString = editTextCosto.text.toString()
            val fechaString = editTextFecha.text.toString()
            val metodoPago = editTextMetodoPago.text.toString()

            // Validar que los campos no estén vacíos
            if (nombre.isEmpty() || precioVentaString.isEmpty() || costoString.isEmpty() || fechaString.isEmpty() || metodoPago.isEmpty()) {
                Toast.makeText(editTextNombre.context, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                return null
            }

            // Validar que los valores numéricos sean válidos
            val precioVenta = precioVentaString.toDoubleOrNull()
            val costo = costoString.toDoubleOrNull()
            var fecha = Date() // Por defecto, se utiliza la fecha actual si la fecha ingresada no es válida
            try {
                fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fechaString) ?: Date()
            } catch (e: Exception) {
                Toast.makeText(editTextNombre.context, "Formato de fecha inválido", Toast.LENGTH_SHORT).show()
                return null
            }

            if (precioVenta == null || costo == null) {
                Toast.makeText(editTextNombre.context, "Los campos Precio de Venta y Costo deben ser números válidos", Toast.LENGTH_SHORT).show()
                return null
            }

            // Crear y devolver el objeto Producto
            return Producto(nombre = nombre, precioVenta = precioVenta, costo = costo, ganancia = precioVenta - costo, fecha = fecha, metodoPago = metodoPago)
        }
    }
}
