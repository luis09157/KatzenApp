package com.example.katzen.Helper

import android.util.Log
import com.example.katzen.DataBaseFirebase.FirebaseAuthPerfilUtil
import com.example.katzen.Model.AuthPerfilModel
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth

object AuthRoleHelper {
    private const val TAG = "AuthRoleHelper"
    const val ROLE_STAFF = "staff"
    const val ROLE_CLIENT = "client"
    const val PORTAL_URL = "https://katzen-a0e3e.web.app/portal/login"

    enum class AccessRole {
        STAFF,
        CLIENT,
        UNKNOWN
    }

    data class PortalSession(
        val selectedMode: AccessRole = AccessRole.UNKNOWN,
        val clienteId: String = "",
        val staffRole: String = "",
        val staffAccess: Boolean = false,
        val clientAccess: Boolean = false
    ) {
        fun isDual(): Boolean = staffAccess && clientAccess

        fun needsRolePicker(): Boolean = isDual() && selectedMode == AccessRole.UNKNOWN

        fun isClient(): Boolean =
            selectedMode == AccessRole.CLIENT ||
                (clientAccess && !staffAccess && selectedMode != AccessRole.STAFF)

        fun isStaff(): Boolean =
            selectedMode == AccessRole.STAFF ||
                (staffAccess && !clientAccess && selectedMode != AccessRole.CLIENT)

        fun isUnknown(): Boolean = !staffAccess && !clientAccess

        fun withMode(mode: AccessRole): PortalSession = copy(selectedMode = mode)
    }

    fun fetchPortalSession(
        forceRefreshToken: Boolean = false,
        onResult: (PortalSession) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onResult(PortalSession())
            return
        }

        val uid = user.uid
        FirebaseAuthPerfilUtil.obtenerPerfilAsync(
            uid,
            onSuccess = { perfil ->
                val session = sessionFromPerfil(perfil)
                if (session.isUnknown()) {
                    resolveFromClaims(user, forceRefreshToken, onResult, onError)
                } else {
                    onResult(session)
                }
            },
            onFailure = { error ->
                Log.w(TAG, "AuthPerfiles no disponible: ${error.message}")
                resolveFromClaims(user, forceRefreshToken, onResult, onError)
            }
        )
    }

    private fun resolveFromClaims(
        user: com.google.firebase.auth.FirebaseUser,
        forceRefreshToken: Boolean,
        onResult: (PortalSession) -> Unit,
        onError: (Exception) -> Unit
    ) {
        user.getIdToken(forceRefreshToken)
            .addOnSuccessListener { tokenResult ->
                val claims = tokenResult.claims
                onResult(sessionFromClaims(claims))
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "Claims no disponibles: ${error.message}")
                onError(error)
                onResult(PortalSession())
            }
    }

    private fun sessionFromPerfil(perfil: AuthPerfilModel?): PortalSession {
        if (perfil == null || !perfil.activo) {
            return PortalSession()
        }
        return PortalSession(
            clienteId = perfil.clienteId,
            staffRole = perfil.staffRole,
            staffAccess = perfil.hasStaffAccess(),
            clientAccess = perfil.hasClientAccess()
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun sessionFromClaims(claims: Map<String, Any?>): PortalSession {
        val roleClaim = claims["role"] as? String
        val clienteId = claims["clienteId"] as? String ?: ""
        val staffRole = claims["staffRole"] as? String ?: ""
        val dualAccess = claims["dualAccess"] == true

        val staffAccess = roleClaim == ROLE_STAFF || dualAccess
        val clientAccess = roleClaim == ROLE_CLIENT ||
            (clienteId.isNotBlank() && (dualAccess || roleClaim == ROLE_STAFF))

        return PortalSession(
            clienteId = clienteId,
            staffRole = staffRole,
            staffAccess = staffAccess,
            clientAccess = clientAccess
        )
    }

    suspend fun fetchPortalSessionBlocking(): PortalSession {
        val user = FirebaseAuth.getInstance().currentUser ?: return PortalSession()
        return try {
            val perfil = FirebaseAuthPerfilUtil.obtenerPerfil(user.uid)
            val fromPerfil = sessionFromPerfil(perfil)
            if (!fromPerfil.isUnknown()) return fromPerfil

            val tokenResult = Tasks.await(user.getIdToken(true))
            sessionFromClaims(tokenResult.claims)
        } catch (e: Exception) {
            Log.w(TAG, "Sesión no disponible: ${e.message}")
            PortalSession()
        }
    }
}
