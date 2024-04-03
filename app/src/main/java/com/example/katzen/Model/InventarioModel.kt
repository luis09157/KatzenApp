package com.example.katzen.Model

data class InventarioModel(
    var fecha: String = "",
    var cantidad: Double = 0.0,
    var unidadMedida: String = ""
) {
    companion object {
        fun validarCantidad(cantidad: Double, unidadMedida: String): Boolean {
            if (unidadMedida.equals("piezas", ignoreCase = true)) {
                // Si la unidad de medida es "piezas", la cantidad debe ser un número entero
                return cantidad.isWholeNumber()
            } else if (unidadMedida.equals("mililitros", ignoreCase = true)) {
                // Si la unidad de medida es "mililitros", la cantidad puede tener decimales
                return cantidad >= 0 // Aquí puedes agregar otras condiciones de validación según tus requisitos
            }
            return false // Retorna falso si la unidad de medida no es "piezas" ni "mililitros"
        }

        fun validarInventario(inventario: InventarioModel): Boolean {
            return validarCantidad(inventario.cantidad, inventario.unidadMedida)
                    && inventario.fecha.isNotBlank()
                    && inventario.unidadMedida.isNotBlank()
        }

        // Método de extensión para Double para verificar si es un número entero
        private fun Double.isWholeNumber(): Boolean {
            return this % 1 == 0.0
        }
    }
}
