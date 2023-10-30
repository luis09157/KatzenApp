package com.example.katzen.Model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class VentaModel(
    var costo: String? = "",
    var ganancia: String? = "",
    var mes: String? = "",
    var venta: String? = "",
    var anio: String? = "",
    var cargos: String? = ""
    )