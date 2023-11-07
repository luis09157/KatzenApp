package com.example.katzen.Model

data class  VentaMesDetalleModel (
    var categoria: String = "",
    var costo: String = "",
    var domicilio: String = "",
    var fecha: String = "",
    var ganancia: String = "",
    var kilometros: String = "",
    var venta: String = "",
    var linkMaps: String = "",

    var isEdit : Boolean  = false,
    var key : String = ""
)