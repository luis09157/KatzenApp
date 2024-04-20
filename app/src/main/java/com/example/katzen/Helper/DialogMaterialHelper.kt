package com.example.katzen.Helper

import android.app.Activity
import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DialogMaterialHelper {
    companion object{

        fun mostrarSuccessDialog(activity: Activity, mensaje: String) {
            activity.runOnUiThread {
                MaterialAlertDialogBuilder(activity)
                    .setTitle("Exito")
                    .setMessage(mensaje)
                    .setPositiveButton("Aceptar", null)
                    .show()
            }
        }
        fun mostrarErrorDialog(activity: Activity, mensaje: String) {
            activity.runOnUiThread {
                MaterialAlertDialogBuilder(activity)
                    .setTitle("Error")
                    .setMessage(mensaje)
                    .setPositiveButton("Aceptar", null)
                    .show()
            }
        }
    }
}