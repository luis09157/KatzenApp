package com.example.katzen.DataBaseFirebase

import android.util.Log
import com.example.katzen.Model.ProcedimientoModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.android.gms.tasks.Task
import java.util.UUID

object FirebaseProcedimientoUtil {
    private const val PROCEDIMIENTOS_PATH = "Katzen/Productos/Procedimientos"
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val referenciaProcedimientos: DatabaseReference = database.getReference(PROCEDIMIENTOS_PATH)

    fun obtenerListaProcedimientos(listener: ValueEventListener) {
        referenciaProcedimientos.addValueEventListener(listener)
    }

    fun removerListener(listener: ValueEventListener) {
        referenciaProcedimientos.removeEventListener(listener)
    }

    fun agregarProcedimiento(procedimiento: ProcedimientoModel, callback: (Boolean, String) -> Unit) {
        try {
            if (procedimiento.id.isEmpty()) {
                procedimiento.id = UUID.randomUUID().toString()
            }
            
            if (procedimiento.fechaRegistro.isEmpty()) {
                procedimiento.fechaRegistro = System.currentTimeMillis().toString()
            }
            
            val referencia = referenciaProcedimientos.child(procedimiento.id)
            referencia.setValue(procedimiento)
                .addOnSuccessListener {
                    Log.d("FirebaseProcedimientoUtil", "Procedimiento agregado correctamente con ID: ${procedimiento.id}")
                    callback(true, "Procedimiento agregado correctamente")
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseProcedimientoUtil", "Error al agregar procedimiento: ${e.message}")
                    callback(false, "Error al agregar procedimiento: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("FirebaseProcedimientoUtil", "Excepción al agregar procedimiento: ${e.message}")
            e.printStackTrace()
            callback(false, "Error: ${e.message}")
        }
    }

    fun actualizarProcedimiento(procedimiento: ProcedimientoModel, callback: (Boolean, String) -> Unit) {
        try {
            if (procedimiento.id.isEmpty()) {
                callback(false, "ID de procedimiento no válido")
                return
            }
            
            val referencia = referenciaProcedimientos.child(procedimiento.id)
            
            Log.d("FirebaseProcedimientoUtil", "Actualizando procedimiento con ID: ${procedimiento.id}")
            
            referencia.setValue(procedimiento)
                .addOnSuccessListener {
                    Log.d("FirebaseProcedimientoUtil", "Procedimiento actualizado correctamente")
                    callback(true, "Procedimiento actualizado correctamente")
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseProcedimientoUtil", "Error al actualizar procedimiento: ${e.message}")
                    callback(false, "Error al actualizar procedimiento: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("FirebaseProcedimientoUtil", "Excepción al actualizar procedimiento: ${e.message}")
            e.printStackTrace()
            callback(false, "Error: ${e.message}")
        }
    }

    fun eliminarProcedimiento(procedimientoId: String): Task<Void> {
        return referenciaProcedimientos.child(procedimientoId).removeValue()
    }
} 