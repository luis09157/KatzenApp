package com.example.katzen.Helper

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.example.katzen.Config.Config
import com.example.katzen.Model.VentaMesModel
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.FieldPosition
import java.text.SimpleDateFormat
import java.util.Calendar

class UtilHelper {

    companion object{

        fun getDateIdMonth() : String {
            val time = Calendar.getInstance().time
            val formatter = SimpleDateFormat("MM-yyyy")

            return formatter.format(time).toString()
        }
        fun getDateYear() : String {
            val time = Calendar.getInstance().time
            val formatter = SimpleDateFormat("yyyy")

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
        fun getMonthYear(position: Int) : String {
            var mes = ""
            when(position){
                1 -> {
                    mes = "ENE"
                }
                2 -> {
                    mes = "FEB"
                }
                3 -> {
                    mes = "MAR"
                }
                4 -> {
                    mes = "ABR"
                }
                5 -> {
                    mes = "MAY"
                }
                6 -> {
                    mes = "JUN"
                }
                7 -> {
                    mes = "JUL"
                }
                8 -> {
                    mes = "AGO"
                }
                9 -> {
                    mes = "SEP"
                }
                10 -> {
                    mes = "OCT"
                }
                11 -> {
                    mes = "NOV"
                }
                12 -> {
                    mes = "DIC"
                }
            }
            return mes
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
        fun getMontsThisYears() : ArrayList<String>{
            var listMonts = arrayListOf<String>()

            listMonts.add("01-${getDateYear()}")
            listMonts.add("02-${getDateYear()}")
            listMonts.add("03-${getDateYear()}")
            listMonts.add("04-${getDateYear()}")
            listMonts.add("05-${getDateYear()}")
            listMonts.add("06-${getDateYear()}")
            listMonts.add("07-${getDateYear()}")
            listMonts.add("08-${getDateYear()}")
            listMonts.add("09-${getDateYear()}")
            listMonts.add("10-${getDateYear()}")
            listMonts.add("11-${getDateYear()}")
            listMonts.add("12-${getDateYear()}")

            return listMonts
        }
    }
}