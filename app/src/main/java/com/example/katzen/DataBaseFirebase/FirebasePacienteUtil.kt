package com.example.katzen.DataBaseFirebase

import PacienteModel
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebasePacienteUtil {
    companion object {
        private const val MASCOTAS_PATH = "Katzen/Mascota" // Ruta donde se guardarán las mascotas
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val referenciaMascota: DatabaseReference = database.getReference(MASCOTAS_PATH)

        @JvmStatic
        fun obtenerListaMascotas(listener: ValueEventListener) {
            referenciaMascota.addValueEventListener(listener)
        }
        suspend fun eliminarMascota(mascotaId: String): Pair<Boolean, String> {
            return suspendCancellableCoroutine { continuation ->
                referenciaMascota.child(mascotaId).removeValue()
                    .addOnSuccessListener {
                        continuation.resume(true to "Paciente eliminado exitosamente.")
                    }
                    .addOnFailureListener { exception ->
                        println("Error al eliminar el paciente: ${exception.message}")
                        continuation.resume(false to "Error al eliminar el paciente.")
                    }
            }
        }
        suspend fun editarMascota(pacienteId: String, nuevoPaciente: PacienteModel): Result<String> {
            return withContext(Dispatchers.IO) {
                suspendCancellableCoroutine { continuation ->
                    referenciaMascota.child(pacienteId).setValue(nuevoPaciente)
                        .addOnSuccessListener {
                            continuation.resume(Result.success("Paciente editado correctamente"))
                        }
                        .addOnFailureListener { exception ->
                            println("Error al editar el paciente: ${exception.message}")
                            continuation.resumeWithException(exception)
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
                                paciente?.let { pacientes.add(it) }
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
        suspend fun obtenerPacientePorId(idPaciente: String): PacienteModel? {
            return suspendCancellableCoroutine { continuation ->
                referenciaMascota.orderByChild("id").equalTo(idPaciente)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                for (dataSnapshot in snapshot.children) {
                                    val paciente = dataSnapshot.getValue(PacienteModel::class.java)
                                    if (paciente != null) {
                                        // Retornar el paciente encontrado
                                        continuation.resume(paciente)
                                        return
                                    }
                                }
                            } else {
                                // Retornar null si no se encontró ningún paciente
                                continuation.resume(null)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            continuation.resumeWithException(error.toException())
                        }
                    })
            }
        }
    }
}
