package com.example.katzen.DataBaseFirebase

import android.util.Log
import com.example.katzen.Model.ProductoMedicamentoModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class FirebaseMedicamentoUtil {
    companion object {
        private const val MEDICAMENTOS_PATH = "Katzen/Productos/Medicamentos"
        private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        private val referenciaMedicamentos: DatabaseReference = database.getReference(MEDICAMENTOS_PATH)

        fun obtenerListaMedicamentos(listener: ValueEventListener) {
            referenciaMedicamentos.addValueEventListener(listener)
        }

        fun removerListener(listener: ValueEventListener) {
            referenciaMedicamentos.removeEventListener(listener)
        }

        fun guardarMedicamento(medicamento: ProductoMedicamentoModel): Pair<Boolean, String> {
            return try {
                if (medicamento.id.isEmpty()) {
                    medicamento.id = UUID.randomUUID().toString()
                }
                
                val referencia = referenciaMedicamentos.child(medicamento.id)
                referencia.setValue(medicamento).isComplete to "Medicamento guardado exitosamente"
            } catch (e: Exception) {
                Log.e("FirebaseMedicamentoUtil", "Error al guardar medicamento: ${e.message}")
                false to "Error al guardar el medicamento: ${e.message}"
            }
        }
    }
} 