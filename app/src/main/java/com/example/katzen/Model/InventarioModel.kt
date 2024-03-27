package com.example.katzen.Model

data class InventarioModel(
    var fecha: String = "",
    var cantidad: Double = 0.0, // Utilizamos Double para manejar cantidades fraccionarias
    var unidadMedida: String = "" // Unidad de medida del inventario (por ejemplo, "piezas", "ml", "µl", etc.)
)