package com.example.katzen.Helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import com.example.katzen.Config.Config
import com.example.katzen.Model.VentaMesModel
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.FieldPosition
import java.text.SimpleDateFormat
import java.util.Calendar

class UtilHelper {

    companion object{
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
        fun hideKeyBoardWorld(activity : Activity,view : View){
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view!!.getWindowToken(), 0)
        }
        fun abrirGoogleMaps(activity: Activity, urlGoogleMaps: String) {
            if(urlGoogleMaps.isNotEmpty()){
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(urlGoogleMaps)
                )
                activity.startActivity(intent)
                if (intent.resolveActivity(activity.packageManager) != null) {
                    activity.startActivity(intent)
                } else {
                    DialogMaterialHelper.mostrarErrorDialog(activity, "No se pudo abrir la aplicación de Google Maps")
                }
            }else{
                DialogMaterialHelper.mostrarErrorDialog(activity, "No tiene una direccion relacionada.")
            }

        }
        fun llamarCliente(activity: Activity, phoneNumber: String){
            if(phoneNumber.isNotEmpty()){
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                }
                activity.startActivity(intent)
            }
        }

    }
}