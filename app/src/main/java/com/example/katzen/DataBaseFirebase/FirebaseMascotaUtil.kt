package com.example.katzen.DataBaseFirebase

import PacienteModel
import android.app.Activity
import android.net.Uri
import com.example.katzen.Config.Config
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.R
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume

class FirebaseMascotaUtil {
    companion object {
        private const val MASCOTAS_PATH = "Katzen/Mascota" // Ruta donde se guardarán las mascotas
        private const val MASCOTAS_IMAGES_PATH = "Mascotas" // Carpeta en Firebase Storage para guardar las imágenes de las mascotas


        @JvmStatic
        fun obtenerListaMascotas(listener: ValueEventListener) {
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val referenciaMascotas: DatabaseReference = database.getReference(MASCOTAS_PATH)
            referenciaMascotas.addValueEventListener(listener)
        }

        suspend fun eliminarMascota(mascotaId: String): Pair<Boolean, String> {
            return suspendCancellableCoroutine { continuation ->
                try {
                    val database = FirebaseDatabase.getInstance()
                    val referenciaMascotas = database.getReference(MASCOTAS_PATH)

                    // Eliminar la mascota de la base de datos
                    referenciaMascotas.child(mascotaId).removeValue()
                        .addOnSuccessListener {
                            continuation.resume(true to "Paciente eliminado exitosamente.")
                        }
                        .addOnFailureListener { exception ->
                            println("Error al eliminar el paciente: ${exception.message}")
                            continuation.resume(false to "Error al eliminar el paciente.")
                        }
                } catch (e: Exception) {
                    println("Excepción atrapada: ${e.message}")
                    continuation.resume(false to "Error al eliminar el paciente.")
                }
            }
        }

        @JvmStatic
        fun obtenerMascota(mascotaId: String, listener: ValueEventListener) {
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val referenciaMascota: DatabaseReference = database.getReference("$MASCOTAS_PATH/$mascotaId")
            referenciaMascota.addListenerForSingleValueEvent(listener)
        }

        @JvmStatic
        fun editarMascota(activity: Activity, mascota: PacienteModel, imagenUri: Uri?) {
            val database = FirebaseDatabase.getInstance()
            val referenciaMascotas = database.getReference(MASCOTAS_PATH)
            mascota.id = Config.MASCOTA_EDIT.id

            if (Config.IMG_CHANGE) {
                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.reference.child(MASCOTAS_IMAGES_PATH)

                // Guardar la imagen en Firebase Storage
                val imagenRef = storageRef.child(UUID.randomUUID().toString()) // Generar un nombre único para la imagen
                val uploadTask = imagenRef.putFile(imagenUri!!)

                // Manejar el resultado de la subida de la imagen
                uploadTask.addOnSuccessListener { _ ->
                    // Obtener la URL de descarga de la imagen
                    imagenRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        mascota.imageUrl = imageUrl // Asignar la URL de la imagen a la mascota
                        actualizarMascotaEnBaseDatos(activity, mascota, referenciaMascotas)
                    }.addOnFailureListener { exception ->
                        manejarErrorSubidaImagen(activity, exception)
                    }
                }.addOnFailureListener { exception ->
                    manejarErrorSubidaImagen(activity, exception)
                }
            } else {
                // Si no hay cambio de imagen, simplemente actualizar la mascota sin subir nueva imagen
                mascota.imageUrl = Config.MASCOTA_EDIT.imageUrl
                actualizarMascotaEnBaseDatos(activity, mascota, referenciaMascotas)
            }
        }

        private fun actualizarMascotaEnBaseDatos(activity: Activity, mascota: PacienteModel, referenciaMascotas: DatabaseReference) {
            referenciaMascotas.child(mascota.id).setValue(mascota)
                .addOnSuccessListener {
                    // Operación exitosa
                    DialogMaterialHelper.mostrarSuccessDialog(activity, activity.getString(R.string.dialog_pet_updated_success))
                    ConfigLoading.hideLoadingAnimation()
                }
                .addOnFailureListener { exception ->
                    // Manejar errores
                    DialogMaterialHelper.mostrarErrorDialog(activity, activity.getString(R.string.dialog_error_updating_pet, exception))
                    ConfigLoading.hideLoadingAnimation()
                }
        }

        private fun manejarErrorSubidaImagen(activity: Activity, exception: Exception) {
            // Manejar errores de la subida de la imagen
            DialogMaterialHelper.mostrarErrorDialog(activity, activity.getString(R.string.dialog_error_uploading_image, exception))
            ConfigLoading.hideLoadingAnimation()
        }
    }
}
