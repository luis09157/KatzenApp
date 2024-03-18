import android.content.Context
import android.net.Uri
import android.widget.EditText
import android.widget.Toast
import com.example.katzen.Model.Producto
import com.example.katzen.R
import java.text.SimpleDateFormat
import java.util.*

class ValidadorProducto {

    companion object {
        fun validarYCrearProducto(context: Context, editTextNombre: EditText, editTextPrecioVenta: EditText, editTextCosto: EditText, editTextFecha: EditText, imagenUri: Uri?): Producto? {
            val nombre = editTextNombre.text.toString()
            val precioVentaString = editTextPrecioVenta.text.toString()
            val costoString = editTextCosto.text.toString()
            val fechaString = editTextFecha.text.toString()

            // Validar que los campos no estén vacíos
            if (nombre.isEmpty() || precioVentaString.isEmpty() || costoString.isEmpty() || fechaString.isEmpty()) {
                mostrarError(context, R.string.error_empty_fields)
                return null
            }

            // Validar que los valores numéricos sean válidos
            val precioVenta = precioVentaString.toDoubleOrNull()
            val costo = costoString.toDoubleOrNull()

            if (precioVenta == null || costo == null) {
                mostrarError(context, R.string.error_invalid_number)
                return null
            }

            // Validar y convertir la fecha a un formato válido
            val fecha: String
            try {
                val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fechaString)
                if (date.after(Date())) {
                    mostrarError(context, R.string.error_future_date)
                    return null
                }
                fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            } catch (e: Exception) {
                mostrarError(context, R.string.error_invalid_date)
                return null
            }

            // Validar la imagen
            val rutaImagen = imagenUri?.toString() ?: ""
            if (rutaImagen.isEmpty()) {
                mostrarError(context, R.string.error_no_image_selected)
                return null
            }

            // Crear y devolver el objeto Producto
            return Producto(nombre = nombre, precioVenta = precioVenta, costo = costo, ganancia = precioVenta - costo, fecha = fecha, rutaImagen = rutaImagen)
        }

        private fun mostrarError(context: Context, mensajeResId: Int) {
            Toast.makeText(context, context.getString(mensajeResId), Toast.LENGTH_SHORT).show()
        }
    }
}
