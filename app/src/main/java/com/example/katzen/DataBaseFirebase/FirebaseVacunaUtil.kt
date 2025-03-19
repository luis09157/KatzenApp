package com.example.katzen.DataBaseFirebase

import android.util.Log
import com.example.katzen.Model.VacunaModel
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.util.UUID

object FirebaseVacunaUtil {
    private const val TAG = "FirebaseVacunaUtil"
    private const val VACUNAS_PATH = "Katzen/Pacientes/Vacunas"
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val referenciaVacunas: DatabaseReference = database.getReference(VACUNAS_PATH)

    /**
     * Obtiene todas las vacunas de un paciente
     */
    fun obtenerVacunasPorPaciente(idPaciente: String, listener: ValueEventListener) {
        Log.d(TAG, "Obteniendo vacunas para el paciente: $idPaciente")
        val query: Query = referenciaVacunas.orderByChild("idPaciente").equalTo(idPaciente)
        query.addValueEventListener(listener)
    }

    /**
     * Remueve un listener de la referencia
     */
    fun removerListener(listener: ValueEventListener) {
        referenciaVacunas.removeEventListener(listener)
    }

    /**
     * Agrega una nueva vacuna
     */
    fun agregarVacuna(vacuna: VacunaModel, callback: (Boolean, String) -> Unit) {
        try {
            // Generar un nuevo ID para la vacuna si no tiene uno
            if (vacuna.id.isEmpty()) {
                vacuna.id = UUID.randomUUID().toString()
            }
            
            Log.d(TAG, "Agregando vacuna con ID: ${vacuna.id} para paciente: ${vacuna.idPaciente}")
            
            // Almacenar la vacuna en Firebase
            referenciaVacunas.child(vacuna.id).setValue(vacuna)
                .addOnSuccessListener {
                    Log.d(TAG, "Vacuna agregada exitosamente con ID: ${vacuna.id}")
                    callback(true, "Vacuna registrada exitosamente")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al agregar vacuna: ${e.message}", e)
                    callback(false, e.message ?: "Error desconocido")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al agregar vacuna: ${e.message}", e)
            callback(false, e.message ?: "Error desconocido")
        }
    }

    /**
     * Actualiza una vacuna existente
     */
    fun actualizarVacuna(vacuna: VacunaModel, callback: (Boolean, String) -> Unit) {
        try {
            if (vacuna.id.isEmpty()) {
                Log.e(TAG, "Error: Intentando actualizar vacuna con ID vacío")
                return callback(false, "ID de vacuna inválido")
            }
            
            Log.d(TAG, "Actualizando vacuna con ID: ${vacuna.id}")
            
            referenciaVacunas.child(vacuna.id).setValue(vacuna)
                .addOnSuccessListener {
                    Log.d(TAG, "Vacuna actualizada exitosamente con ID: ${vacuna.id}")
                    callback(true, "Vacuna actualizada exitosamente")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al actualizar vacuna: ${e.message}", e)
                    callback(false, e.message ?: "Error desconocido")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al actualizar vacuna: ${e.message}", e)
            callback(false, e.message ?: "Error desconocido")
        }
    }

    /**
     * Elimina una vacuna
     */
    fun eliminarVacuna(vacunaId: String): Task<Void> {
        Log.d(TAG, "Eliminando vacuna con ID: $vacunaId")
        return referenciaVacunas.child(vacunaId).removeValue()
    }

    /**
     * Obtiene una vacuna específica por su ID
     */
    fun obtenerVacuna(vacunaId: String): Task<com.google.firebase.database.DataSnapshot> {
        return referenciaVacunas.child(vacunaId).get()
    }
} 