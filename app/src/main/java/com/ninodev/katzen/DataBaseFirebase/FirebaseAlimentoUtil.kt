package com.ninodev.katzen.DataBaseFirebase

import android.util.Log
import com.ninodev.katzen.Model.ProductoAlimentoModel
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

object FirebaseAlimentoUtil {
    private const val ALIMENTOS_PATH = "Katzen/Productos/Alimentos"
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val referenciaAlimentos: DatabaseReference = database.getReference(ALIMENTOS_PATH)

    fun obtenerListaAlimentos(listener: ValueEventListener) {
        referenciaAlimentos.addValueEventListener(listener)
    }

    fun removerListener(listener: ValueEventListener) {
        referenciaAlimentos.removeEventListener(listener)
    }

    fun agregarAlimento(alimento: ProductoAlimentoModel, callback: (Boolean, String) -> Unit) {
        try {
            if (alimento.id.isEmpty()) {
                alimento.id = UUID.randomUUID().toString()
            }
            
            if (alimento.fechaRegistro.isEmpty()) {
                alimento.fechaRegistro = System.currentTimeMillis().toString()
            }
            
            val referencia = referenciaAlimentos.child(alimento.id)
            referencia.setValue(alimento)
                .addOnSuccessListener {
                    Log.d("FirebaseAlimentoUtil", "Alimento agregado correctamente con ID: ${alimento.id}")
                    callback(true, "Alimento agregado correctamente")
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseAlimentoUtil", "Error al agregar alimento: ${e.message}")
                    callback(false, "Error al agregar alimento: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("FirebaseAlimentoUtil", "Excepción al agregar alimento: ${e.message}")
            e.printStackTrace()
            callback(false, "Error: ${e.message}")
        }
    }

    fun actualizarAlimento(alimento: ProductoAlimentoModel, callback: (Boolean, String) -> Unit) {
        try {
            if (alimento.id.isEmpty()) {
                callback(false, "ID de alimento no válido")
                return
            }
            
            val referencia = referenciaAlimentos.child(alimento.id)
            
            Log.d("FirebaseAlimentoUtil", "Actualizando alimento con ID: ${alimento.id}")
            
            referencia.setValue(alimento)
                .addOnSuccessListener {
                    Log.d("FirebaseAlimentoUtil", "Alimento actualizado correctamente")
                    callback(true, "Alimento actualizado correctamente")
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseAlimentoUtil", "Error al actualizar alimento: ${e.message}")
                    callback(false, "Error al actualizar alimento: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("FirebaseAlimentoUtil", "Excepción al actualizar alimento: ${e.message}")
            e.printStackTrace()
            callback(false, "Error: ${e.message}")
        }
    }

    fun eliminarAlimento(alimentoId: String): Task<Void> {
        return referenciaAlimentos.child(alimentoId).removeValue()
    }
} 