package com.example.katzen.DataBaseFirebase

import com.example.katzen.Helper.CalendarioUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FirebaseCampañaUtil {
    companion object {
        private const val CAMPANAS_PATH = "Katzen/Campaña" // Ruta donde se guardan las campañas
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val referenciaCampaña: DatabaseReference = database.getReference(CAMPANAS_PATH)

        @JvmStatic
        fun obtenerListaCampañas(listener: ValueEventListener) {
            referenciaCampaña.addValueEventListener(listener)
        }

        suspend fun contarCampañasPorMes(mes: String): Int {
            return suspendCancellableCoroutine { continuation ->
                referenciaCampaña.child(CalendarioUtil.obtenerAñoActual()).child(mes).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val count = snapshot.childrenCount.toInt()
                        continuation.resume(count)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println("Error al obtener la cantidad de campañas para el mes $mes: ${error.message}")
                        continuation.resume(0) // Retornar cero en caso de error
                    }
                })
            }
        }

        // Otros métodos de utilidad para editar, eliminar, obtener campañas, etc. según tus necesidades
    }
}
