package com.example.katzen.Model

import android.content.Context
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.acos

class ClienteModel(
    var id: String = UUID.randomUUID().toString(),
    var nombre: String = "",
    var apellidoPaterno: String = "",
    var apellidoMaterno: String = "",
    var expediente: String = "",
    var correo: String = "",
    var telefono: String = "",
    var calle: String = "",
    var numero: String = "",
    var colonia: String = "",
    var municipio: String = "",
    var urlGoogleMaps: String = "",
    var kilometrosCasa: String = "",
    var fecha: String = UtilHelper.getDate(),
    var imageUrl: String = "",
    var imageFileName: String = ""
) {
    companion object {
        fun validarCliente(context: Context, cliente: ClienteModel): ValidationResult {

            if(cliente.nombre.isEmpty()){
                return ValidationResult(false, context.getString(R.string.error_empty_nombre))
            }else  if(cliente.expediente.isEmpty()){
                return ValidationResult(false, context.getString(R.string.error_empty_expediente))
            }else if(cliente.telefono.isEmpty()){
                return ValidationResult(false, context.getString(R.string.error_empty_telefono))
            }else if(cliente.calle.isEmpty()){
                return ValidationResult(false, context.getString(R.string.error_empty_calle))
            }else if(cliente.numero.isEmpty()){
                return ValidationResult(false, context.getString(R.string.error_empty_numero))
            }else if(cliente.colonia.isEmpty()){
                return ValidationResult(false, context.getString(R.string.error_empty_colonia))
            }else if(cliente.municipio.isEmpty()){
                return ValidationResult(false, context.getString(R.string.error_empty_municipio))
            }else if(cliente.fecha.isEmpty()){
                return ValidationResult(false, context.getString(R.string.error_empty_fecha))
            }

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

            val phonePattern = "\\d{10}"
            if (!cliente.telefono.matches(phonePattern.toRegex())) {
                return ValidationResult(false, context.getString(R.string.error_invalid_phone))
            }

            /*if(cliente.correo.isEmpty()){
                cliente.correo = context.getString(R.string.error_no_proporcionado)
            }else if(cliente.telefono.isEmpty()){
                cliente.telefono = context.getString(R.string.error_no_proporcionado)
            }else if(cliente.nombre.isEmpty()){
                cliente.nombre = context.getString(R.string.error_no_proporcionado)
            }else if(cliente.apellidoMaterno.isEmpty()){
                cliente.apellidoMaterno = context.getString(R.string.error_no_proporcionado)
            }else if(cliente.apellidoPaterno.isEmpty()){
                cliente.apellidoPaterno = context.getString(R.string.error_no_proporcionado)
            } else if(cliente.urlGoogleMaps.isEmpty()){
                cliente.urlGoogleMaps = context.getString(R.string.error_no_proporcionado)
            }else if(cliente.calle.isEmpty()){
                cliente.calle = context.getString(R.string.error_no_proporcionado)
            }else if(cliente.numero.isEmpty()){
                cliente.numero = context.getString(R.string.error_no_proporcionado)
            }else if(cliente.colonia.isEmpty()){
                cliente.colonia = context.getString(R.string.error_no_proporcionado)
            }else if(cliente.municipio.isEmpty()){
                cliente.municipio = context.getString(R.string.error_no_proporcionado)
            }*/

            if(cliente.correo.isEmpty()){
                cliente.correo = ""
            }else if(cliente.telefono.isEmpty()){
                cliente.telefono = ""
            }else if(cliente.nombre.isEmpty()){
                cliente.nombre = ""
            }else if(cliente.apellidoMaterno.isEmpty()){
                cliente.apellidoMaterno = ""
            }else if(cliente.apellidoPaterno.isEmpty()){
                cliente.apellidoPaterno = ""
            } else if(cliente.urlGoogleMaps.isEmpty()){
                cliente.urlGoogleMaps = ""
            }else if(cliente.calle.isEmpty()){
                cliente.calle = ""
            }else if(cliente.numero.isEmpty()){
                cliente.numero = ""
            }else if(cliente.colonia.isEmpty()){
                cliente.colonia = ""
            }else if(cliente.municipio.isEmpty()){
                cliente.municipio = ""
            }

            return ValidationResult(true)
        }
    }
}
