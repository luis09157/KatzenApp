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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
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
            if (medicamento.id.isEmpty()) {
                medicamento.id = UUID.randomUUID().toString()
            }
            
            if (medicamento.fechaRegistro.isEmpty()) {
                medicamento.fechaRegistro = System.currentTimeMillis().toString()
            }
            
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
        Log.d("FirebaseMedicamentoUtil", "Intentando eliminar medicamento con ID: $medicamentoId")
        val task = referenciaMedicamentos.child(medicamentoId).removeValue()
        task.addOnSuccessListener {
            Log.d("FirebaseMedicamentoUtil", "Medicamento eliminado correctamente")
        }.addOnFailureListener { e ->
            Log.e("FirebaseMedicamentoUtil", "Error al eliminar medicamento: ${e.message}")
        }
        return task
    }

    /**
     * Obtiene todos los medicamentos activos que son de tipo vacuna
     */
    fun obtenerMedicamentosTipoVacuna(callback: (Boolean, List<ProductoMedicamentoModel>) -> Unit) {
        // Lista de rutas a probar en orden
        val rutas = listOf(
            "producto_medicamento",
            "Productos/Medicamentos",
            "Medicamentos"
        )
        
        // Inicia la consulta recursiva con el primer índice
        consultarSiguienteRuta(rutas, 0, callback)
    }

    /**
     * Consulta recursivamente las rutas disponibles hasta encontrar medicamentos
     */
    private fun consultarSiguienteRuta(rutas: List<String>, index: Int, callback: (Boolean, List<ProductoMedicamentoModel>) -> Unit) {
        // Si ya probamos todas las rutas, devolvemos lista vacía
        if (index >= rutas.size) {
            Log.e("FirebaseMedicamentoUtil", "No se encontraron datos en ninguna ruta")
            callback(false, emptyList())
            return
        }
        
        // Obtener la ruta actual
        val rutaActual = rutas[index]
        val ref = database.getReference(rutaActual)
        
        Log.d("FirebaseMedicamentoUtil", "Consultando vacunas en: $rutaActual")
        
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    Log.d("FirebaseMedicamentoUtil", "Datos en $rutaActual: ${snapshot.childrenCount} elementos")

                    if (snapshot.childrenCount.toInt() == 0) {
                        consultarSiguienteRuta(rutas, index + 1, callback)
                        return
                    }


                    // Procesar los datos encontrados
                    val medicamentos = mutableListOf<ProductoMedicamentoModel>()
                    
                    for (medicamentoSnapshot in snapshot.children) {
                        try {
                            // Datos básicos
                            val id = medicamentoSnapshot.key ?: ""
                            val nombre = medicamentoSnapshot.child("nombre").getValue(String::class.java) ?: ""
                            
                            // Detectar si es una vacuna - verificar tipo o categoría
                            val tipo = medicamentoSnapshot.child("tipo").getValue(String::class.java) ?: ""
                            val categoria = medicamentoSnapshot.child("categoria").getValue(String::class.java) ?: ""
                            
                            val esVacuna = tipo.equals("Vacuna", ignoreCase = true) || 
                                           categoria.equals("Vacuna", ignoreCase = true)
                            
                            // Estado activo - manejo seguro sin comparar diferentes tipos
                            var estaActivo = true // por defecto asumimos activo
                            
                            // Revisar Boolean directamente (el caso más común)
                            if (medicamentoSnapshot.child("activo").exists()) {
                                medicamentoSnapshot.child("activo").getValue(Boolean::class.java)?.let {
                                    estaActivo = it
                                }
                            }
                            
                            // Si no es Boolean, intentar con otros tipos sin comparar directamente
                            if (medicamentoSnapshot.child("activo").exists() && 
                                medicamentoSnapshot.child("activo").getValue(Boolean::class.java) == null) {
                                
                                val activoValue = medicamentoSnapshot.child("activo").getValue()
                                
                                if (activoValue != null) {
                                    // Convertir a String y evaluar
                                    val activoStr = activoValue.toString()
                                    estaActivo = activoStr == "true" || activoStr == "1"
                                }
                            }
                            
                            // Datos adicionales
                            val descripcion = medicamentoSnapshot.child("descripcion").getValue(String::class.java) ?: ""
                            val precio = medicamentoSnapshot.child("precio").getValue(String::class.java) ?: 
                                         medicamentoSnapshot.child("costoCompra").getValue(String::class.java) ?: "0"
                            
                            if (esVacuna && estaActivo) {
                                val medicamento = ProductoMedicamentoModel(
                                    id = id,
                                    nombre = nombre,
                                    descripcion = descripcion,
                                    precio = precio,
                                    tipo = tipo,
                                    categoria = categoria
                                )
                                medicamentos.add(medicamento)
                                Log.d("FirebaseMedicamentoUtil", "Agregado: $nombre")
                            }
                        } catch (e: Exception) {
                            Log.e("FirebaseMedicamentoUtil", "Error procesando item: ${e.message}")
                        }
                    }
                    
                    Log.d("FirebaseMedicamentoUtil", "Total vacunas encontradas: ${medicamentos.size}")
                    
                    if (medicamentos.isEmpty()) {
                        // Si no encontramos vacunas, probar la siguiente ruta
                        consultarSiguienteRuta(rutas, index + 1, callback)
                    } else {
                        // Encontramos vacunas, retornamos
                        callback(true, medicamentos)
                    }
                    
                } catch (e: Exception) {
                    Log.e("FirebaseMedicamentoUtil", "Error en ruta $rutaActual: ${e.message}")
                    // Si hay error, probar la siguiente ruta
                    consultarSiguienteRuta(rutas, index + 1, callback)
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseMedicamentoUtil", "Consulta cancelada en $rutaActual: ${error.message}")
                // Si se cancela, probar la siguiente ruta
                consultarSiguienteRuta(rutas, index + 1, callback)
            }
        })
    }
} 