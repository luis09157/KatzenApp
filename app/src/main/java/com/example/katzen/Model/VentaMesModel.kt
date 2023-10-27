package com.example.katzen.Model

import com.google.firebase.database.PropertyName
import com.google.gson.annotations.Expose

data class VentaMesModel(
     var venta: String = "",
     var costo: String = "",
     var mes: String = "",
     var anio: String = "",
     var fecha: String ="",
     var ganancia: String = ""
)