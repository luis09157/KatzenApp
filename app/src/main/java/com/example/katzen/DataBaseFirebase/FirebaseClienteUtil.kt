package com.example.katzen.DataBaseFirebase

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FirebaseClienteUtil {
    companion object {
        private const val CLIENTES_PATH = "Katzen/Cliente"
        private const val CLIENTES_IMAGES_PATH = "Clientes"

        fun obtenerListaClientes(listener: ValueEventListener) {
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val referenciaMascotas: DatabaseReference = database.getReference(CLIENTES_PATH)
            referenciaMascotas.addValueEventListener(listener)
        }
    }
}