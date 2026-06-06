package com.example.katzen.Model

data class PortalHistorialModel(
    var id: String = "",
    var paciente_id: String = "",
    var fecha_registro: String = "",
    var diagnostico: String = "",
    var tratamiento: String = "",
    var medicamentos: String = "",
    var notas: String = "",
    var medico_atendio: String = "",
    var historia_clinica: String = "",
    var hallazgos: String = "",
    var activo: Boolean = true
)
