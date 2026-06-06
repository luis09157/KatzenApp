package com.example.katzen.Model

data class User(
    val nombreUsuario: String = "",
    val correo: String = "",
    var imagenPerfil: String = "",
    val role: String = "",
    val staffRole: String = ""
) {
    fun isStaff(): Boolean = role.isBlank() || role == "staff"
    fun isClient(): Boolean = role == "client"
}
