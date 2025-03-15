package com.example.katzen.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AuxiliarModel(
    var id: String = "",
    var nombre: String = "",
    var codigoInterno: String = "",
    var activo: Boolean = true,
    var fechaRegistro: String = ""
) : Parcelable 