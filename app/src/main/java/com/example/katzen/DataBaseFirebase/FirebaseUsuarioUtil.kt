package com.example.katzen.DataBaseFirebase

import android.util.Log
import com.example.katzen.Model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase

object FirebaseUsuarioUtil {
    private const val TAG = "FirebaseUsuarioUtil"
    private const val USUARIOS_PATH = "Katzen/Usuarios"

    fun getUserData(
        userId: String,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        FirebaseDatabase.getInstance()
            .getReference(USUARIOS_PATH)
            .child(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    onSuccess(mapUser(snapshot))
                } else {
                    onFailure(Exception("No existe registro en Katzen/Usuarios/$userId"))
                }
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Error leyendo usuario: ${error.message}")
                onFailure(error)
            }
    }

    private fun mapUser(snapshot: DataSnapshot): User {
        return User(
            nombreUsuario = readString(snapshot, "nombreUsuario", "nombre", "displayName"),
            correo = readString(snapshot, "correo", "email", "mail"),
            imagenPerfil = readString(snapshot, "imagenPerfil", "foto", "photoUrl", "avatar"),
            role = readString(snapshot, "role", "rol"),
            staffRole = readString(snapshot, "staffRole", "perfil", "rol")
        )
    }

    private fun readString(snapshot: DataSnapshot, vararg keys: String): String {
        for (key in keys) {
            snapshot.child(key).getValue(String::class.java)?.takeIf { it.isNotBlank() }?.let {
                return it
            }
        }
        return ""
    }
}
