package com.example.katzen.Model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class VacunaModel(
    var id: String = "",
    var idPaciente: String = "",
    var idCliente: String = "", 
    var fecha: String = "",
    var vacuna: String = "",
    var cantidadAplicada: String = "",
    var lote: String = "",
    var validezDias: String = "",
    var recordatorio: Boolean = false,
    var fechaRecordatorio: String = "",
    var observaciones: String = "",
    var productosAplicados: List<ProductoAplicadoModel> = emptyList(),
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