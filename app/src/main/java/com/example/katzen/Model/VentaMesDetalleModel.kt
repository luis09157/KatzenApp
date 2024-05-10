package com.example.katzen.Model

import java.util.UUID

data class  VentaMesDetalleModel (
    var id: String = UUID.randomUUID().toString(),
    var categoria: String = "",
    var costo: String = "",
    var domicilio: String = "",
    var fecha: String = "",
    var ganancia: String = "",
    var kilometros: String = "",
    var venta: String = "",
    var linkMaps: String = "",
    var nombreCliente: String = "",
    var idCliente: String = "",

    var isEdit : Boolean  = false,
    var key : String = "",
    var key_date : String = "",
    var key_fecha_hora : String = ""

)