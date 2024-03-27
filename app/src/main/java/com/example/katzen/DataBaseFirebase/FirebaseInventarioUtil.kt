package com.example.katzen.DataBaseFirebase

import android.content.Context
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Model.InventarioModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FirebaseInventarioUtil {
    companion object {
        private const val INVENTARIO_PATH = "Katzen/Inventario" // Ruta donde se guardarán los registros de inventario

        @JvmStatic
        fun agregarRegistroInventario(context: Context, idProducto: String, inventario: InventarioModel) {
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val referenciaInventario: DatabaseReference = database.getReference(INVENTARIO_PATH).child(idProducto)

            // Generar una nueva clave para el registro de inventario
            val nuevoRegistroKey = referenciaInventario.push().key ?: ""

            // Guardar el registro de inventario en la base de datos
            referenciaInventario.child(nuevoRegistroKey).setValue(inventario)
                .addOnSuccessListener {
                    // Operación exitosa
                    DialogMaterialHelper.mostrarSuccessDialog(context, "Registro de inventario agregado exitosamente.")
                }
                .addOnFailureListener { exception ->
                    // Manejar errores
                    DialogMaterialHelper.mostrarErrorDialog(context, "Error al agregar registro de inventario: ${exception.message}")
                }
        }
    }
}