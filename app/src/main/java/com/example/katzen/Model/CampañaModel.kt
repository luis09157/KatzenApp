package com.example.katzen.Model

import java.util.UUID
class CampañaModel(
    var id: String = UUID.randomUUID().toString(),
    var mes: String = "",
    var cantidadCampañas: String = ""

) {
    companion object {
    }
}
