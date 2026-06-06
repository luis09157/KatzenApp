package com.example.katzen.Helper

import android.content.Context
import com.example.katzen.Config.ConfigLoading
import com.google.firebase.database.DatabaseError
import com.ninodev.katzen.R

object FirebaseUiHelper {

    fun handleLoadError(
        context: Context,
        error: DatabaseError,
        retryAction: (() -> Unit)? = null
    ) {
        val message = context.getString(R.string.error_firebase_load, error.message)
        ConfigLoading.showError(message, retryAction)
    }

    fun handleLoadError(context: Context, throwable: Throwable, retryAction: (() -> Unit)? = null) {
        val message = context.getString(
            R.string.error_generic_load,
            throwable.localizedMessage ?: context.getString(R.string.error_unknown)
        )
        ConfigLoading.showError(message, retryAction)
    }

    fun parsePositiveDouble(raw: String, onInvalid: () -> Unit): Double? {
        val value = raw.trim().replace(",", ".").toDoubleOrNull()
        if (value == null || value <= 0) {
            onInvalid()
            return null
        }
        return value
    }
}
