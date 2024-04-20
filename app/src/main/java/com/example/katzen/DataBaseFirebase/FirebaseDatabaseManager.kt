package com.example.katzen.DataBaseFirebase

import com.example.katzen.Model.MascotaModel
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.coroutines.resume

class FirebaseDatabaseManager {
    companion object{
        suspend fun <T> insertModel(modelo: T, MODELOS_PATH: String): Boolean {
            return suspendCancellableCoroutine { continuation ->
                try {
                    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
                    val referenciaModelos: DatabaseReference = database.getReference(MODELOS_PATH)

                    // Insertar el modelo en la base de datos
                    val modeloId = UUID.randomUUID().toString()
                    referenciaModelos.child(modeloId).setValue(modelo)
                        .addOnSuccessListener {
                            continuation.resume(true)
                        }
                        .addOnFailureListener { exception ->
                            println("Error al guardar el modelo: ${exception.message}")
                            continuation.resume(false)
                        }
                } catch (e: Exception) {
                    println("Excepción atrapada: ${e.message}")
                    continuation.resume(false)
                }
            }
        }
    }
}
