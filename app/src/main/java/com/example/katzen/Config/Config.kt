package com.example.katzen.Config

import PacienteModel
import com.example.katzen.Model.ProductoModel

class Config {
    companion object{

        val CATEGORIAS = listOf("Semana X4","Semana X2", "Campaña", "Ruta", "Moto")
        val UNIDAD_MEDIDA = listOf("Piezas","Mililitros", "Litros")
        val CATEGORIAS_PRODUCTO = listOf("MEDICAMENTO","ALIMENTO", "PET SHOP")
        val PROVEEDORES = listOf("GOPET")
        val SEXO = listOf("Macho Entero","Hembra Entera","Macho Operado","Hembra Operada")
        val METODOS_PAGO = listOf("EFECTIVO","TARJETA","TRANSFERENCIA")
        val ESPECIE = listOf("Canino","Felino","Ave","Roedor","Lagomorfo","Reptil")


        var MES_DETALLE = ""

        var COSTO : Double = 0.00
        var VENTA : Double = 0.00
        var GANANCIA : Double = 0.00

        var PRODUCTO_EDIT : ProductoModel = ProductoModel()
        var MASCOTA_EDIT : PacienteModel = PacienteModel()
        var IMG_CHANGE : Boolean = false
    }
}