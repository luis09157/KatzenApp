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
     * Actualiza un medicamento existente (Firestore)
     * @param medicamento El medicamento a actualizar
     * @param callback Función de retorno con éxito/fracaso y mensaje
     */
    fun actualizarMedicamento(medicamento: ProductoMedicamentoModel, callback: (success: Boolean, message: String) -> Unit) {
        if (medicamento.id.isEmpty()) {
            callback(false, "ID de medicamento no válido")
            return
        }
        
        val docRef = db.collection(COLECCION_MEDICAMENTOS).document(medicamento.id)
        
        // Crear un mapa de datos para actualizar
        val datosActualizados = hashMapOf(
            "nombre" to medicamento.nombre,
            "descripcion" to medicamento.descripcion,
            "codigoInterno" to medicamento.codigoInterno,
            "codigoBarras" to medicamento.codigoBarras,
            "tipo" to medicamento.tipo,
            "unidadMedida" to medicamento.unidadMedida,
            "costoCompra" to medicamento.costoCompra,
            "margenGanancia" to medicamento.margenGanancia,
            "precioSinIva" to medicamento.precioSinIva,
            "iva" to medicamento.iva,
            "precio" to medicamento.precio,
            "categoria" to medicamento.categoria,
            "activo" to medicamento.activo,
            "fechaActualizacion" to System.currentTimeMillis()
        )
        
        docRef.update(datosActualizados as Map<String, Any>)
            .addOnSuccessListener {
                callback(true, "Medicamento actualizado correctamente")
            }
            .addOnFailureListener { e ->
                callback(false, "Error al actualizar medicamento: ${e.message}")
            }
    }
    
    /**
     * Agrega un nuevo medicamento (Firestore)
     * @param medicamento El medicamento a agregar
     * @param callback Función de retorno con éxito/fracaso y mensaje
     */
    fun agregarMedicamento(medicamento: ProductoMedicamentoModel, callback: (success: Boolean, message: String) -> Unit) {
        // Generar un nuevo ID para el documento
        val docRef = db.collection(COLECCION_MEDICAMENTOS).document()
        
        // Asignar el ID generado al medicamento
        medicamento.id = docRef.id
        
        // Asegurar que tenemos fechas
        if (medicamento.fechaRegistro.isEmpty()) {
            medicamento.fechaRegistro = System.currentTimeMillis().toString()
        }
        
        docRef.set(medicamento)
            .addOnSuccessListener {
                callback(true, "Medicamento agregado correctamente")
            }
            .addOnFailureListener { e ->
                callback(false, "Error al agregar medicamento: ${e.message}")
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