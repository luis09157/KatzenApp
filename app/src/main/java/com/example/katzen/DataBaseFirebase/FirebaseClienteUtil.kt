package com.example.katzen.DataBaseFirebase

import com.example.katzen.Model.ClienteModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
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
        fun eliminarCliente(clienteId: String, listener: OnCompleteListener) {
            val clienteReference = referenciaCliente.child(clienteId)

            clienteReference.removeValue { error, _ ->
                if (error == null) {
                    // Si no hay errores, la eliminación fue exitosa
                    listener.onComplete(true, "Cliente eliminado correctamente")
                } else {
                    // Si hay errores, la eliminación falló
                    listener.onComplete(false, "Error al eliminar el cliente: ${error.message}")
                }
            }
        }
        suspend fun editarCliente(clienteId: String, nuevoCliente: ClienteModel): Result<String> {
            return withContext(Dispatchers.IO) {
                suspendCoroutine { continuation ->
                    val clienteReference = referenciaCliente.child(clienteId)

                    clienteReference.setValue(nuevoCliente) { error, _ ->
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
    }
}