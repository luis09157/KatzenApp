package com.example.katzen.Model

import java.util.UUID

data class Producto(
    var id: String = UUID.randomUUID().toString(),
    var nombre: String = "",
    var descripcion: String = "",
    var precioVenta: Double = 0.0,
    var costo: Double = 0.0,
    var ganancia: Double = 0.0,
    var fecha: String = "",
    var rutaImagen: String = ""
)