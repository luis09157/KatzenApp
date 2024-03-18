package com.example.katzen.Model

import java.util.Date
import java.util.UUID

data class Producto(
    val id: String = UUID.randomUUID().toString(), // ID único para el producto
    val nombre: String,
    val precioVenta: Double,
    val costo: Double,
    val ganancia: Double,
    val fecha: Date,
    val metodoPago: String
)