package com.example.katzen.Helper

import com.google.firebase.database.DataSnapshot

/**
 * Registros activos en RTDB (compatible web + legacy sin campo activo).
 */
object RtdbActiveRecords {

    fun isActive(snapshot: DataSnapshot): Boolean {
        return snapshot.child("activo").getValue(Boolean::class.java) != false
    }

    fun isActive(activo: Boolean?): Boolean = activo != false
}
