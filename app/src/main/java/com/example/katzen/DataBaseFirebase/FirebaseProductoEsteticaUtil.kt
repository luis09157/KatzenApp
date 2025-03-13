package com.example.katzen.DataBaseFirebase

import android.util.Log
import com.example.katzen.Model.ProductoEsteticaModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

object FirebaseProductoEsteticaUtil {
    private const val PRODUCTOS_ESTETICA_PATH = "Katzen/Productos/Estetica"
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val referenciaProductosEstetica: DatabaseReference = database.getReference(PRODUCTOS_ESTETICA_PATH)

    fun obtenerListaProductosEstetica(listener: ValueEventListener) {
        referenciaProductosEstetica.addValueEventListener(listener)
    }

    fun removerListener(listener: ValueEventListener) {
        referenciaProductosEstetica.removeEventListener(listener)
    }

    fun agregarProductoEstetica(producto: ProductoEsteticaModel, callback: (Boolean, String) -> Unit) {
        try {
            if (producto.id.isEmpty()) {
                producto.id = UUID.randomUUID().toString()
            }
            
            if (producto.fechaRegistro.isEmpty()) {
                producto.fechaRegistro = System.currentTimeMillis().toString()
            }
            
            val referencia = referenciaProductosEstetica.child(producto.id)
            referencia.setValue(producto)
                .addOnSuccessListener {
                    Log.d("FirebaseProductoEsteticaUtil", "Producto agregado correctamente con ID: ${producto.id}")
                    callback(true, "Producto agregado correctamente")
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseProductoEsteticaUtil", "Error al agregar producto: ${e.message}")
                    callback(false, "Error al agregar producto: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("FirebaseProductoEsteticaUtil", "Excepción al agregar producto: ${e.message}")
            e.printStackTrace()
            callback(false, "Error: ${e.message}")
        }
    }

    fun actualizarProductoEstetica(producto: ProductoEsteticaModel, callback: (Boolean, String) -> Unit) {
        try {
            if (producto.id.isEmpty()) {
                callback(false, "ID de producto no válido")
                return
            }
            
            val referencia = referenciaProductosEstetica.child(producto.id)
            
            Log.d("FirebaseProductoEsteticaUtil", "Actualizando producto con ID: ${producto.id}")
            
            referencia.setValue(producto)
                .addOnSuccessListener {
                    Log.d("FirebaseProductoEsteticaUtil", "Producto actualizado correctamente")
                    callback(true, "Producto actualizado correctamente")
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseProductoEsteticaUtil", "Error al actualizar producto: ${e.message}")
                    callback(false, "Error al actualizar producto: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("FirebaseProductoEsteticaUtil", "Excepción al actualizar producto: ${e.message}")
            e.printStackTrace()
            callback(false, "Error: ${e.message}")
        }
    }
} 