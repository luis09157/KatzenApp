package com.example.katzen.DataBaseFirebase

import com.example.katzen.Helper.ImageLoaderHelper
import com.example.katzen.Helper.RtdbActiveRecords
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.PacienteModel
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebasePacienteUtil {
    companion object {
        private const val MASCOTAS_PATH = "Katzen/Mascota"
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val referenciaMascota: DatabaseReference = database.getReference(MASCOTAS_PATH)

        @JvmStatic
        fun obtenerListaMascotas(listener: ValueEventListener) {
            referenciaMascota.addValueEventListener(listener)
        }

        @JvmStatic
        fun obtenerListaMascotasUnaVez(listener: ValueEventListener) {
            referenciaMascota.addListenerForSingleValueEvent(listener)
        }

        @JvmStatic
        fun removerListenerMascotas(listener: ValueEventListener) {
            referenciaMascota.removeEventListener(listener)
        }

        fun parsePaciente(snapshot: DataSnapshot): PacienteModel? {
            if (!snapshot.exists()) return null
            val paciente = snapshot.getValue(PacienteModel::class.java) ?: return null
            val resolvedImage = ImageLoaderHelper.resolveProfileImage(
                imageUrl = paciente.imageUrl,
                imageFileName = paciente.imageFileName,
                snapshot = snapshot
            )
            paciente.imageUrl = resolvedImage.imageUrl
            paciente.imageFileName = resolvedImage.imageFileName
            if (paciente.id.isBlank()) paciente.id = snapshot.key.orEmpty()
            if (!snapshot.hasChild("activo")) paciente.activo = true
            return paciente
        }

        suspend fun desactivarMascota(mascotaId: String): Pair<Boolean, String> {
            return suspendCancellableCoroutine { continuation ->
                val updates = mapOf(
                    "activo" to false,
                    "fechaBaja" to UtilHelper.getDate()
                )
                referenciaMascota.child(mascotaId).updateChildren(updates)
                    .addOnSuccessListener {
                        continuation.resume(true to "Mascota desactivada correctamente.")
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(false to "Error al desactivar la mascota: ${exception.message}")
                    }
            }
        }

        @Deprecated("Usa desactivarMascota", ReplaceWith("desactivarMascota(mascotaId)"))
        suspend fun eliminarMascota(mascotaId: String): Pair<Boolean, String> = desactivarMascota(mascotaId)

        suspend fun editarMascota(pacienteId: String, nuevoPaciente: PacienteModel): Result<String> {
            return withContext(Dispatchers.IO) {
                suspendCancellableCoroutine { continuation ->
                    referenciaMascota.child(pacienteId).setValue(nuevoPaciente)
                        .addOnSuccessListener {
                            continuation.resume(Result.success("Paciente editado correctamente"))
                        }
                        .addOnFailureListener { exception ->
                            continuation.resumeWithException(exception)
                        }
                }
            }
        }

        suspend fun obtenerPacientesDeCliente(idCliente: String, soloActivos: Boolean = true): List<PacienteModel> {
            return suspendCancellableCoroutine { continuation ->
                referenciaMascota.orderByChild("idCliente").equalTo(idCliente)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val pacientes = snapshot.children.mapNotNull { child ->
                                parsePaciente(child)?.takeIf {
                                    !soloActivos || RtdbActiveRecords.isActive(it.activo)
                                }
                            }
                            continuation.resume(pacientes)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            continuation.resumeWithException(error.toException())
                        }
                    })
            }
        }

        @Deprecated("No borrar mascotas; usa desactivarMascota por registro")
        suspend fun eliminarPacientesDeCliente(clienteId: String): Boolean {
            return try {
                val pacientes = obtenerPacientesDeCliente(clienteId, soloActivos = false)
                for (paciente in pacientes) {
                    desactivarMascota(paciente.id)
                }
                true
            } catch (_: Exception) {
                false
            }
        }

        suspend fun obtenerPacientePorId(idPaciente: String): PacienteModel? {
            if (idPaciente.isBlank()) return null
            return suspendCancellableCoroutine { continuation ->
                referenciaMascota.child(idPaciente)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            continuation.resume(parsePaciente(snapshot))
                        }

                        override fun onCancelled(error: DatabaseError) {
                            continuation.resumeWithException(error.toException())
                        }
                    })
            }
        }

        suspend fun reactivarMascota(mascotaId: String): Pair<Boolean, String> {
            return suspendCancellableCoroutine { continuation ->
                val updates = mapOf(
                    "activo" to true,
                    "fechaBaja" to ""
                )
                referenciaMascota.child(mascotaId).updateChildren(updates)
                    .addOnSuccessListener {
                        continuation.resume(true to "Mascota reactivada correctamente.")
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(false to "Error al reactivar: ${exception.message}")
                    }
            }
        }

        suspend fun obtenerMascotasInactivas(): List<PacienteModel> {
            return suspendCancellableCoroutine { continuation ->
                referenciaMascota.orderByChild("activo").equalTo(false)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        continuation.resume(snapshot.children.mapNotNull { parsePaciente(it) })
                    }
                    .addOnFailureListener { continuation.resumeWithException(it) }
            }
        }
    }
}
