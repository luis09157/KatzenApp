package com.example.katzen.Model

data class ValidationResult(val isValid: Boolean, val message: String = "")

data class InventarioModel(
    var fecha: String = "",
    var cantidad: Double = 0.0,
    var unidadMedida: String = ""
) {
    companion object {
        fun validarCantidad(cantidad: Double, unidadMedida: String): ValidationResult {
            if (unidadMedida.equals("piezas", ignoreCase = true)) {
                // Si la unidad de medida es "piezas", la cantidad debe ser un número entero
                if (cantidad.isWholeNumber()) {
                    return ValidationResult(true)
                } else {
                    return ValidationResult(false, "La cantidad debe ser un número entero cuando la unidad de medida es 'piezas'")
                }
            } else if (unidadMedida.equals("mililitros", ignoreCase = true)) {
                // Si la unidad de medida es "mililitros", la cantidad puede tener decimales
                if (cantidad >= 0) {
                    return ValidationResult(true)
                } else {
                    return ValidationResult(false, "La cantidad debe ser mayor o igual a 0 cuando la unidad de medida es 'mililitros'")
                }
            } else {
                return ValidationResult(false, "Unidad de medida no válida")
            }
        }

        fun validarInventario(inventario: InventarioModel): ValidationResult {
            if (inventario.fecha.isBlank()) {
                return ValidationResult(false, "La fecha no puede estar en blanco")
            }
            if (inventario.cantidad == 0.0) {
                return ValidationResult(false, "La cantidad no puede ser 0")
            }
            if (inventario.unidadMedida.isBlank()) {
                return ValidationResult(false, "La unidad de medida no puede estar en blanco")
            }
            return validarCantidad(inventario.cantidad, inventario.unidadMedida)
        }

        // Método de extensión para Double para verificar si es un número entero
        private fun Double.isWholeNumber(): Boolean {
            return this % 1 == 0.0
        }
    }
}
