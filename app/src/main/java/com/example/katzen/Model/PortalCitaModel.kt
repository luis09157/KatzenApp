package com.example.katzen.Model

data class PortalCitaModel(
    var id: String = "",
    var cliente_id: String = "",
    var paciente_id: String = "",
    var fecha_hora: String = "",
    var motivo: String = "",
    var estado: String = "",
    var veterinario: String = "",
    var observaciones: String = "",
    var activo: Boolean = true
)
