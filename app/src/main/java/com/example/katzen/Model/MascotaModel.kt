package com.example.katzen.Model

import android.content.Context
import android.net.Uri
import com.example.katzen.R
import java.text.SimpleDateFormat
import java.util.*

data class MascotaModel(
    var id: String = UUID.randomUUID().toString(),
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
    var imageFileName: String = "",
    var imgData: Uri = Uri.EMPTY
) {
    companion object {
        fun validarMascota(context: Context, mascota: MascotaModel): ValidationResult {
            if (mascota.nombre.isEmpty() || mascota.peso.isEmpty() || mascota.raza.isEmpty() ||
                mascota.especie.isEmpty() || mascota.edad.isEmpty() || mascota.sexo.isEmpty()
                || mascota.fecha.isEmpty() || mascota.imgData == Uri.EMPTY) {
                return ValidationResult(false, context.getString(R.string.error_empty_fields))
            }

            // Validar la fecha
            val fecha: String
            try {
                val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(mascota.fecha)
                if (date.after(Date())) {
                    return ValidationResult(false, context.getString(R.string.error_future_date))
                }
                fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
            } catch (e: Exception) {
                return ValidationResult(false, context.getString(R.string.error_invalid_date))
            }

            // Si todas las validaciones pasan, retornar un resultado válido
            return ValidationResult(true)
        }
    }

}