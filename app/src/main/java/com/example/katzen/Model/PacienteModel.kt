import android.content.Context
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.ValidationResult
import com.example.katzen.R
import java.text.SimpleDateFormat
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
    companion object {

        fun validarMascota(context: Context, mascota: PacienteModel): ValidationResult {
            if (mascota.nombre.isEmpty()  || mascota.raza.isEmpty() ||
                mascota.especie.isEmpty()  || mascota.sexo.isEmpty() ||
                mascota.fecha.isEmpty() || mascota.nombreCliente.isEmpty() || mascota.idCliente.isEmpty()) {
                return ValidationResult(false, context.getString(R.string.error_empty_fields))
            }


            return ValidationResult(true)
        }
    }
}
