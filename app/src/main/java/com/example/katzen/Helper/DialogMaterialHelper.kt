package com.example.katzen.Helper

import android.app.Activity
import android.content.DialogInterface
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DialogMaterialHelper {
    companion object {

        fun mostrarSuccessDialog(activity: Activity, mensaje: String, onAccept: () -> Unit = {}) {
            MaterialDialog(activity).show {
                title(text = "Éxito")
                message(text = mensaje)
                positiveButton(text = "Aceptar") {
                    it.dismiss()
                    onAccept()
                }
            }
        }
        fun mostrarSuccessClickDialog(activity: Activity, mensaje: String, onAceptarClick: () -> Unit) {
            activity.runOnUiThread {
                MaterialAlertDialogBuilder(activity)
                    .setTitle("Éxito")
                    .setMessage(mensaje)
                    .setPositiveButton("Aceptar") { _, _ ->
                        onAceptarClick()
                    }
                    .show()
            }
        }
        fun mostrarConfirmEditDialog(
            activity: Activity,
            mensaje: String,
            onConfirm: () -> Unit
        ) {
            activity.runOnUiThread {
                MaterialAlertDialogBuilder(activity)
                    .setTitle("Confirmar")
                    .setMessage(mensaje)
                    .setPositiveButton("Aceptar") { _, _ ->
                        onConfirm()
                    }
                    .show()
            }
        }
        fun mostrarConfirmDeleteDialog(
            activity: Activity,
            mensaje: String,
            callback: (confirmed: Boolean) -> Unit
        ) {
            activity.runOnUiThread {
                MaterialAlertDialogBuilder(activity)
                    .setTitle("Confirmar")
                    .setMessage(mensaje)
                    .setPositiveButton("Aceptar") { _, _ ->
                        callback(true)
                    }
                    .setNegativeButton("Cancelar") { _, _ ->
                        callback(false)
                    }
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