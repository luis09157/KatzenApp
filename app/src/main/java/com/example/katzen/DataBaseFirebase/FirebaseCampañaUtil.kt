package com.example.katzen.DataBaseFirebase

import PacienteModel
import android.util.Log
import com.example.katzen.Fragment.Campaña.CampañaFragment
import com.example.katzen.Helper.CalendarioUtil
import com.example.katzen.Model.CampañaModel
import com.example.katzen.Model.ClienteModel
import com.example.katzen.Model.PacienteCampañaModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.coroutines.resume

class FirebaseCampañaUtil {
    companion object {
        private const val CAMPANAS_PATH = "Katzen/Campaña" // Ruta donde se guardan las campañas
        private const val PACIENTES_PATH = "Katzen/Paciente"
        private const val CLIENTES_PATH = "Katzen/Cliente"
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val referenciaCampaña: DatabaseReference = database.getReference(CAMPANAS_PATH)
        val referenciaPaciente: DatabaseReference = database.getReference(PACIENTES_PATH)
        val referenciaCliente: DatabaseReference = database.getReference(CLIENTES_PATH)

        @JvmStatic
        fun obtenerListaCampañas(listener: ValueEventListener) {
            val referenciaCampañas = database.getReference("$CAMPANAS_PATH/${CampañaFragment.ADD_CAMPAÑA.año}/${CampañaFragment.ADD_CAMPAÑA.mes}-${CampañaFragment.ADD_CAMPAÑA.año}")

            referenciaCampañas.addValueEventListener(listener)
        }
        suspend fun contarCampañasPorMes(mes: String): Int {
            return suspendCancellableCoroutine { continuation ->
                referenciaCampaña.child(CalendarioUtil.obtenerAñoActual()).child(mes).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val count = snapshot.childrenCount.toInt()
                        continuation.resume(count)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println("Error al obtener la cantidad de campañas para el mes $mes: ${error.message}")
                        continuation.resume(0) // Retornar cero en caso de error
                    }
                })
            }
        }
        suspend fun agregarCampaña(campaña: CampañaModel): Result<String> {
            return try {
                val referenciaAddCampaña: DatabaseReference = database.getReference("${CAMPANAS_PATH}/${campaña.año}/${campaña.mes}-${campaña.año}")
                val campañaData = mapOf(
                    "id" to campaña.id,
                    "nombreCampaña" to campaña.nombreCampaña,
                    "fecha" to campaña.fecha
                )
                referenciaAddCampaña.child(campaña.id).setValue(campañaData).await()
                Result.success("Campaña agregada exitosamente con ID: ${campaña.id}")
            } catch (e: Exception) {
                println("Error al agregar la campaña: ${e.message}")
                Result.failure(e)
            }
        }
        suspend fun agregarPacienteCampaña(campaña: CampañaModel): Result<String> {
            return try {
                val referenciaPacienteCampaña: DatabaseReference = database.getReference("${CAMPANAS_PATH}/${campaña.año}/${campaña.mes}-${campaña.año}/${campaña.id}/pacientes")

                // Verificar si el paciente ya existe
                val existingPacienteSnapshot = referenciaPacienteCampaña.orderByChild("idPaciente").equalTo(campaña.idPaciente).get().await()
                if (existingPacienteSnapshot.exists()) {
                    return Result.failure(Exception("El paciente con ID: ${campaña.idPaciente} ya existe en la campaña."))
                }

                // Si no existe, proceder a agregar el nuevo paciente
                val nuevoPacienteRef = referenciaPacienteCampaña.push()
                val pacienteId = nuevoPacienteRef.key ?: return Result.failure(Exception("No se pudo generar un ID para el paciente"))
                val pacienteData = mapOf(
                    "id" to UUID.randomUUID().toString(),
                    "idCliente" to campaña.idCliente,
                    "idPaciente" to campaña.idPaciente,
                    "fecha" to campaña.fecha
                )
                nuevoPacienteRef.setValue(pacienteData).await()
                return Result.success("Paciente agregado exitosamente a la campaña con ID: $pacienteId")
            } catch (e: Exception) {
                println("Error al agregar el paciente a la campaña: ${e.message}")
                return Result.failure(e)
            }
        }

        suspend fun obtenerCantidadPacientes(campaña: CampañaModel): Int {
            return suspendCancellableCoroutine { continuation ->
                val referenciaPacientes = database.getReference("${CAMPANAS_PATH}/${campaña.año}/${campaña.mes}-${campaña.año}/${campaña.id}/pacientes")

                referenciaPacientes.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val cantidadPacientes = snapshot.childrenCount.toInt()
                        continuation.resume(cantidadPacientes)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println("Error al obtener la cantidad de pacientes: ${error.message}")
                        continuation.resume(0) // Retornar cero en caso de error
                    }
                })
            }
        }
        suspend fun obtenerListaPacientes(campaña: CampañaModel): List<PacienteCampañaModel> {
            return suspendCancellableCoroutine { continuation ->
                val referenciaPacientes: DatabaseReference = database.getReference("$CAMPANAS_PATH/${campaña.año}/${campaña.mes}-${campaña.año}/${campaña.id}/pacientes")

                referenciaPacientes.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val pacientes = snapshot.children.mapNotNull { it.getValue(PacienteCampañaModel::class.java) }
                        continuation.resume(pacientes)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(emptyList()) // Retornar una lista vacía en caso de error
                    }
                })
            }
        }
        suspend fun eliminarPacientePorIdPacienteCampaña(idPaciente: String): Boolean {
            return try {
                val campaña = CampañaFragment.ADD_CAMPAÑA
                val referenciaPacientes = FirebaseDatabase.getInstance()
                    .getReference("Katzen/Campaña/${campaña.año}/${campaña.mes}-${campaña.año}/${campaña.id}/pacientes")

                val pacienteSnapshot = referenciaPacientes.orderByChild("idPaciente").equalTo(idPaciente).get().await()

                if (pacienteSnapshot.exists()) {
                    for (snapshot in pacienteSnapshot.children) {
                        snapshot.ref.removeValue().await()
                    }
                    true
                } else {
                    Log.e("FirebaseCampañaUtil", "Paciente con idPaciente $idPaciente no encontrado en la campaña.")
                    false
                }
            } catch (e: Exception) {
                Log.e("FirebaseCampañaUtil", "Error al eliminar paciente de la campaña: ${e.message}")
                false
            }
        }
        suspend fun eliminarCampaña(): Pair<Boolean, String> {
            return suspendCancellableCoroutine { continuation ->
                val referenciaCampaña: DatabaseReference = database.getReference("${CAMPANAS_PATH}/${CampañaFragment.ADD_CAMPAÑA.año}/${CampañaFragment.ADD_CAMPAÑA.mes}-${CampañaFragment.ADD_CAMPAÑA.año}")
                referenciaCampaña.child(CampañaFragment.ADD_CAMPAÑA.id).removeValue()
                    .addOnSuccessListener {
                        continuation.resume(true to "Campaña eliminada exitosamente con ID: ${CampañaFragment.ADD_CAMPAÑA.id}")
                    }
                    .addOnFailureListener { exception ->
                        println("Error al eliminar la campaña: ${exception.message}")
                        continuation.resume(false to "Error al eliminar la campaña.")
                    }
            }
        }
        suspend fun obtenerPacienteYCliente(idPaciente: String): Pair<PacienteModel?, ClienteModel?> {
            return try {
                val pacienteResult = FirebasePacienteUtil.obtenerPacientePorId(idPaciente)
                if (pacienteResult != null) {
                    val clienteResult = FirebaseClienteUtil.obtenerClientePorId(pacienteResult.idCliente)
                    Pair(pacienteResult, clienteResult)
                } else {
                    Pair(null, null)
                }
            } catch (e: Exception) {
                // Utiliza un logger en lugar de println para una mejor gestión de errores
                Log.e("ErrorObteniendoDatos", "Error al obtener los datos: ${e.message}", e)
                Pair(null, null)
            }
        }

    }
}
