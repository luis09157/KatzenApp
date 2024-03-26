package com.example.katzen.Helper
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TimePicker
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*

class CalendarioUtil {
    companion object {
        @JvmStatic
        fun mostrarCalendario(context: Context, editTextFecha: TextInputLayout) {
            // Obtener la fecha y hora actual
            val calendario = Calendar.getInstance()
            val anio = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)
            val hora = calendario.get(Calendar.HOUR_OF_DAY)
            val minutos = calendario.get(Calendar.MINUTE)

            // Crear una instancia de DatePickerDialog y configurar la fecha actual como predeterminada
            val datePickerDialog = DatePickerDialog(context, { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                // Cuando se selecciona la fecha, se muestra un TimePickerDialog
                val timePickerDialog = TimePickerDialog(context, { _: TimePicker, hourOfDay: Int, minute: Int ->
                    // Actualizar el EditTextFecha con la fecha y hora seleccionadas
                    val fechaHoraSeleccionada = "$dayOfMonth/${monthOfYear + 1}/$year $hourOfDay:$minute"
                    editTextFecha.editText!!.setText(fechaHoraSeleccionada)
                }, hora, minutos, true) // true para el formato de 24 horas
                timePickerDialog.show()
            }, anio, mes, dia)

            // Mostrar el DatePickerDialog
            datePickerDialog.show()
        }
        fun obtenerFechaHoraActual(): String {
            // Obtener la fecha y hora actual
            val calendario = Calendar.getInstance()
            val formatoFechaHora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            // Formatear la fecha y hora actual como una cadena de texto
            return formatoFechaHora.format(calendario.time)
        }
    }
}
