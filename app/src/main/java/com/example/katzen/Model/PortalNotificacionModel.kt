package com.example.katzen.Model

data class PortalNotificacionModel(
    var id: String = "",
    var tipo: String = "",
    var mascotaId: String = "",
    var titulo: String = "",
    var mensaje: String = "",
    var leida: Boolean = false,
    var fecha: String = "",
    var referenciaId: String = ""
)
