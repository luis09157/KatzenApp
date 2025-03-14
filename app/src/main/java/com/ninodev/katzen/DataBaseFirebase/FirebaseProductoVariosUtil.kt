package com.ninodev.katzen.DataBaseFirebase

import android.util.Log
import com.ninodev.katzen.Model.ProductoVariosModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID
import com.google.android.gms.tasks.Task

object FirebaseProductoVariosUtil {
    private const val PRODUCTOS_VARIOS_PATH = "Katzen/Productos/Varios"
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val referenciaProductosVarios: DatabaseReference = database.getReference(PRODUCTOS_VARIOS_PATH)

    fun obtenerListaProductosVarios(listener: ValueEventListener) {
        referenciaProductosVarios.addValueEventListener(listener)
    }

    fun removerListener(listener: ValueEventListener) {
        referenciaProductosVarios.removeEventListener(listener)
    }

    fun agregarProductoVarios(producto: ProductoVariosModel, callback: (Boolean, String) -> Unit) {
        try {
            if (producto.id.isEmpty()) {
                producto.id = UUID.randomUUID().toString()
            }
            
            if (producto.fechaRegistro.isEmpty()) {
                producto.fechaRegistro = System.currentTimeMillis().toString()
            }
            
            val referencia = referenciaProductosVarios.child(producto.id)
            referencia.setValue(producto)
                .addOnSuccessListener {
                    Log.d("FirebaseProductoVariosUtil", "Producto agregado correctamente con ID: ${producto.id}")
                    callback(true, "Producto agregado correctamente")
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseProductoVariosUtil", "Error al agregar producto: ${e.message}")
                    callback(false, "Error al agregar producto: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("FirebaseProductoVariosUtil", "Excepción al agregar producto: ${e.message}")
            e.printStackTrace()
            callback(false, "Error: ${e.message}")
        }
    }

    fun actualizarProductoVarios(producto: ProductoVariosModel, callback: (Boolean, String) -> Unit) {
        try {
            if (producto.id.isEmpty()) {
                callback(false, "ID de producto no válido")
                return
            }
            
            val referencia = referenciaProductosVarios.child(producto.id)
            
            Log.d("FirebaseProductoVariosUtil", "Actualizando producto con ID: ${producto.id}")
            
            referencia.setValue(producto)
                .addOnSuccessListener {
                    Log.d("FirebaseProductoVariosUtil", "Producto actualizado correctamente")
                    callback(true, "Producto actualizado correctamente")
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseProductoVariosUtil", "Error al actualizar producto: ${e.message}")
                    callback(false, "Error al actualizar producto: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("FirebaseProductoVariosUtil", "Excepción al actualizar producto: ${e.message}")
            e.printStackTrace()
            callback(false, "Error: ${e.message}")
        }
    }

    fun eliminarProductoVarios(productoId: String): Task<Void> {
        return referenciaProductosVarios.child(productoId).removeValue()
    }
} 