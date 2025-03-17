package com.example.katzen.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AuxiliarModel(
    var id: String = "",
    var nombre: String = "",
    var codigoInterno: String = "",
    var activo: Boolean = true,
    var tipo: String = "",
    var laboratorio: String = "",
    var tipoMuestra: String = "",
    var metodoAnalisis: String = "",
    var instrucciones: String = "",
    var costoCompra: String = "0.00",
    var precioUnitario: String = "0.00",
    var margenGanancia: String = "0.00",
    var porcentajeIva: String = "0.00",
    var precioFinal: String = "0.00",
    var fechaRegistro: String = ""
) : Parcelable 