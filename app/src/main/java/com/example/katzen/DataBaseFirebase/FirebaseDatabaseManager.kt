package com.example.katzen.DataBaseFirebase

import com.example.katzen.Model.ClienteModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FirebaseDatabaseManager {
    companion object {
        private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        suspend fun <T : Any> insertModel(modelo: T, modeloId: String, MODELOS_PATH: String): Pair<Boolean, String> {
            return suspendCancellableCoroutine { continuation ->
                try {
                    val referenciaModelos: DatabaseReference = database.getReference(MODELOS_PATH)
                    val correo = (modelo as? ClienteModel)?.correo
                    if (correo != null) {
                        verificarExistenciaCorreo(referenciaModelos, correo) { existe ->
                            if (existe) {
                                continuation.resume(false to "Ya existe un registro con este correo electrónico.")
                            } else {
                                insertarModelo(referenciaModelos, modeloId, modelo) { exito, mensaje ->
                                    continuation.resume(exito to mensaje)
                                }
                            }
                        }
                    } else {
                        insertarModelo(referenciaModelos, modeloId, modelo) { exito, mensaje ->
                            continuation.resume(exito to mensaje)
                        }
                    }
                } catch (e: Exception) {
                    continuation.resume(false to "Error al insertar el modelo: ${e.message}")
                }
            }
        }

        private fun verificarExistenciaCorreo(referenciaModelos: DatabaseReference, correo: String, callback: (Boolean) -> Unit) {
            // Verificar si el correo está vacío
            if (correo.isEmpty()) {
                // Llamar al callback con false si el correo está vacío
                callback(false)
                return
            }else if(correo == "No proporcionado"){
                callback(false)
                return
            }

            // Realizar la consulta para verificar la existencia del correo en la base de datos
            referenciaModelos.orderByChild("correo").equalTo(correo).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Llamar al callback con el resultado de si el correo existe en la base de datos o no
                    callback(snapshot.exists())
                }

                override fun onCancelled(error: DatabaseError) {
                    // Llamar al callback con false en caso de error
                    callback(false)
                }
            })
        }


        private fun insertarModelo(referenciaModelos: DatabaseReference, modeloId: String, modelo: Any, callback: (Boolean, String) -> Unit) {
            referenciaModelos.child(modeloId).setValue(modelo)
                .addOnSuccessListener {
                    callback(true, "Modelo insertado exitosamente.")
                }
                .addOnFailureListener { exception ->
                    callback(false, "Error al insertar el modelo: ${exception.message}")
                }
        }
    }
}
