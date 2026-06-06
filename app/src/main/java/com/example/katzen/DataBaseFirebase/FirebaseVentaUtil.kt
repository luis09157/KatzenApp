package com.example.katzen.DataBaseFirebase

import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.VentaModel
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object FirebaseVentaUtil {

    private const val VENTAS_PATH = "Katzen/Venta"
    private val database = FirebaseDatabase.getInstance()
    private val referenciaVentas = database.getReference(VENTAS_PATH)

    /**
     * Guarda bajo Katzen/Venta/{año}/{dd-MM-yyyy}/{id} (estructura web).
     * También escribe en Katzen/Venta/registro/{id} para compatibilidad con lecturas planas.
     */
    fun guardarVenta(venta: VentaModel): Task<Void> {
        venta.ganancia = VentaModel.calcularGanancia(venta.venta, venta.costo)
        val structuredRef = referenciaVentas.child(buildStructuredPath(venta))
        val flatRef = referenciaVentas.child("registro").child(venta.id)
        val payload = venta.copy()
        return structuredRef.setValue(payload).continueWithTask { task ->
            if (!task.isSuccessful) task
            else flatRef.setValue(payload)
        }
    }

    private fun buildStructuredPath(venta: VentaModel): String {
        return try {
            val fechaLimpia = venta.fecha.split(",").first().trim()
            val (dia, mes, anio) = UtilHelper.parseFecha(fechaLimpia)
            val dateKey = "$dia-$mes-$anio"
            "$anio/$dateKey/${venta.id}"
        } catch (_: Exception) {
            "registro/${venta.id}"
        }
    }

    fun obtenerListaVentas(listener: ValueEventListener) {
        referenciaVentas.child("registro").orderByChild("fecha").addValueEventListener(listener)
    }

    fun removerListener(listener: ValueEventListener) {
        referenciaVentas.child("registro").removeEventListener(listener)
    }
}
