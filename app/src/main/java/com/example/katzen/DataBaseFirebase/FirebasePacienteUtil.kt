package com.example.katzen.DataBaseFirebase

import PacienteModel
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebasePacienteUtil {
    companion object {
        private const val MASCOTAS_PATH = "Katzen/Mascota" // Ruta donde se guardarán las mascotas
        private const val MASCOTAS_IMAGES_PATH = "Mascotas" // Carpeta en Firebase Storage para guardar las imágenes de las mascotas
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val referenciaMascota: DatabaseReference = database.getReference(MASCOTAS_PATH)

        @JvmStatic
        fun obtenerListaMascotas(listener: ValueEventListener) {
            referenciaMascota.addValueEventListener(listener)
        }
        suspend fun eliminarMascota(mascotaId: String): Pair<Boolean, String> {
            return suspendCancellableCoroutine { continuation ->
                try {
                    referenciaMascota.child(mascotaId).removeValue()
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
        suspend fun editarMascota(pacienteId: String, nuevoPaciente: PacienteModel): Result<String> {
            return withContext(Dispatchers.IO) {
                suspendCoroutine { continuation ->
                    val mascotaReference = referenciaMascota.child(pacienteId)

                    mascotaReference.setValue(nuevoPaciente) { error, _ ->
                        if (error == null) {
                            // Si no hay errores, la edición fue exitosa
                            continuation.resume(Result.success("Cliente editado correctamente"))
                        } else {
                            // Si hay errores, la edición falló
                            continuation.resumeWithException(error.toException())
                        }
                    }
                }
            }
        }
        suspend fun obtenerPacientesDeCliente(idCliente: String): List<PacienteModel> {
            return suspendCancellableCoroutine { continuation ->
                referenciaMascota.orderByChild("idCliente").equalTo(idCliente)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val pacientes = mutableListOf<PacienteModel>()
                            for (pacienteSnapshot in snapshot.children) {
                                val paciente = pacienteSnapshot.getValue(PacienteModel::class.java)
                                paciente?.let {
                                    pacientes.add(it)
                                }
                            }
                            continuation.resume(pacientes)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            println("Error al obtener la lista de pacientes: ${error.message}")
                            continuation.resumeWithException(error.toException())
                        }
                    })
            }
        }
        suspend fun eliminarPacientesDeCliente(clienteId: String): Boolean {
            return try {
                val pacientes = obtenerPacientesDeCliente(clienteId)
                for (paciente in pacientes) {
                    referenciaMascota.child(paciente.id).removeValue().await()
                }
                true
            } catch (e: Exception) {
                println("Error al eliminar los pacientes del cliente: ${e.message}")
                false
            }
        }


    }
}
