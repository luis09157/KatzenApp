package com.example.katzen.Model

import java.util.UUID
import android.content.Context
import com.example.katzen.R
import java.text.SimpleDateFormat
import java.util.*

data class ProductoModel(
    var id: String = UUID.randomUUID().toString(),
    var nombre: String = "",
    var descripcion: String = "",
    var precioVenta: Double = 0.0,
    var costo: Double = 0.0,
    var ganancia: Double = 0.0,
    var fecha: String = "",
    var rutaImagen: String = "",
    var categoria: String = "",
    var proveedor: String = "",
    var cantidadInventario: Double = 0.00
) {
    companion object {
        fun validarProducto(context: Context, producto: ProductoModel): ValidationResult {
            if (producto.nombre.isEmpty() || producto.precioVenta <= 0 || producto.costo <= 0 || producto.fecha.isEmpty()) {
                return ValidationResult(false, context.getString(R.string.error_empty_fields))
            }

            // Validar y convertir la fecha a un formato válido
            val fecha: String
            try {
                val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).parse(producto.fecha)
                if (date.after(Date())) {
                    return ValidationResult(false, context.getString(R.string.error_future_date))
                }
                fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date)
            } catch (e: Exception) {
                return ValidationResult(false, context.getString(R.string.error_invalid_date))
            }

            // Validar la imagen
            if (producto.rutaImagen.isEmpty() || producto.rutaImagen.equals("")
                || producto.rutaImagen == null  || producto.rutaImagen.equals("null")) {
                return ValidationResult(false, context.getString(R.string.error_no_image_selected))
            }

            // Si todas las validaciones pasan, retornar un resultado válido
            return ValidationResult(true)
        }
    }
}
