package com.example.katzen.Model

import android.content.Context
import com.ninodev.katzen.R
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

            if (viaje.nombreDomicilio.isEmpty()) {
                return ValidationResult(false, context.getString(R.string.error_empty_nombre_domicilio))
            } else if (viaje.categoria.isEmpty()) {
                return ValidationResult(false, context.getString(R.string.error_empty_categoria))
            } else if (viaje.domicilio.isEmpty()) {
                return ValidationResult(false, context.getString(R.string.error_empty_domicilio))
            } else if (viaje.fecha.isEmpty()) {
                return ValidationResult(false, context.getString(R.string.error_empty_fecha))
            } else if (viaje.kilometros.isEmpty()) {
                return ValidationResult(false, context.getString(R.string.error_empty_kilometros))
            } else if (viaje.linkMaps.isEmpty()) {
                return ValidationResult(false, context.getString(R.string.error_empty_link_maps))
            }


            // Si todas las validaciones pasan, retornar un resultado válido
            return ValidationResult(true)
        }
    }
}
