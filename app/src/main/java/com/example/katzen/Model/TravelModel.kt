package com.example.katzen.Model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class TravelModel(
    var anio: Int = 0,
    var mes: Int = 0,
    var costo : Double = 0.0,
    var ganancia : Double = 0.0,
    var venta : Double = 0.0
)