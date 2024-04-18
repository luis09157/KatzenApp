package com.example.katzen.DataBaseFirebase

import com.example.katzen.Model.MascotaModel
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseDatabaseManager {
    private val database = Firebase.database.reference

    suspend fun  insertModel(model: MascotaModel, databasePath: String): Boolean {
        return try {
            val modelRef = database.child(databasePath).push() // Obtener una referencia única en la ruta especificada
            modelRef.child(model.id).setValue(model).await() // Insertar el modelo en la base de datos y esperar la operación
            true // La inserción fue exitosa
        } catch (e: DatabaseException) {
            e.printStackTrace()
            false // Hubo un error durante la inserción
        }
    }
}
