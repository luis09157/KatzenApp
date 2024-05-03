package com.example.katzen.Helper

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DialogMaterialHelper {
    companion object {

        fun mostrarSuccessDialog(activity: Activity, mensaje: String) {
            activity.runOnUiThread {
                MaterialAlertDialogBuilder(activity)
                    .setTitle("Éxito")
                    .setMessage(mensaje)
                    .setPositiveButton("Aceptar", null)
                    .show()
            }
        }

        fun mostrarErrorDialog(activity: Activity, mensaje: String) {
            activity.runOnUiThread {
                MaterialAlertDialogBuilder(activity)
                    .setTitle("Advertencia")
                    .setMessage(mensaje)
                    .setPositiveButton("Aceptar", null)
                    .show()
            }
        }

        fun mostrarConfirmDialog(activity: Activity, message: String, callback: (confirmed: Boolean) -> Unit) {
            MaterialAlertDialogBuilder(activity)
                .setMessage(message)
                .setPositiveButton("Confirmar") { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                    callback.invoke(true)
                }
                .setNegativeButton("Cancelar") { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                    callback.invoke(false)
                }
                .show()
        }
    }
}