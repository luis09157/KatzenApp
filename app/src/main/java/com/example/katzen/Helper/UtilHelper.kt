package com.example.katzen.Helper

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
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
    }
}