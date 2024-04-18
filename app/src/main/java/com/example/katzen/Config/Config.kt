package com.example.katzen.Config

import com.example.katzen.Model.MascotaModel
import com.example.katzen.Model.ProductoModel

class Config {
    companion object{

        val CATEGORIAS = listOf("Semana X4","Semana X2", "Campaña", "Ruta", "Moto")
        val UNIDAD_MEDIDA = listOf("Piezas","Mililitros", "Litros")
        val CATEGORIAS_PRODUCTO = listOf("Medicamento","Alimento", "Pet Shop")
        val PROVEEDORES = listOf("GoPet")
        val SEXO = listOf("Macho","Hembra")
        val METODOS_PAGO = listOf("Efectivo","Tarjeta","Transferencia")
        val ESPECIE = listOf("Canino","Felino")


        var MES_DETALLE = ""

        var COSTO : Double = 0.00
        var VENTA : Double = 0.00
        var GANANCIA : Double = 0.00

        var PRODUCTO_EDIT : ProductoModel = ProductoModel()
        var MASCOTA_EDIT : MascotaModel = MascotaModel()
        var IMG_CHANGE : Boolean = false
    }
}