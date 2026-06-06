package com.example.katzen.Helper

import com.example.katzen.Model.PortalCitaModel
import com.example.katzen.Model.PortalHistorialModel
import com.example.katzen.Model.PortalNotificacionModel
import com.example.katzen.Model.VacunaModel
import com.google.firebase.database.DataSnapshot

/**
 * Mapeo de nodos RTDB legacy (Android), web admin y portal.
 * No modifica la base de datos: solo normaliza lecturas en la app.
 */
object RtdbSnapshotMapper {

    fun firstNonBlank(vararg values: String?): String {
        return values.firstOrNull { !it.isNullOrBlank() }.orEmpty()
    }

    fun isActiveRecord(snapshot: DataSnapshot): Boolean {
        return snapshot.child("activo").getValue(Boolean::class.java) != false
    }

    /**
     * Historial clínico: el web usa diagnostico_presuntivo, manejo_terapeutico, etc.
     * Muchos registros legacy tienen activo=false pero siguen siendo válidos para el cliente.
     */
    fun isVisibleInClientPortal(snapshot: DataSnapshot): Boolean {
        val ocultoPortal = snapshot.child("ocultoPortal").getValue(Boolean::class.java)
            ?: snapshot.child("oculto_portal").getValue(Boolean::class.java)
        return ocultoPortal != true
    }

    fun mapHistorial(snapshot: DataSnapshot): PortalHistorialModel? {
        if (!isVisibleInClientPortal(snapshot)) return null

        val diagnostico = firstNonBlank(
            snapshot.child("diagnostico").getValue(String::class.java),
            snapshot.child("diagnostico_presuntivo").getValue(String::class.java),
            snapshot.child("titulo").getValue(String::class.java)
        )
        val tratamiento = firstNonBlank(
            snapshot.child("tratamiento").getValue(String::class.java),
            snapshot.child("manejo_terapeutico").getValue(String::class.java),
            snapshot.child("receta").getValue(String::class.java)
        )
        val medicamentos = firstNonBlank(
            snapshot.child("medicamentos").getValue(String::class.java),
            snapshot.child("receta").getValue(String::class.java)
        )
        val notas = buildHistorialNotas(snapshot)
        val fecha = firstNonBlank(
            snapshot.child("fecha_registro").getValue(String::class.java),
            snapshot.child("created_at").getValue(String::class.java),
            snapshot.child("fecha").getValue(String::class.java)
        )

        return PortalHistorialModel(
            id = snapshot.key.orEmpty(),
            paciente_id = firstNonBlank(
                snapshot.child("paciente_id").getValue(String::class.java),
                snapshot.child("idPaciente").getValue(String::class.java)
            ),
            fecha_registro = fecha,
            diagnostico = diagnostico,
            tratamiento = tratamiento,
            medicamentos = medicamentos,
            notas = notas,
            medico_atendio = snapshot.child("medico_atendio").getValue(String::class.java).orEmpty(),
            historia_clinica = snapshot.child("historia_clinica").getValue(String::class.java).orEmpty(),
            hallazgos = snapshot.child("hallazgos").getValue(String::class.java).orEmpty(),
            activo = snapshot.child("activo").getValue(Boolean::class.java) ?: true
        )
    }

    private fun buildHistorialNotas(snapshot: DataSnapshot): String {
        val explicit = snapshot.child("notas").getValue(String::class.java).orEmpty()
        if (explicit.isNotBlank()) return explicit

        return listOfNotNull(
            snapshot.child("historia_clinica").getValue(String::class.java)?.takeIf { it.isNotBlank() }
                ?.let { "Historia: $it" },
            snapshot.child("hallazgos").getValue(String::class.java)?.takeIf { it.isNotBlank() }
                ?.let { "Hallazgos: $it" },
            snapshot.child("estudios_solicitados").getValue(String::class.java)?.takeIf { it.isNotBlank() }
                ?.let { "Estudios: $it" },
            snapshot.child("medico_atendio").getValue(String::class.java)?.takeIf { it.isNotBlank() }
                ?.let { "Médico: $it" }
        ).joinToString("\n")
    }

    fun mapCita(snapshot: DataSnapshot): PortalCitaModel? {
        if (!isActiveRecord(snapshot)) return null

        val fechaHora = firstNonBlank(
            snapshot.child("fecha_hora").getValue(String::class.java),
            snapshot.child("fecha").getValue(String::class.java)
        )
        val hora = snapshot.child("hora").getValue(String::class.java).orEmpty()
        val fechaDisplay = if (fechaHora.isNotBlank() && hora.isNotBlank() && !fechaHora.contains(hora)) {
            "$fechaHora $hora"
        } else {
            fechaHora
        }

        return PortalCitaModel(
            id = snapshot.key.orEmpty(),
            cliente_id = firstNonBlank(
                snapshot.child("cliente_id").getValue(String::class.java),
                snapshot.child("idCliente").getValue(String::class.java)
            ),
            paciente_id = firstNonBlank(
                snapshot.child("paciente_id").getValue(String::class.java),
                snapshot.child("idPaciente").getValue(String::class.java)
            ),
            fecha_hora = fechaDisplay,
            motivo = firstNonBlank(
                snapshot.child("motivo").getValue(String::class.java),
                snapshot.child("titulo").getValue(String::class.java)
            ),
            estado = snapshot.child("estado").getValue(String::class.java).orEmpty(),
            veterinario = firstNonBlank(
                snapshot.child("veterinario").getValue(String::class.java),
                snapshot.child("medico_atendio").getValue(String::class.java)
            ),
            observaciones = snapshot.child("observaciones").getValue(String::class.java).orEmpty(),
            activo = snapshot.child("activo").getValue(Boolean::class.java) ?: true
        )
    }

    fun mapVacuna(snapshot: DataSnapshot): VacunaModel? {
        if (!isActiveRecord(snapshot)) return null

        val model = snapshot.getValue(VacunaModel::class.java) ?: VacunaModel()
        if (model.id.isBlank()) model.id = snapshot.key.orEmpty()
        if (model.idPaciente.isBlank()) {
            model.idPaciente = firstNonBlank(
                snapshot.child("idPaciente").getValue(String::class.java),
                snapshot.child("paciente_id").getValue(String::class.java)
            )
        }
        if (model.fecha.isBlank()) {
            model.fecha = firstNonBlank(
                snapshot.child("fechaAplicacion").getValue(String::class.java),
                snapshot.child("fecha").getValue(String::class.java)
            )
        }
        if (model.vacuna.isBlank()) {
            model.vacuna = firstNonBlank(
                snapshot.child("nombre").getValue(String::class.java),
                snapshot.child("vacuna").getValue(String::class.java)
            )
        }
        if (model.veterinario.isBlank()) {
            model.veterinario = snapshot.child("veterinario").getValue(String::class.java).orEmpty()
        }
        return model
    }

    fun mapNotificacion(snapshot: DataSnapshot): PortalNotificacionModel? {
        val model = snapshot.getValue(PortalNotificacionModel::class.java) ?: PortalNotificacionModel()
        if (model.id.isBlank()) model.id = snapshot.key.orEmpty()
        if (model.fecha.isBlank()) {
            model.fecha = firstNonBlank(
                snapshot.child("fecha").getValue(String::class.java),
                snapshot.child("created_at").getValue(String::class.java)
            )
        }
        if (model.mascotaId.isBlank()) {
            model.mascotaId = firstNonBlank(
                snapshot.child("mascotaId").getValue(String::class.java),
                snapshot.child("paciente_id").getValue(String::class.java),
                snapshot.child("idPaciente").getValue(String::class.java)
            )
        }
        return model
    }
}
