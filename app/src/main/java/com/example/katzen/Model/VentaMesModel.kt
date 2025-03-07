package com.example.katzen.Model

import java.util.UUID

data class VentaMesModel(
     var id: String = UUID.randomUUID().toString(),
     var venta: String = "",
     var costo: String = "",
     var mes: String = "",
     var anio: String = "",
     var fecha: String = "",
     var ganancia: String = "",
     var cargos: String = ""
) {
     // Constructor sin argumentos requerido por Firebase
     constructor() : this(
          id = UUID.randomUUID().toString(),
          venta = "",
          costo = "",
          mes = "",
          anio = "",
          fecha = "",
          ganancia = "",
          cargos = ""
     )
}
