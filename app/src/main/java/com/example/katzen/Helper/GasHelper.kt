package com.example.katzen.Helper

import com.example.katzen.Config.Config
import java.math.RoundingMode
import java.text.DecimalFormat

class GasHelper {
    companion object{
        val kmLitro = 10
        val litroGasolina = 24.50
        fun calcular(km : Double , categoria : String) :  Triple<String, String, String> {
            val df = DecimalFormat("#.##")
            df.roundingMode = RoundingMode.DOWN
            var kmV = 0.00
            var porcentaje = 0.0

            when(categoria){
                Config.CATEGORIAS[0] -> {
                    //SEMANA X4
                    kmV = km * 4
                    porcentaje = 1.66
                }
                Config.CATEGORIAS[1] -> {
                    //SEMANA X2
                    kmV = km * 2
                    porcentaje = 1.66
                }
                Config.CATEGORIAS[2] -> {
                    //CAMPAÑA
                    kmV = km * 4
                    porcentaje = 1.32
                }
                Config.CATEGORIAS[3] -> {
                    //RUTA
                    kmV = km * 2
                    porcentaje = 1.32

                }
                Config.CATEGORIAS[4] -> {
                    //MOTO
                    kmV = km * 2
                    porcentaje = 1.66
                }
            }

            var costo = (kmV / kmLitro) * litroGasolina
            var venta = costo * porcentaje

            return Triple(df.format(costo), df.format((venta - costo)), df.format(venta))
        }
    }
}