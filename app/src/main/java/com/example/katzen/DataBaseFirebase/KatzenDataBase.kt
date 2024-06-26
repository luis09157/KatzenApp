import android.app.Activity
import android.net.Uri
import android.util.Log
import com.afollestad.materialdialogs.MaterialDialog
import com.example.katzen.Helper.UtilHelper
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class KatzenDataBase(activity: Activity) {
    val activity = activity
    private val TAG = "KatzenDataBase"

    // Referencia a la base de datos
    private val database = FirebaseDatabase.getInstance()
    private val mascotasRef = database.getReference("Katzen/Mascota/paciente")

    // Función para agregar una nueva mascota y paciente a la base de datos
    fun agregarMascota(
        mascota: PacienteModel,
        uriImagen: Uri? = null
    ) {
        val nuevoId = mascotasRef.push().key

        // Crear una nueva referencia utilizando el ID generado
        val nuevoPacienteRef = mascotasRef.child(nuevoId ?: "")
        mascota.id = nuevoId.toString()
        mascota.fecha = UtilHelper.getDate()

        // Si se proporciona una URI de imagen, subirla a Firebase Storage y obtener la URL
        if (uriImagen != null) {
            subirImagenAFirebaseStorage(uriImagen) { urlImagen ->
                mascota.imageUrl = urlImagen
                // Después de obtener la URL de la imagen, agregar la mascota a la base de datos
                //nuevoPacienteRef.setValue(mascota.toMap())
                Log.d(TAG, "Mascota y paciente agregados correctamente.")
            }
        } else {
            // Si no se proporciona una URI de imagen, agregar la mascota directamente a la base de datos
            //nuevoPacienteRef.setValue(mascota.toMap())
            Log.d(TAG, "Mascota y paciente agregados correctamente.")
        }
        
        MaterialDialog(activity)
            .title(text = "Tarea completada!")
            .message(text = "¡Has completado la tarea con éxito!")
            .positiveButton(text = "¡Entendido!") {
                // Acción a realizar al hacer clic en el botón de confirmación
            }
            .show()
    }

    private fun subirImagenAFirebaseStorage(uriImagen: Uri, onComplete: (String) -> Unit) {
        // Genera un nombre de archivo único
        val nombreArchivo = "${System.currentTimeMillis()}_${Uri.parse(uriImagen.toString()).lastPathSegment}"

        // Referencia al Storage
        val storageRef = FirebaseStorage.getInstance().reference.child("imagenes_mascotas/$nombreArchivo")

        // Sube la imagen
        val uploadTask = storageRef.putFile(uriImagen)

        // Obtiene la URL de la imagen después de la subida
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@continueWithTask storageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                // La URL de la imagen está en downloadUri.toString()
                onComplete.invoke(downloadUri.toString())
            } else {
                // Maneja el fallo en la carga de la imagen
                Log.e(TAG, "Error al subir la imagen al Storage.")
            }
        }
    }
}