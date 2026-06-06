package com.example.katzen.Helper

import android.util.Log
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase

object PortalInviteHelper {
    private const val TAG = "PortalInviteHelper"

    fun inviteClientePortal(
        clienteId: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (clienteId.isBlank()) {
            onFailure("Cliente no válido.")
            return
        }

        val data = hashMapOf("clienteId" to clienteId)
        Firebase.functions
            .getHttpsCallable("inviteClientePortal")
            .call(data)
            .addOnSuccessListener { result ->
                val payload = result.getData() as? Map<*, *>
                val message = payload?.get("message") as? String
                    ?: "Portal activado correctamente."
                val tempPassword = payload?.get("tempPassword") as? String
                val fullMessage = if (!tempPassword.isNullOrBlank()) {
                    "$message\nContraseña temporal: $tempPassword"
                } else {
                    message
                }
                onSuccess(fullMessage)
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "Error invitando al portal", error)
                onFailure(error.localizedMessage ?: "No se pudo activar el portal.")
            }
    }
}
