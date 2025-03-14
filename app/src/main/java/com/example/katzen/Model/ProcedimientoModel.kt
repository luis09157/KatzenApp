package com.example.katzen.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProcedimientoModel(
    var id: String = "",
    var nombre: String = "",
    var codigoInterno: String = "",
    var instrucciones: String = "",
    var tipo: String = "Otro", // Otro, Castración, Eutanasia
    var precioUnitario: String = "0.00",
    var iva: String = "0.00",
    var precioFinal: String = "0.00",
    var activo: Boolean = true,
    var fechaRegistro: String = ""
) : Parcelable 