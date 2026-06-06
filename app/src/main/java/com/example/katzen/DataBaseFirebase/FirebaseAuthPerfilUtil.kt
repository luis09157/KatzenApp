package com.example.katzen.DataBaseFirebase

import com.example.katzen.Model.AuthPerfilModel
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object FirebaseAuthPerfilUtil {
    private const val AUTH_PERFILES_PATH = "Katzen/AuthPerfiles"

    suspend fun obtenerPerfil(authUid: String): AuthPerfilModel? {
        if (authUid.isBlank()) return null
        return suspendCancellableCoroutine { continuation ->
            FirebaseDatabase.getInstance()
                .getReference(AUTH_PERFILES_PATH)
                .child(authUid)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.exists()) {
                        continuation.resume(null)
                        return@addOnSuccessListener
                    }
                    continuation.resume(mapPerfil(snapshot))
                }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }

    private fun mapPerfil(snapshot: com.google.firebase.database.DataSnapshot): AuthPerfilModel {
        val roles = snapshot.child("roles").children.mapNotNull { child ->
            child.getValue(String::class.java)?.trim()?.takeIf { it.isNotBlank() }
        }
        return AuthPerfilModel(
            authUid = snapshot.key.orEmpty(),
            email = readString(snapshot, "email"),
            role = readString(snapshot, "role"),
            staffRole = readString(snapshot, "staffRole"),
            clienteId = readString(snapshot, "clienteId"),
            staffRefId = readString(snapshot, "staffRefId"),
            roles = roles,
            activo = snapshot.child("activo").getValue(Boolean::class.java) != false
        )
    }

    private fun readString(snapshot: com.google.firebase.database.DataSnapshot, key: String): String {
        return snapshot.child(key).getValue(String::class.java).orEmpty()
    }

    fun obtenerPerfilAsync(
        authUid: String,
        onSuccess: (AuthPerfilModel?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (authUid.isBlank()) {
            onSuccess(null)
            return
        }
        FirebaseDatabase.getInstance()
            .getReference(AUTH_PERFILES_PATH)
            .child(authUid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    onSuccess(null)
                } else {
                    onSuccess(mapPerfil(snapshot))
                }
            }
            .addOnFailureListener { onFailure(it) }
    }
}
