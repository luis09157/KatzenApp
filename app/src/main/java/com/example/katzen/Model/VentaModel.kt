package com.example.katzen.Model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class VentaModel(
    var id: String = "", var producto: String? = null, var categoria: String? = null,
    var costo: Double? = null, var venta: Double? = null, var fecha: String? = null) {
}