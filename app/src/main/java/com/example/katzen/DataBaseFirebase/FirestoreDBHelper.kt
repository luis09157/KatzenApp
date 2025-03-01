package com.example.katzen.DataBaseFirebase

import android.graphics.Bitmap
import android.util.Log
import com.example.katzen.Model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class FirestoreDBHelper {

    val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    companion object{
        val _URL_STORAGE_FIREBASE = "https://firebasestorage.googleapis.com/v0/b/rutasmagicas-2514a.appspot.com/o/"
    }


    fun getUserDataFromFirestore(
        idUsuario: String,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Referencia a la colección de usuarios en Firestore
        val userDocRef = firestore.collection("RutasMagicas")
            .document("RegistroUsuarios")
            .collection("Usuarios")
            .document(idUsuario)

        // Obtener el documento del usuario
        userDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Extraer los campos necesarios
                    val nombreUsuario = documentSnapshot.getString("nombreUsuario") ?: ""
                    val correo = documentSnapshot.getString("correo") ?: ""
                    val imagenPerfil = documentSnapshot.getString("imagenPerfil") ?: ""

                    // Crear una instancia del modelo User
                    val user = User(
                        nombreUsuario = nombreUsuario,
                        correo = correo,
                        imagenPerfil = imagenPerfil
                    )

                    // Devolver el objeto User en caso de éxito
                    onSuccess(user)
                } else {
                    // Si el documento no existe, lanzar una excepción
                    onFailure(Exception("El usuario con ID $idUsuario no existe"))
                }
            }
            .addOnFailureListener { exception ->
                // Manejar cualquier error que ocurra durante la obtención del documento
                Log.e("FirestoreDBHelper", "Error obteniendo datos del usuario: ${exception.message}")
                onFailure(exception)
            }
    }

}
