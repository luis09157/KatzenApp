package com.example.katzen.Model

import com.google.firebase.database.DataSnapshot

data class PacienteCampañaModel(
    val fecha: String = "",
    val id: String = "",
    val idCliente: String = "",
    val idPaciente: String = ""
) {
    companion object {
        fun fromDataSnapshot(snapshot: DataSnapshot): PacienteCampañaModel {
            return PacienteCampañaModel(
                fecha = snapshot.child("fecha").getValue(String::class.java) ?: "",
                id = snapshot.child("id").getValue(String::class.java) ?: "",
                idCliente = snapshot.child("idCliente").getValue(String::class.java) ?: "",
                idPaciente = snapshot.child("idPaciente").getValue(String::class.java) ?: ""
            )
        }

        fun listFromDataSnapshot(snapshot: DataSnapshot): List<PacienteCampañaModel> {
            return snapshot.children.map { child ->
                fromDataSnapshot(child)
            }
        }
    }
}
