package com.example.katzen.Config

import com.example.katzen.Model.Producto

class Config {
    companion object{

        val CATEGORIAS = listOf("Semana X4","Semana X2", "Campaña", "Ruta", "Moto")
        val UNIDAD_MEDIDA = listOf("Piezas","Mililitros", "Litros")
        val SEXO = listOf("Macho","Hembra")
        val METODOS_PAGO = listOf("Efectivo","Tarjeta","Transferencia")
        val ESPECIE = listOf("Canino","Felino")
        var MES_DETALLE = ""

        var COSTO : Double = 0.00
        var VENTA : Double = 0.00
        var GANANCIA : Double = 0.00

        var PRODUCTO_EDIT : Producto = Producto()
        var IMG_CHANGE : Boolean = false
    }
}