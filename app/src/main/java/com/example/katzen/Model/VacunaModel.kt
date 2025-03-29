package com.example.katzen.Model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import java.util.*

@Keep
@Parcelize
data class VacunaModel(
    var id: String = UUID.randomUUID().toString(),
    var idPaciente: String = "",
    var idVacuna: String = "",
    var fecha: String = "",
    var vacuna: String = "",
    var dosis: String = "",
    var recordatorio: Boolean = false,
    var fechaRecordatorio: String = "",
    var observaciones: String = "",
    var fechaRegistro: String = ""
) : Parcelable

@Keep
@Parcelize
data class ProductoAplicadoModel(
    var id: String = "",
    var nombre: String = "",
    var cantidad: String = "",
    var tipo: String = ""  // Puede ser "Medicamento", "Alimento", etc.
) : Parcelable 