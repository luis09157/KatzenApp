package com.example.katzen.DataBaseFirebase

import android.util.Log
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.ProductoMedicamentoModel
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.UUID

object FirebaseMedicamentoUtil {
    // Constantes para ambas bases de datos
    private const val COLECCION_MEDICAMENTOS = "medicamentos"
    private const val MEDICAMENTOS_PATH = "Katzen/Productos/Medicamentos"
    
    // Referencias a Firestore
    private val db = FirebaseFirestore.getInstance()
    
    // Referencias a Realtime Database (para compatibilidad con código existente)
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val referenciaMedicamentos: DatabaseReference = database.getReference(MEDICAMENTOS_PATH)

    /**
     * Obtiene lista de medicamentos (Realtime Database)
     */
    fun obtenerListaMedicamentos(listener: ValueEventListener) {
        referenciaMedicamentos.addValueEventListener(listener)
    }

    /**
     * Elimina un listener (Realtime Database)
     */
    fun removerListener(listener: ValueEventListener) {
        referenciaMedicamentos.removeEventListener(listener)
    }

    /**
     * Guarda un medicamento (Realtime Database)
     */
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

    /**
     * Actualiza un medicamento existente (Realtime Database)
     * @param medicamento El medicamento a actualizar
     * @param callback Función de retorno con éxito/fracaso y mensaje
     */
    fun actualizarMedicamento(medicamento: ProductoMedicamentoModel, callback: (success: Boolean, message: String) -> Unit) {
        try {
            if (medicamento.id.isEmpty()) {
                callback(false, "ID de medicamento no válido")
                return
            }
            
            // Actualizar en Realtime Database
            val referencia = referenciaMedicamentos.child(medicamento.id)
            
            // En lugar de modificar el objeto, lo guardamos tal cual
            // Ya que Realtime Database almacena todo el objeto
            Log.d("FirebaseMedicamentoUtil", "Actualizando medicamento con ID: ${medicamento.id}")
            
            referencia.setValue(medicamento)
                .addOnSuccessListener {
                    Log.d("FirebaseMedicamentoUtil", "Medicamento actualizado correctamente")
                    callback(true, "Medicamento actualizado correctamente")
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseMedicamentoUtil", "Error al actualizar medicamento: ${e.message}")
                    callback(false, "Error al actualizar medicamento: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("FirebaseMedicamentoUtil", "Excepción al actualizar medicamento: ${e.message}")
            e.printStackTrace()
            callback(false, "Error: ${e.message}")
        }
    }
    
    /**
     * Agrega un nuevo medicamento (Realtime Database)
     * @param medicamento El medicamento a agregar
     * @param callback Función de retorno con éxito/fracaso y mensaje
     */
    fun agregarMedicamento(medicamento: ProductoMedicamentoModel, callback: (success: Boolean, message: String) -> Unit) {
        try {
            // Asignar ID si no tiene uno
            if (medicamento.id.isEmpty()) {
                medicamento.id = UUID.randomUUID().toString()
            }
            
            // Asegurar que tenemos fechas
            if (medicamento.fechaRegistro.isEmpty()) {
                medicamento.fechaRegistro = System.currentTimeMillis().toString()
            }
            
            // Guardar en Realtime Database (es lo que usa la app actualmente)
            val referencia = referenciaMedicamentos.child(medicamento.id)
            referencia.setValue(medicamento)
                .addOnSuccessListener {
                    Log.d("FirebaseMedicamentoUtil", "Medicamento agregado correctamente con ID: ${medicamento.id}")
                    callback(true, "Medicamento agregado correctamente")
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseMedicamentoUtil", "Error al agregar medicamento: ${e.message}")
                    callback(false, "Error al agregar medicamento: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("FirebaseMedicamentoUtil", "Excepción al agregar medicamento: ${e.message}")
            e.printStackTrace()
            callback(false, "Error: ${e.message}")
        }
    }
    
    /**
     * Obtiene la lista de medicamentos (Firestore)
     * @param callback Función de retorno con la lista de medicamentos
     */
    fun obtenerMedicamentos(callback: (List<ProductoMedicamentoModel>) -> Unit) {
        db.collection(COLECCION_MEDICAMENTOS)
            .orderBy("nombre", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                val medicamentos = result.documents.mapNotNull { doc ->
                    val medicamento = doc.toObject(ProductoMedicamentoModel::class.java)
                    medicamento?.id = doc.id
                    medicamento
                }
                callback(medicamentos)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }
    
    /**
     * Elimina un medicamento (Firestore)
     * @param medicamentoId ID del medicamento a eliminar
     * @return Task que se completa cuando la operación finaliza
     */
    fun eliminarMedicamento(medicamentoId: String): Task<Void> {
        return db.collection(COLECCION_MEDICAMENTOS).document(medicamentoId).delete()
    }
} 