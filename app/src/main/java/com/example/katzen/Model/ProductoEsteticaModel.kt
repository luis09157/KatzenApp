package com.example.katzen.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductoEsteticaModel(
    var id: String = "",
    var nombre: String = "",
    var codigoInterno: String = "",
    var precioUnitario: String = "0.00",
    var iva: String = "0.00",
    var precioFinal: String = "0.00",
    var activo: Boolean = true,
    var fechaRegistro: String = ""
) : Parcelable 