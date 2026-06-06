package com.example.katzen.DataBaseFirebase

import com.example.katzen.Helper.ImageLoaderHelper
import com.example.katzen.Helper.RtdbActiveRecords
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.ClienteModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

interface OnCompleteListener {
    fun onComplete(success: Boolean, message: String)
}

class FirebaseClienteUtil {
    companion object {
        private const val CLIENTES_PATH = "Katzen/Cliente"
        private const val CLIENTES_IMAGES_PATH = "Clientes"
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val referenciaCliente: DatabaseReference = database.getReference(CLIENTES_PATH)

        fun obtenerListaClientes(listener: ValueEventListener) {
            referenciaCliente.addValueEventListener(listener)
        }

        fun obtenerListaClientesUnaVez(listener: ValueEventListener) {
            referenciaCliente.addListenerForSingleValueEvent(listener)
        }

        fun removerListenerClientes(listener: ValueEventListener) {
            referenciaCliente.removeEventListener(listener)
        }

        suspend fun obtenerClientePorId(idCliente: String): ClienteModel? {
            return suspendCancellableCoroutine { continuation ->
                referenciaCliente.child(idCliente).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val cliente = parseCliente(snapshot)
                        continuation.resume(cliente)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(null)
                    }
                })
            }
        }

        fun parseCliente(snapshot: DataSnapshot): ClienteModel? {
            if (!snapshot.exists()) return null
            val cliente = snapshot.getValue(ClienteModel::class.java) ?: ClienteModel()
            if (cliente.id.isBlank()) cliente.id = snapshot.key.orEmpty()
            val resolvedImage = ImageLoaderHelper.resolveProfileImage(
                imageUrl = cliente.imageUrl,
                imageFileName = cliente.imageFileName,
                snapshot = snapshot
            )
            cliente.imageUrl = resolvedImage.imageUrl
            cliente.imageFileName = resolvedImage.imageFileName
            if (!snapshot.hasChild("activo")) cliente.activo = true
            return cliente
        }

        /**
         * Desactiva el cliente (soft-delete). No borra datos ni mascotas.
         * Alineado con el campo activo del portal web.
         */
        fun desactivarCliente(clienteId: String, listener: OnCompleteListener) {
            val updates = mapOf(
                "activo" to false,
                "portalActivo" to false,
                "fechaBaja" to UtilHelper.getDate()
            )
            referenciaCliente.child(clienteId).updateChildren(updates) { error, _ ->
                if (error == null) {
                    listener.onComplete(true, "Cliente desactivado correctamente")
                } else {
                    listener.onComplete(false, "Error al desactivar el cliente: ${error.message}")
                }
            }
        }

        @Deprecated("Usa desactivarCliente para no borrar datos", ReplaceWith("desactivarCliente(clienteId, listener)"))
        fun eliminarCliente(clienteId: String, listener: OnCompleteListener) {
            desactivarCliente(clienteId, listener)
        }

        suspend fun editarCliente(clienteId: String, nuevoCliente: ClienteModel): Result<String> {
            return withContext(Dispatchers.IO) {
                suspendCoroutine { continuation ->
                    val clienteReference = referenciaCliente.child(clienteId)
                    clienteReference.setValue(nuevoCliente) { error, _ ->
                        if (error == null) {
                            continuation.resume(Result.success("Cliente editado correctamente"))
                        } else {
                            continuation.resumeWithException(error.toException())
                        }
                    }
                }
            }
        }

        @Deprecated("Usa desactivarCliente", ReplaceWith("desactivarCliente(clienteId, listener)"))
        suspend fun eliminarCliente(clienteId: String): Boolean {
            return try {
                desactivarClienteSuspend(clienteId)
                true
            } catch (_: Exception) {
                false
            }
        }

        suspend fun desactivarClienteSuspend(clienteId: String) {
            val updates = mapOf(
                "activo" to false,
                "portalActivo" to false,
                "fechaBaja" to UtilHelper.getDate()
            )
            referenciaCliente.child(clienteId).updateChildren(updates).await()
        }

        suspend fun reactivarCliente(clienteId: String): Result<String> {
            return try {
                val updates = mapOf(
                    "activo" to true,
                    "fechaBaja" to ""
                )
                referenciaCliente.child(clienteId).updateChildren(updates).await()
                Result.success("Cliente reactivado correctamente")
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        suspend fun obtenerClientesInactivos(): List<ClienteModel> {
            return queryByActivo(false) { snapshot ->
                snapshot.children.mapNotNull { parseCliente(it) }
            }
        }

        private suspend fun queryByActivo(
            activo: Boolean,
            mapper: (DataSnapshot) -> List<ClienteModel>
        ): List<ClienteModel> {
            return suspendCancellableCoroutine { continuation ->
                referenciaCliente.orderByChild("activo").equalTo(activo)
                    .get()
                    .addOnSuccessListener { continuation.resume(mapper(it)) }
                    .addOnFailureListener { continuation.resumeWithException(it) }
            }
        }
    }
}
