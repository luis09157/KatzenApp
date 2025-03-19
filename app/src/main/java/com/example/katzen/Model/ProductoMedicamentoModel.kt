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
    var codigoInterno: String = "",
    var codigoBarras: String = "",
    var tipo: String = "",
    var unidadMedida: String = "",
    var costoCompra: String = "0.00",
    var margenGanancia: String = "0.00",
    var precioSinIva: String = "0.00",
    var iva: String = "0.00",
    var precio: String = "0.00",
    var categoria: String = "",
    var fechaRegistro: String = "",
    var activo: Boolean = true
) : Parcelable
