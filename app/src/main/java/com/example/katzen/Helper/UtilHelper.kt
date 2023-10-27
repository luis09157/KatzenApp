package com.example.katzen.Helper

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.example.katzen.Model.VentaMesModel
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
    }
}