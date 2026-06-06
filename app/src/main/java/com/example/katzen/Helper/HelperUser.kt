package com.example.katzen.Helper

import android.util.Log
import com.example.katzen.DataBaseFirebase.FirebaseUsuarioUtil
import com.example.katzen.DataBaseFirebase.FirestoreDBHelper
import com.example.katzen.MainActivity
import com.example.katzen.Model.User
import com.google.firebase.auth.FirebaseAuth

class HelperUser {
    companion object {
        private const val TAG = "HelperUser"
        var _ID_USER = ""

        fun getUserId(): String? {
            return FirebaseAuth.getInstance().currentUser?.uid
        }

        fun isUserLoggedIn(): Boolean {
            return FirebaseAuth.getInstance().currentUser != null
        }

        fun getDataUserRefresh(activity: MainActivity, onComplete: (() -> Unit)? = null) {
            val userId = getUserId()
            if (userId.isNullOrEmpty()) {
                Log.w(TAG, "No hay usuario autenticado para cargar perfil.")
                onComplete?.invoke()
                return
            }

            FirebaseUsuarioUtil.getUserData(
                userId,
                onSuccess = { user ->
                    MainActivity._INFO_USER = user
                    Log.d(TAG, "Perfil cargado desde Realtime Database")
                    onComplete?.invoke()
                },
                onFailure = { rtDbError ->
                    Log.w(TAG, "Perfil RTDB no disponible: ${rtDbError.message}")
                    FirestoreDBHelper().getUserDataFromFirestore(
                        userId,
                        onSuccess = { user ->
                            MainActivity._INFO_USER = user
                            Log.d(TAG, "Perfil cargado desde Firestore (legacy)")
                            onComplete?.invoke()
                        },
                        onFailure = {
                            applyAuthFallbackProfile()
                            Log.w(TAG, "Perfil legacy no disponible, usando Firebase Auth")
                            onComplete?.invoke()
                        }
                    )
                }
            )
        }

        private fun applyAuthFallbackProfile() {
            val authUser = FirebaseAuth.getInstance().currentUser
            MainActivity._INFO_USER = User(
                nombreUsuario = authUser?.displayName.orEmpty(),
                correo = authUser?.email.orEmpty(),
                imagenPerfil = authUser?.photoUrl?.toString().orEmpty()
            )
        }
    }
}
