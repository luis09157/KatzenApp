package com.example.katzen.DataBaseFirebase


import android.content.Context
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Model.InventarioModel
import com.google.firebase.database.*

object FirebaseInventarioUtil {

    private const val INVENTARIO_PATH = "Katzen/Inventario"

    private val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }

    interface RegistroInventarioCallback {
        fun onRegistroAgregadoExitosamente()
        fun onRegistroError(mensaje: String)
    }

    fun agregarRegistroInventario(context: Context, idProducto: String, inventario: InventarioModel, callback: RegistroInventarioCallback) {
        val referenciaInventario = database.getReference("$INVENTARIO_PATH/$idProducto")

        val nuevoRegistroKey = referenciaInventario.push().key

        referenciaInventario.child(nuevoRegistroKey!!)
            .setValue(inventario)
            .addOnSuccessListener {
                callback.onRegistroAgregadoExitosamente()
            }
            .addOnFailureListener { exception ->
                callback.onRegistroError("Error al agregar registro de inventario: ${exception.message}")
            }
    }


    fun obtenerInventarioPorProducto(productoId: String, onComplete: (List<InventarioModel>) -> Unit) {
        val referenciaInventario = database.getReference(INVENTARIO_PATH).child(productoId)

        referenciaInventario.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(inventarioSnapshot: DataSnapshot) {
                val inventarioList = mutableListOf<InventarioModel>()
                for (registroInventarioSnapshot in inventarioSnapshot.children) {
                    val inventario = registroInventarioSnapshot.getValue(InventarioModel::class.java)
                    inventario?.let {
                        inventarioList.add(it)
                    }
                }
                onComplete(inventarioList)
            }

            override fun onCancelled(error: DatabaseError) {
                onComplete(emptyList())
            }
        })
    }
}
