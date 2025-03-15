package com.example.katzen.DataBaseFirebase

import android.util.Log
import com.example.katzen.Model.AuxiliarModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.android.gms.tasks.Task
import java.util.UUID

object FirebaseAuxiliarUtil {
    private const val AUXILIARES_PATH = "Katzen/Productos/Auxiliares"
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val referenciaAuxiliares: DatabaseReference = database.getReference(AUXILIARES_PATH)

    fun obtenerListaAuxiliares(listener: ValueEventListener) {
        referenciaAuxiliares.addValueEventListener(listener)
    }

    fun removerListener(listener: ValueEventListener) {
        referenciaAuxiliares.removeEventListener(listener)
    }

    fun agregarAuxiliar(auxiliar: AuxiliarModel, callback: (Boolean, String) -> Unit) {
        try {
            if (auxiliar.id.isEmpty()) {
                auxiliar.id = UUID.randomUUID().toString()
            }
            
            if (auxiliar.fechaRegistro.isEmpty()) {
                auxiliar.fechaRegistro = System.currentTimeMillis().toString()
            }
            
            val referencia = referenciaAuxiliares.child(auxiliar.id)
            referencia.setValue(auxiliar)
                .addOnSuccessListener {
                    Log.d("FirebaseAuxiliarUtil", "Auxiliar agregado correctamente con ID: ${auxiliar.id}")
                    callback(true, "Auxiliar agregado correctamente")
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseAuxiliarUtil", "Error al agregar auxiliar: ${e.message}")
                    callback(false, "Error al agregar auxiliar: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("FirebaseAuxiliarUtil", "Excepción al agregar auxiliar: ${e.message}")
            e.printStackTrace()
            callback(false, "Error: ${e.message}")
        }
    }

    fun actualizarAuxiliar(auxiliar: AuxiliarModel, callback: (Boolean, String) -> Unit) {
        try {
            if (auxiliar.id.isEmpty()) {
                callback(false, "ID de auxiliar no válido")
                return
            }
            
            val referencia = referenciaAuxiliares.child(auxiliar.id)
            
            Log.d("FirebaseAuxiliarUtil", "Actualizando auxiliar con ID: ${auxiliar.id}")
            
            referencia.setValue(auxiliar)
                .addOnSuccessListener {
                    Log.d("FirebaseAuxiliarUtil", "Auxiliar actualizado correctamente")
                    callback(true, "Auxiliar actualizado correctamente")
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseAuxiliarUtil", "Error al actualizar auxiliar: ${e.message}")
                    callback(false, "Error al actualizar auxiliar: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("FirebaseAuxiliarUtil", "Excepción al actualizar auxiliar: ${e.message}")
            e.printStackTrace()
            callback(false, "Error: ${e.message}")
        }
    }

    fun eliminarAuxiliar(auxiliarId: String): Task<Void> {
        return referenciaAuxiliares.child(auxiliarId).removeValue()
    }
} 