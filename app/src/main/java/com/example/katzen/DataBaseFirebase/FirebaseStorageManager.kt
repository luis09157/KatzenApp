package com.example.katzen.DataBaseFirebase
import android.net.Uri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import java.util.*
class FirebaseStorageManager {
    private val storage = Firebase.storage

    suspend fun uploadImage(imageUri: Uri, folderName: String): String {
        val fileName = UUID.randomUUID().toString() // Nombre autogenerado para la imagen
        val storageRef = storage.reference.child("$folderName/$fileName")

        return try {
            storageRef.putFile(imageUri).await() // Subir la imagen al almacenamiento
            storageRef.downloadUrl.await().toString() // Obtener la URL de descarga de la imagen
        } catch (e: Exception) {
            e.printStackTrace()
            "" // Devolver una cadena vacía en caso de error
        }
    }
}
