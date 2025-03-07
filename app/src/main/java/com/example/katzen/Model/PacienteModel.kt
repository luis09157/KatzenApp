import android.content.Context
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.ValidationResult
import com.ninodev.katzen.R
import java.util.*

data class PacienteModel(
    var id: String = UUID.randomUUID().toString(),
    var nombre: String = "",
    var peso: String = "",
    var edad: String = "",
    var sexo: String = "",
    var especie: String = "",
    var raza: String = "",
    var color: String = "",
    var nombreCliente: String = "",
    var idCliente: String = "",
    var fecha: String = UtilHelper.getDate(),
    var imageUrl: String = "",
    var imageFileName: String = ""
) {
    constructor() : this("", "", "", "", "", "", "", "", "", "", "", "", "")

    companion object {
        fun validarMascota(context: Context, mascota: PacienteModel): ValidationResult {
            // 1️⃣ Validar campos obligatorios
            if (mascota.nombre.isEmpty() || mascota.raza.isEmpty() ||
                mascota.especie.isEmpty() || mascota.sexo.isEmpty() ||
                mascota.fecha.isEmpty() || mascota.nombreCliente.isEmpty() || mascota.idCliente.isEmpty()) {
                return ValidationResult(false, context.getString(R.string.error_empty_fields))
            }

            // 2️⃣ Validar edad (solo números y rango razonable)
            val edadValida = mascota.edad.toIntOrNull()
            if (edadValida == null || edadValida < 0 || edadValida > 30) {
                return ValidationResult(false, context.getString(R.string.error_invalid_age))
            }

            // 3️⃣ Validar peso (solo números y en un rango lógico)
            val pesoValido = mascota.peso.toDoubleOrNull()
            if (pesoValido == null || pesoValido < 0.1 || pesoValido > 200) {
                return ValidationResult(false, context.getString(R.string.error_invalid_weight))
            }

            // 4️⃣ Validar que la fecha no sea en el futuro
            try {
                val fechaRegistro = UtilHelper.parseDate(mascota.fecha)
                if (fechaRegistro!!.after(Date())) {
                    return ValidationResult(false, context.getString(R.string.error_future_date))
                }
            } catch (e: Exception) {
                return ValidationResult(false, context.getString(R.string.error_invalid_date))
            }

            // 5️⃣ Validar ID de cliente
            if (mascota.idCliente.length < 5) {
                return ValidationResult(false, context.getString(R.string.error_invalid_id_cliente))
            }

            return ValidationResult(true)
        }
    }
}
