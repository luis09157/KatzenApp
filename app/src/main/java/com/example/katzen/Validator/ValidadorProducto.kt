import android.content.Context
import android.net.Uri
import android.widget.EditText
import android.widget.Toast
import com.example.katzen.Config.Config
import com.example.katzen.Model.ProductoModel
import com.example.katzen.R
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*

class ValidadorProducto {

    companion object {
        fun validarYCrearProducto(
            context: Context,
            editTextNombre: TextInputLayout,
            editTextPrecioVenta: TextInputLayout,
            editTextCosto: TextInputLayout,
            editTextFecha: TextInputLayout,
            editTextDescripcion: EditText, // Nuevo campo para la descripción
            imagenUri: Uri?
        ): ProductoModel? {
            val nombre = editTextNombre.editText!!.text.toString()
            val precioVentaString = editTextPrecioVenta.editText!!.text.toString()
            val costoString = editTextCosto.editText!!.text.toString()
            val fechaString = editTextFecha.editText!!.text.toString()
            val descripcion = editTextDescripcion.text.toString() // Obtener el texto de la descripción

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
                val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).parse(fechaString)
                if (date.after(Date())) {
                    mostrarError(context, R.string.error_future_date)
                    return null
                }
                fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date)
            } catch (e: Exception) {
                mostrarError(context, R.string.error_invalid_date)
                return null
            }

            // Validar la imagen
            val rutaImagen = imagenUri?.toString() ?: ""
            if (rutaImagen.isEmpty()) {
                if(Config.PRODUCTO_EDIT.rutaImagen == ""){
                    mostrarError(context, R.string.error_no_image_selected)
                    return null
                }

            }

            // Crear y devolver el objeto Producto con la descripción incluida
            return ProductoModel(
                nombre = nombre,
                precioVenta = precioVenta,
                costo = costo,
                ganancia = precioVenta - costo,
                fecha = fecha,
                rutaImagen = rutaImagen,
                descripcion = descripcion // Agregar la descripción al objeto Producto
            )
        }

        private fun mostrarError(context: Context, mensajeResId: Int) {
            Toast.makeText(context, context.getString(mensajeResId), Toast.LENGTH_SHORT).show()
        }
    }
}
