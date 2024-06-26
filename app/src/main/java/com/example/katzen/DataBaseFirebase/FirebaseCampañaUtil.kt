package com.example.katzen.DataBaseFirebase

import android.util.Log
import com.example.katzen.Fragment.Campaña.CampañaFragment
import com.example.katzen.Helper.CalendarioUtil
import com.example.katzen.Model.CampañaModel
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
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val referenciaCampaña: DatabaseReference = database.getReference(CAMPANAS_PATH)

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
                Log.e("amonosperro","${CAMPANAS_PATH}/${campaña.año}/${campaña.mes}-${campaña.año}/eb901a42-fa5f-4458-bc7b-98b1cc4ebe5a/eventos")
                val referenciaPacienteCampaña: DatabaseReference = database.getReference("${CAMPANAS_PATH}/${campaña.año}/${campaña.mes}-${campaña.año}/-O071659lrlEpUyoenFQ/eventos")
                val nuevoPacienteRef = referenciaPacienteCampaña.push()
                val pacienteId = nuevoPacienteRef.key ?: return Result.failure(Exception("No se pudo generar un ID para el paciente"))
                val pacienteData = mapOf(
                    "id" to UUID.randomUUID().toString(),
                    "idCliente" to campaña.idCliente,
                    "idPaciente" to campaña.idPaciente,
                    "fecha" to campaña.fecha
                )
                nuevoPacienteRef.setValue(pacienteData).await()
                Result.success("Paciente agregado exitosamente a la campaña con ID: $pacienteId")
            } catch (e: Exception) {
                println("Error al agregar el paciente a la campaña: ${e.message}")
                Result.failure(e)
            }
        }

        // Otros métodos de utilidad para editar, eliminar, obtener campañas, etc. según tus necesidades
    }
}
