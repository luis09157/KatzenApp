package com.example.katzen.Model

import java.util.UUID

data class Producto(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val precioVenta: Double,
    val costo: Double,
    val ganancia: Double,
    val fecha: String, // Cambio de Date a String
    var rutaImagen: String // Nueva propiedad para la ruta de la imagen del producto
)
