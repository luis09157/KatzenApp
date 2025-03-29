package com.example.katzen.DataBaseFirebase

import android.util.Log
import com.example.katzen.Model.VacunaModel
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class FirebaseVacunaUtil {
    companion object {
        private val database = FirebaseDatabase.getInstance()
        private val TAG = "FirebaseVacunaUtil"
        private const val BASE_REF = "Katzen"
        private const val VACUNAS_REF = "$BASE_REF/Vacunas"

        /**
         * Obtiene todas las vacunas de un paciente
         */
        fun obtenerVacunasPorPaciente(idPaciente: String, listener: ValueEventListener) {
            Log.d(TAG, "Obteniendo vacunas para el paciente: $idPaciente")
            val query: Query = database.getReference(VACUNAS_REF)
                .orderByChild("idPaciente")
                .equalTo(idPaciente)
            query.addValueEventListener(listener)
        }

        /**
         * Remueve un listener de la referencia
         */
        fun removerListener(listener: ValueEventListener) {
            database.getReference(VACUNAS_REF).removeEventListener(listener)
        }

        /**
         * Agrega una nueva vacuna
         */
        fun agregarVacuna(vacuna: VacunaModel, callback: (Boolean, String) -> Unit) {
            val vacunasRef = database.getReference(VACUNAS_REF)
            
            // Usar el ID generado como clave del documento
            vacunasRef.child(vacuna.id).setValue(vacuna)
                .addOnSuccessListener {
                    callback(true, "Vacuna agregada correctamente")
                }
                .addOnFailureListener { e ->
                    callback(false, e.message ?: "Error al agregar la vacuna")
                }
        }

        /**
         * Actualiza una vacuna existente
         */
        fun actualizarVacuna(vacuna: VacunaModel, callback: (Boolean, String) -> Unit) {
            val vacunasRef = database.getReference(VACUNAS_REF)
            
            vacunasRef.child(vacuna.id).setValue(vacuna)
                .addOnSuccessListener {
                    callback(true, "Vacuna actualizada correctamente")
                }
                .addOnFailureListener { e ->
                    callback(false, e.message ?: "Error al actualizar la vacuna")
                }
        }

        /**
         * Elimina una vacuna
         */
        fun eliminarVacuna(idVacuna: String): Task<Void> {
            return database.getReference(VACUNAS_REF).child(idVacuna).removeValue()
        }

        /**
         * Obtiene una vacuna específica por su ID
         */
        fun obtenerVacuna(vacunaId: String): Task<com.google.firebase.database.DataSnapshot> {
            return database.getReference(VACUNAS_REF).child(vacunaId).get()
        }
    }
} 