package com.example.katzen.Helper

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.example.katzen.Config.Config
import com.example.katzen.Model.VentaMesModel
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar

class UtilHelper {

    companion object{

        fun getDateIdMonth() : String {
            val time = Calendar.getInstance().time
            val formatter = SimpleDateFormat("MM-yyyy")

            return formatter.format(time).toString()
        }
        fun getDate() : String {
            val time = Calendar.getInstance().time
            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")

            return formatter.format(time).toString()
        }
        fun getID() : String {
            val time = Calendar.getInstance().time
            val formatter = SimpleDateFormat("yyyyMMddHHmmss")

            return formatter.format(time).toString()
        }
        fun View.hideKeyboard() {
            val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(windowToken, 0)
        }
        fun getMonthYear() : ArrayList<String> {
            var listVentaMes = arrayListOf<String>()
            listVentaMes.add("ENE")
            listVentaMes.add("FEB")
            listVentaMes.add("MAR")
            listVentaMes.add("ABR")
            listVentaMes.add("MAY")
            listVentaMes.add("JUN")
            listVentaMes.add("JUL")
            listVentaMes.add("AGO")
            listVentaMes.add("SEP")
            listVentaMes.add("OCT")
            listVentaMes.add("NOV")
            listVentaMes.add("DIC")

            return listVentaMes
        }
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
                    porcentaje = 1.31

                }
                Config.CATEGORIAS[4] -> {
                    //MOTO
                    kmV = km * 2
                    porcentaje = 1.66
                }
            }

            var costo = (kmV / 10) * 23
            var venta = costo * porcentaje

            return Triple(df.format(costo), df.format((venta - costo)), df.format(venta))
        }
    }
}