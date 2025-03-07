package com.example.katzen.Model

import java.util.UUID

class CampañaModel(
    var id: String = UUID.randomUUID().toString(),
    var nombreCampaña: String = "",
    var cantidadCampañas: String = "",
    var cantidadPacientes: String = "",
    var idCliente: String = "",
    var nombreCliente: String = "",
    var idPaciente: String = "",
    var nombrePaciente: String = "",
    var año: String = "",
    var mes: String = "",
    var dia: String = "",
    var fecha: String = ""
) {
    // Constructor vacío necesario para Firebase
    constructor() : this(
        id = UUID.randomUUID().toString(),
        nombreCampaña = "",
        cantidadCampañas = "",
        cantidadPacientes = "",
        idCliente = "",
        nombreCliente = "",
        idPaciente = "",
        nombrePaciente = "",
        año = "",
        mes = "",
        dia = "",
        fecha = ""
    )
}
