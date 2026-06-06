package com.example.katzen.Helper

import android.util.Log
import com.google.firebase.auth.FirebaseAuth

/**
 * Centraliza la verificación de sesión al abrir la app.
 * Mantiene sync de claims + resolución de rol en un solo flujo para evitar
 * llamadas duplicadas y pantallas de login visibles durante el bootstrap.
 */
object AuthSessionHelper {
    private const val TAG = "AuthSessionHelper"

    fun bootstrap(
        onAuthenticated: (AuthRoleHelper.PortalSession) -> Unit,
        onUnauthenticated: () -> Unit,
        onInvalidSession: () -> Unit = onUnauthenticated
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onUnauthenticated()
            return
        }

        val userId = user.uid
        if (userId.isBlank()) {
            onUnauthenticated()
            return
        }

        HelperUser._ID_USER = userId
        FirebaseMonitoringHelper.setUserId(userId)

        AuthClaimsSyncHelper.syncMyClaims(
            onSuccess = {
                resolveSession(
                    forceRefreshToken = false,
                    onAuthenticated = onAuthenticated,
                    onInvalidSession = onInvalidSession
                )
            },
            onFailure = { message ->
                Log.w(TAG, "Claims no sincronizados: $message")
                resolveSession(
                    forceRefreshToken = true,
                    onAuthenticated = onAuthenticated,
                    onInvalidSession = onInvalidSession
                )
            }
        )
    }

    fun verifySessionQuick(
        onAuthenticated: (AuthRoleHelper.PortalSession) -> Unit,
        onUnauthenticated: () -> Unit,
        onInvalidSession: () -> Unit = onUnauthenticated
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onUnauthenticated()
            return
        }

        HelperUser._ID_USER = user.uid
        resolveSession(
            forceRefreshToken = false,
            onAuthenticated = onAuthenticated,
            onInvalidSession = onInvalidSession
        )
    }

    fun refreshClaimsInBackground() {
        AuthClaimsSyncHelper.syncMyClaims(onSuccess = {}, onFailure = {})
    }

    private fun resolveSession(
        forceRefreshToken: Boolean,
        onAuthenticated: (AuthRoleHelper.PortalSession) -> Unit,
        onInvalidSession: () -> Unit
    ) {
        AuthRoleHelper.fetchPortalSession(
            forceRefreshToken = forceRefreshToken,
            onResult = { session ->
                when {
                    session.needsRolePicker() -> onAuthenticated(session)
                    session.isClient() || session.isStaff() -> onAuthenticated(session)
                    else -> {
                        Log.w(TAG, "Sesión sin perfil válido")
                        FirebaseAuth.getInstance().signOut()
                        HelperUser._ID_USER = ""
                        onInvalidSession()
                    }
                }
            },
            onError = {
                FirebaseAuth.getInstance().signOut()
                HelperUser._ID_USER = ""
                onInvalidSession()
            }
        )
    }
}
