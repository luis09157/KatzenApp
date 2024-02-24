package com.example.katzen.Model

data class MascotaModel(
    var id: String = "",
    var nombre: String = "",
    var peso: String = "",
    var edad: String = "",
    var sexo: String = "",
    var especie: String = "",
    var raza: String = "",
    var color: String = "",
    var idUsuario: String = "",
    var fecha: String = "",

    var imageUrl: String = "",
    var imageFileName: String = ""
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["id"] = id
        map["nombre"] = nombre
        map["peso"] = peso
        map["edad"] = edad
        map["sexo"] = sexo
        map["especie"] = especie
        map["raza"] = raza
        map["color"] = color
        map["id_usuario"] = idUsuario
        map["fecha"] = fecha

        map["imageUrl"] = imageUrl
        map["imageFileName"] = imageFileName
        return map
    }
}
