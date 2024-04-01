package com.example.katzen.Model

import java.util.UUID

data class ProductoModel(
    var id: String = UUID.randomUUID().toString(),
    var nombre: String = "",
    var descripcion: String = "",
    var precioVenta: Double = 0.0,
    var costo: Double = 0.0,
    var ganancia: Double = 0.0,
    var fecha: String = "",
    var rutaImagen: String = "",
    var categoria: String = "",
    var proveedor: String = "",
    var cantidadInventario: Double = 0.00
)