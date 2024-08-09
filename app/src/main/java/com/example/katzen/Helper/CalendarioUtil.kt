package com.example.katzen.Helper
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import com.example.katzen.Fragment.Campaña.AddCampañaFragment
import com.example.katzen.Fragment.Campaña.CampañaFragment
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
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
        fun mostrarCalendarioFecha(context: Context, editTextFecha: TextInputLayout) {
            // Obtener la fecha actual
            val calendario = Calendar.getInstance()
            val anio = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            // Crear una instancia de DatePickerDialog y configurar la fecha actual como predeterminada
            val datePickerDialog = DatePickerDialog(context, { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                // Actualizar el EditTextFecha con la fecha seleccionada
                val fechaSeleccionada = "$dayOfMonth/${monthOfYear + 1}/$year"
                editTextFecha.editText!!.setText(fechaSeleccionada)
            }, anio, mes, dia)

            // Mostrar el DatePickerDialog
            datePickerDialog.show()
        }

        fun mostrarCalendarioFechaCampaña(context: Context, editTextFecha: TextInputLayout) {
            // Obtener el año y mes desde CampañaFragment.ADD_CAMPAÑA
            val anio = CampañaFragment.ADD_CAMPAÑA.año.toInt()
            val mes = CampañaFragment.ADD_CAMPAÑA.mes.toInt() - 1 // Restar 1 si el mes es del 1 al 12
            val dia = 1

            // Crear una instancia de Calendar para establecer los límites
            val inicioMes = Calendar.getInstance().apply {
                set(Calendar.YEAR, anio)
                set(Calendar.MONTH, mes)
                set(Calendar.DAY_OF_MONTH, 1)
            }

            val finMes = Calendar.getInstance().apply {
                set(Calendar.YEAR, anio)
                set(Calendar.MONTH, mes)
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH)) // Último día del mes
            }

            // Crear una instancia de DatePickerDialog y configurar la fecha actual como predeterminada
            val datePickerDialog = DatePickerDialog(context, { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                // Formatear el mes para agregar un 0 a la izquierda si está en el rango de 1-9
                val mesFormateado = if (monthOfYear + 1 < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"
                // Actualizar el EditTextFecha con la fecha seleccionada
                val fechaSeleccionada = "$dayOfMonth/$mesFormateado/$year"
                CampañaFragment.ADD_CAMPAÑA.fecha = fechaSeleccionada
                editTextFecha.editText!!.setText(fechaSeleccionada)
            }, anio, mes, dia)

            // Establecer los límites en el DatePickerDialog
            datePickerDialog.datePicker.minDate = inicioMes.timeInMillis
            datePickerDialog.datePicker.maxDate = finMes.timeInMillis

            // Mostrar el DatePickerDialog
            datePickerDialog.show()
        }







        fun calcularEdadMascota(fechaNacimiento: String): Pair<Int, Int> {
            // Formato de fecha esperado
            val formato = SimpleDateFormat("dd/MM/yyyy")

            // Convertir la fecha de nacimiento de la mascota a Date
            val fechaNacimientoMascota: Date = formato.parse(fechaNacimiento)

            // Obtener la fecha actual
            val calendarioActual = Calendar.getInstance()
            val fechaActual: Date = calendarioActual.time

            // Calcular la diferencia entre las fechas
            val diferenciaMillis: Long = fechaActual.time - fechaNacimientoMascota.time
            val diferenciaDias = diferenciaMillis / (1000 * 60 * 60 * 24)
            val anios = (diferenciaDias / 365).toInt()
            val meses = ((diferenciaDias % 365) / 30).toInt()

            return Pair(anios, meses)
        }
        fun obtenerFechaHoraActual(): String {
            // Obtener la fecha y hora actual
            val calendario = Calendar.getInstance()
            val formatoFechaHora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            // Formatear la fecha y hora actual como una cadena de texto
            return formatoFechaHora.format(calendario.time)
        }

        fun obtenerFechaHoraActualCampaña(): String {
            // Obtener la fecha y hora actual
            val calendario = Calendar.getInstance()

            // Establecer el día en 1
            calendario.set(Calendar.DAY_OF_MONTH, 1)

            // Establecer el mes y el año según los parámetros pasados
            calendario.set(Calendar.MONTH, CampañaFragment.ADD_CAMPAÑA.mes.toInt() - 1)  // Restar 1 porque los meses en Calendar van de 0 a 11
            calendario.set(Calendar.YEAR, CampañaFragment.ADD_CAMPAÑA.año.toInt())

            // Formatear la fecha y hora actual como una cadena de texto
            val formatoFechaHora = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return formatoFechaHora.format(calendario.time)
        }

        fun obtenerAñoActual(): String {
            // Obtener la fecha y hora actual
            val calendario = Calendar.getInstance()
            val formatoFechaHora = SimpleDateFormat("yyyy", Locale.getDefault())

            // Formatear la fecha y hora actual como una cadena de texto
            return formatoFechaHora.format(calendario.time)
        }
        fun obtenerDiaDesdeString(fechaString: String): String {
            return try {
                val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val fecha = formato.parse(fechaString)

                if (fecha != null) {
                    val calendario = Calendar.getInstance()
                    calendario.time = fecha

                    // Obtener el día del mes
                    calendario.get(Calendar.DAY_OF_MONTH).toString()
                } else {
                    "0" // Retorna "0" si la fecha es null
                }
            } catch (e: Exception) {
                println("Error al obtener el día desde el string de fecha: ${e.message}")
                "0" // En caso de error, retorna "0"
            }
        }

    }
}
