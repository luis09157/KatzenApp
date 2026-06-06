package com.example.katzen.DataBaseFirebase

import com.example.katzen.Helper.RtdbSnapshotMapper
import com.example.katzen.Model.PortalCitaModel
import com.example.katzen.Model.PortalHistorialModel
import com.example.katzen.Model.PortalNotificacionModel
import com.example.katzen.Model.VacunaModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object FirebasePortalUtil {
    private val database = FirebaseDatabase.getInstance()

    suspend fun obtenerNotificaciones(clienteId: String): List<PortalNotificacionModel> {
        return readList("Katzen/Notificaciones/$clienteId") { snapshot ->
            snapshot.children.mapNotNull { child ->
                RtdbSnapshotMapper.mapNotificacion(child)
            }.sortedByDescending { it.fecha }
        }
    }

    suspend fun marcarNotificacionLeida(clienteId: String, notificacionId: String) {
        database.getReference("Katzen/Notificaciones/$clienteId/$notificacionId/leida")
            .setValue(true)
            .await()
    }

    suspend fun obtenerVacunasPorMascota(mascotaId: String): List<VacunaModel> {
        return queryByChild("Katzen/Vacunas", "idPaciente", mascotaId) { snapshot ->
            snapshot.children.mapNotNull { child ->
                RtdbSnapshotMapper.mapVacuna(child)
            }
        }
    }

    suspend fun obtenerCitasPorMascota(mascotaId: String): List<PortalCitaModel> {
        return queryByChild("Katzen/Citas", "paciente_id", mascotaId) { snapshot ->
            snapshot.children.mapNotNull { child ->
                RtdbSnapshotMapper.mapCita(child)
            }.sortedByDescending { it.fecha_hora }
        }
    }

    suspend fun obtenerHistorialesPorMascota(mascotaId: String): List<PortalHistorialModel> {
        return queryByChild("Katzen/Historiales_Clinicos", "paciente_id", mascotaId) { snapshot ->
            snapshot.children.mapNotNull { child ->
                RtdbSnapshotMapper.mapHistorial(child)
            }.sortedByDescending { it.fecha_registro }
        }
    }

    private suspend fun <T> queryByChild(
        path: String,
        childKey: String,
        value: String,
        mapper: (DataSnapshot) -> List<T>
    ): List<T> {
        return suspendCancellableCoroutine { continuation ->
            database.getReference(path)
                .orderByChild(childKey)
                .equalTo(value)
                .get()
                .addOnSuccessListener { snapshot ->
                    continuation.resume(mapper(snapshot))
                }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }

    private suspend fun <T> readList(
        path: String,
        mapper: (DataSnapshot) -> List<T>
    ): List<T> {
        return suspendCancellableCoroutine { continuation ->
            database.getReference(path).get()
                .addOnSuccessListener { snapshot ->
                    continuation.resume(mapper(snapshot))
                }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }
}
