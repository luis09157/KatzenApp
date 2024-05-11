package com.example.katzen.Model

import android.content.Context
import com.example.katzen.R
import java.util.UUID

data class VentaMesDetalleModel(
    var id: String = UUID.randomUUID().toString(),
    var nombreDomicilio: String = "",
    var categoria: String = "",
    var costo: String = "",
    var domicilio: String = "",
    var fecha: String = "",
    var ganancia: String = "",
    var kilometros: String = "",
    var venta: String = "",
    var linkMaps: String = "",
    var nombreCliente: String = "",
    var idCliente: String = ""
) {
    companion object {
        fun validarViaje(context: Context, viaje: VentaMesDetalleModel): ValidationResult {
            if (viaje.nombreDomicilio.isEmpty() || viaje.categoria.isEmpty() ||
                viaje.domicilio.isEmpty() ||
                viaje.fecha.isEmpty() || viaje.kilometros.isEmpty() ||
                viaje.linkMaps.isEmpty() ||
                viaje.nombreCliente.isEmpty() || viaje.idCliente.isEmpty()
            ) {
                return ValidationResult(false, context.getString(R.string.error_empty_fields))
            }

            // Si todas las validaciones pasan, retornar un resultado válido
            return ValidationResult(true)
        }
    }
}
