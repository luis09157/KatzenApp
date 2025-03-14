package com.example.katzen.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ServicioModel(
    var id: String = "",
    var nombre: String = "",
    var codigoInterno: String = "",
    var descripcion: String = "",
    var precioUnitario: String = "0.00",
    var precioVenta: String = "0.00",
    var margenGanancia: String = "0",
    var iva: String = "0.00",
    var precioFinal: String = "0.00",
    var activo: Boolean = true,
    var fechaRegistro: String = ""
) : Parcelable 