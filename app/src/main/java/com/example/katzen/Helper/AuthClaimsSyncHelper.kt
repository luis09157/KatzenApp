package com.example.katzen.Helper

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase

object AuthClaimsSyncHelper {
    private const val TAG = "AuthClaimsSyncHelper"

    fun syncMyClaims(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit = {}
    ) {
        Firebase.functions
            .getHttpsCallable("syncMyClaims")
            .call(hashMapOf<String, Any>())
            .addOnSuccessListener {
                refreshAuthToken(onSuccess, onFailure)
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "syncMyClaims falló: ${error.message}")
                refreshAuthToken(onSuccess, onFailure)
            }
    }

    private fun refreshAuthToken(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onSuccess()
            return
        }
        user.getIdToken(true)
            .addOnSuccessListener {
                Log.d(TAG, "Token actualizado con custom claims")
                onSuccess()
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "No se pudo refrescar token: ${error.message}")
                onFailure(error.localizedMessage ?: "No se pudieron sincronizar permisos.")
            }
    }
}
