package com.example.katzen.Helper
import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import android.widget.EditText
import java.util.*

class CalendarioUtil {
    companion object {
        @JvmStatic
        fun mostrarCalendario(context: Context, editTextFecha: EditText) {
            // Obtener la fecha actual
            val calendario = Calendar.getInstance()
            val anio = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            // Crear una instancia de DatePickerDialog y configurar la fecha actual como predeterminada
            val datePickerDialog = DatePickerDialog(context, { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                // Actualizar el EditTextFecha con la fecha seleccionada
                val fechaSeleccionada = "$dayOfMonth/${monthOfYear + 1}/$year"
                editTextFecha.setText(fechaSeleccionada)
            }, anio, mes, dia)

            // Mostrar el DatePickerDialog
            datePickerDialog.show()
        }
    }
}
