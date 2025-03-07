package com.example.katzen.Model

import android.content.Context
import com.example.katzen.Helper.UtilHelper
import com.ninodev.katzen.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ClienteModel() { // Constructor vacío requerido por Firebase
    var id: String = UUID.randomUUID().toString()
    var nombre: String = ""
    var apellidoPaterno: String = ""
    var apellidoMaterno: String = ""
    var expediente: String = ""
    var correo: String = ""
    var telefono: String = ""
    var calle: String = ""
    var numero: String = ""
    var colonia: String = ""
    var municipio: String = ""
    var urlGoogleMaps: String = ""
    var kilometrosCasa: String = ""
    var fecha: String = UtilHelper.getDate()
    var imageUrl: String = ""
    var imageFileName: String = ""

    // Constructor principal con parámetros
    constructor(
        id: String,
        nombre: String,
        apellidoPaterno: String,
        apellidoMaterno: String,
        expediente: String,
        correo: String,
        telefono: String,
        calle: String,
        numero: String,
        colonia: String,
        municipio: String,
        urlGoogleMaps: String,
        kilometrosCasa: String,
        fecha: String,
        imageUrl: String,
        imageFileName: String
    ) : this() { // Llamada al constructor vacío
        this.id = id
        this.nombre = nombre
        this.apellidoPaterno = apellidoPaterno
        this.apellidoMaterno = apellidoMaterno
        this.expediente = expediente
        this.correo = correo
        this.telefono = telefono
        this.calle = calle
        this.numero = numero
        this.colonia = colonia
        this.municipio = municipio
        this.urlGoogleMaps = urlGoogleMaps
        this.kilometrosCasa = kilometrosCasa
        this.fecha = fecha
        this.imageUrl = imageUrl
        this.imageFileName = imageFileName
    }

    companion object {
        fun validarCliente(context: Context, cliente: ClienteModel): ValidationResult {
            if (cliente.nombre.isEmpty()) {
                return ValidationResult(false, context.getString(R.string.error_empty_nombre))
            } else if (cliente.expediente.isEmpty()) {
                return ValidationResult(false, context.getString(R.string.error_empty_expediente))
            } else if (cliente.telefono.isEmpty()) {
                return ValidationResult(false, context.getString(R.string.error_empty_telefono))
            } else if (cliente.calle.isEmpty()) {
                return ValidationResult(false, context.getString(R.string.error_empty_calle))
            } else if (cliente.numero.isEmpty()) {
                return ValidationResult(false, context.getString(R.string.error_empty_numero))
            } else if (cliente.colonia.isEmpty()) {
                return ValidationResult(false, context.getString(R.string.error_empty_colonia))
            } else if (cliente.municipio.isEmpty()) {
                return ValidationResult(false, context.getString(R.string.error_empty_municipio))
            } else if (cliente.fecha.isEmpty()) {
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

            if (cliente.correo.isEmpty()) {
                cliente.correo = ""
            } else if (cliente.telefono.isEmpty()) {
                cliente.telefono = ""
            } else if (cliente.nombre.isEmpty()) {
                cliente.nombre = ""
            } else if (cliente.apellidoMaterno.isEmpty()) {
                cliente.apellidoMaterno = ""
            } else if (cliente.apellidoPaterno.isEmpty()) {
                cliente.apellidoPaterno = ""
            } else if (cliente.urlGoogleMaps.isEmpty()) {
                cliente.urlGoogleMaps = ""
            } else if (cliente.calle.isEmpty()) {
                cliente.calle = ""
            } else if (cliente.numero.isEmpty()) {
                cliente.numero = ""
            } else if (cliente.colonia.isEmpty()) {
                cliente.colonia = ""
            } else if (cliente.municipio.isEmpty()) {
                cliente.municipio = ""
            }

            return ValidationResult(true)
        }
    }
}
