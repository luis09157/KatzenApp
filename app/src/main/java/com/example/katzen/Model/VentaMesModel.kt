package com.example.katzen.Model

import java.util.UUID

data class VentaMesModel(
     var id: String = UUID.randomUUID().toString(),
     var venta: String = "",
     var costo: String = "",
     var mes: String = "",
     var anio: String = "",
     var fecha: String ="",
     var ganancia: String = "",
     var cargos : String = ""
)