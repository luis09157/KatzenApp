package com.example.katzen.DataBaseFirebase

import com.example.katzen.Helper.UtilHelper
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FirebaseViajesUtil {
    companion object {
        private const val VIAJES_PATH = "Katzen/Gasolina"
        private const val VIAJES_IMAGES_PATH = "Viajes"
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val referenciaViaje: DatabaseReference = database.getReference(VIAJES_PATH)
        fun obtenerListaViajes(listener: ValueEventListener) {
            referenciaViaje.child(UtilHelper.getDateYear()).addValueEventListener(listener)
        }
        fun obtenerListaCargosViajes(mes: String, listener: ValueEventListener) {
            val referenciaViajesCargos: DatabaseReference = database.getReference(VIAJES_PATH)
                .child(UtilHelper.getDateYear())
                .child(mes)
                .child("cargos")

            referenciaViajesCargos.addValueEventListener(listener)
        }

    }
}