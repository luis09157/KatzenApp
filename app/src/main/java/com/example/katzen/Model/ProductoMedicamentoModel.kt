package com.example.katzen.Model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class ProductoMedicamentoModel(
    var id: String = "",
    var nombre: String = "",
    var descripcion: String = "",
    var precio: String = "",
    var tipo: String = "",  // "Vacuna", "Medicamento", etc.
    var categoria: String = "", // Podría ser igual a tipo o diferente
    var activo: Boolean = true,
    var codigoBarras: String = "",
    var codigoInterno: String = "",
    var costoCompra: String = "",
    var fechaRegistro: String = "",
    var presentacion: String = "", // ml, pastillas, etc.
    // Otros campos que podrían existir en tu base de datos
    var stock: String = "",
    var unidadMedida: String = "",
    var imagenUrl: String = "",
    var margenGanancia: String = "0.00",
    var precioSinIva: String = "0.00",
    var iva: String = "0.00"
) : Parcelable
