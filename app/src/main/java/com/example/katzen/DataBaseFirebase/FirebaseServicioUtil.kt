package com.example.katzen.DataBaseFirebase

import android.util.Log
import com.example.katzen.Model.ServicioModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID
import com.google.android.gms.tasks.Task

object FirebaseServicioUtil {
    private const val SERVICIOS_PATH = "Katzen/Productos/Servicios"
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val referenciaServicios: DatabaseReference = database.getReference(SERVICIOS_PATH)

    fun obtenerListaServicios(listener: ValueEventListener) {
        referenciaServicios.addValueEventListener(listener)
    }

    fun removerListener(listener: ValueEventListener) {
        referenciaServicios.removeEventListener(listener)
    }

    fun agregarServicio(servicio: ServicioModel, callback: (Boolean, String) -> Unit) {
        try {
            if (servicio.id.isEmpty()) {
                servicio.id = UUID.randomUUID().toString()
            }
            
            if (servicio.fechaRegistro.isEmpty()) {
                servicio.fechaRegistro = System.currentTimeMillis().toString()
            }
            
            val referencia = referenciaServicios.child(servicio.id)
            referencia.setValue(servicio)
                .addOnSuccessListener {
                    Log.d("FirebaseServicioUtil", "Servicio agregado correctamente con ID: ${servicio.id}")
                    callback(true, "Servicio agregado correctamente")
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseServicioUtil", "Error al agregar servicio: ${e.message}")
                    callback(false, "Error al agregar servicio: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("FirebaseServicioUtil", "Excepción al agregar servicio: ${e.message}")
            e.printStackTrace()
            callback(false, "Error: ${e.message}")
        }
    }

    fun actualizarServicio(servicio: ServicioModel, callback: (Boolean, String) -> Unit) {
        try {
            if (servicio.id.isEmpty()) {
                callback(false, "ID de servicio no válido")
                return
            }
            
            val referencia = referenciaServicios.child(servicio.id)
            
            Log.d("FirebaseServicioUtil", "Actualizando servicio con ID: ${servicio.id}")
            
            referencia.setValue(servicio)
                .addOnSuccessListener {
                    Log.d("FirebaseServicioUtil", "Servicio actualizado correctamente")
                    callback(true, "Servicio actualizado correctamente")
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseServicioUtil", "Error al actualizar servicio: ${e.message}")
                    callback(false, "Error al actualizar servicio: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("FirebaseServicioUtil", "Excepción al actualizar servicio: ${e.message}")
            e.printStackTrace()
            callback(false, "Error: ${e.message}")
        }
    }

    fun eliminarServicio(servicioId: String): Task<Void> {
        return referenciaServicios.child(servicioId).removeValue()
    }
} 