package com.example.katzen.Model

import com.google.firebase.database.DataSnapshot

data class PacienteCampañaModel(
    val fecha: String = "",
    val id: String? = null,  // Hacerlo nullable
    val idCliente: String = "",
    val idPaciente: String = ""
) {
    // Constructor vacío explícito necesario para Firebase
    constructor() : this(
        fecha = "",
        id = null,  // Valor predeterminado como null
        idCliente = "",
        idPaciente = ""
    )

    companion object {
        fun fromDataSnapshot(snapshot: DataSnapshot): PacienteCampañaModel {
            return PacienteCampañaModel(
                fecha = snapshot.child("fecha").getValue(String::class.java) ?: "",
                id = snapshot.child("id").getValue(String::class.java), // Permitir que id sea null
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
