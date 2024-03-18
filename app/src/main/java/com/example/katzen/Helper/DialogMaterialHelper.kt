package com.example.katzen.Helper

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DialogMaterialHelper {
    companion object{

        fun mostrarSuccessDialog(context: Context, mensaje: String) {
            MaterialAlertDialogBuilder(context)
                .setTitle("Exito")
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", null)
                .show()
        }
        fun mostrarErrorDialog(context: Context, mensaje: String) {
            MaterialAlertDialogBuilder(context)
                .setTitle("Error")
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", null)
                .show()
        }
    }
}