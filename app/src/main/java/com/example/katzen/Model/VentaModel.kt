package com.example.katzen.Model

import java.util.UUID

data class VentaModel(
    var id: String = UUID.randomUUID().toString(),
    var idProducto: String = "",
    var articulo: String = "",
    var venta: Double = 0.0,
    var costo: Double = 0.0,
    var ganancia: Double = 0.0,
    var fecha: String = "",
    var metodoPago: String = "EFECTIVO",
    var categoria: String = ""
) {
    companion object {
        fun calcularGanancia(venta: Double, costo: Double): Double =
            (venta - costo).coerceAtLeast(0.0)
    }
}
