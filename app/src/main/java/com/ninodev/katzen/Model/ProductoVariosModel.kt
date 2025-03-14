package com.ninodev.katzen.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductoVariosModel(
    var id: String = "",
    var nombre: String = "",
    var codigoInterno: String = "",
    var codigoBarras: String = "",
    var unidadMedida: String = "Unidad",
    var costoCompra: String = "0.00",
    var precioVenta: String = "0.00",
    var margenGanancia: String = "0",
    var iva: String = "0",
    var precioFinal: String = "0.00",
    var activo: Boolean = true,
    var fechaRegistro: String = ""
) : Parcelable 