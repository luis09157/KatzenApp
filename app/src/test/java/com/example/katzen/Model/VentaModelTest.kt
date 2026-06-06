package com.example.katzen.Model

import org.junit.Assert.assertEquals
import org.junit.Test

class VentaModelTest {

    @Test
    fun calcularGanancia_restaVentaMenosCosto() {
        assertEquals(50.0, VentaModel.calcularGanancia(150.0, 100.0), 0.001)
    }

    @Test
    fun calcularGanancia_noRetornaNegativo() {
        assertEquals(0.0, VentaModel.calcularGanancia(80.0, 100.0), 0.001)
    }
}
