package com.example.katzen.Model

import android.content.Context
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
class ClienteModel(
    var id: String = UUID.randomUUID().toString(),
    var nombre: String = "",
    var apellidoPaterno: String = "",
    var apellidoMaterno: String = "",
    var correo: String = "",
    var telefono: String = "",
    var calle: String = "",
    var numero: String = "",
    var colonia: String = "",
    var municipio: String = "",
    var fecha: String = UtilHelper.getDate(),
    var imageUrl: String = "",
    var imageFileName: String = ""
) {
    companion object {
        fun validarCliente(context: Context, cliente: ClienteModel): ValidationResult {
            if (cliente.nombre.isEmpty() || cliente.apellidoPaterno.isEmpty() ||
                cliente.correo.isEmpty() || cliente.telefono.isEmpty() ||
                cliente.calle.isEmpty() || cliente.numero.isEmpty() ||
                cliente.colonia.isEmpty() || cliente.municipio.isEmpty() ||
                cliente.fecha.isEmpty()) {
                return ValidationResult(false, context.getString(R.string.error_empty_fields))
            }

            // Validar la fecha
            val fecha: String
            try {
                val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(cliente.fecha)
                if (date.after(Date())) {
                    return ValidationResult(false, context.getString(R.string.error_future_date))
                }
                fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
            } catch (e: Exception) {
                return ValidationResult(false, context.getString(R.string.error_invalid_date))
            }

            // Validar el formato de correo electrónico
            val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
            if (!cliente.correo.matches(emailPattern.toRegex())) {
                return ValidationResult(false, context.getString(R.string.error_invalid_email))
            }

            // Validar el formato de número de teléfono
            val phonePattern = "\\d{10}"
            if (!cliente.telefono.matches(phonePattern.toRegex())) {
                return ValidationResult(false, context.getString(R.string.error_invalid_phone))
            }

            // Si todas las validaciones pasan, retornar un resultado válido
            return ValidationResult(true)
        }
    }
}
