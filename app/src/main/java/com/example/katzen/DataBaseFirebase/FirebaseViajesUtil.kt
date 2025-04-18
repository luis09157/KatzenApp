package com.example.katzen.DataBaseFirebase

import com.example.katzen.Config.Config
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.VentaMesDetalleModel
import com.example.katzen.Model.VentaMesModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DecimalFormat
import android.util.Log

class FirebaseViajesUtil {
    companion object {
        private const val VIAJES_PATH = "Katzen/Gasolina"
        private const val VIAJES_IMAGES_PATH = "Viajes"
        val decimalFormat = DecimalFormat("#.00")
        private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val referenciaViaje: DatabaseReference = database.getReference(VIAJES_PATH)
        fun obtenerListaViajes(year: String, listener: ValueEventListener) {
            Log.d("FirebaseViajesUtil", "Buscando viajes para el año: $year")
            val referencia = database.getReference(VIAJES_PATH).child(year)
            Log.d("FirebaseViajesUtil", "Ruta completa: ${referencia.path}")
            referencia.addValueEventListener(listener)
        }
        fun obtenerListaCargosViajes(listener: ValueEventListener) {
            val referenciaViajesCargos: DatabaseReference = database.getReference(VIAJES_PATH)
                .child(Config.MES_DETALLE.split("-")[1])
                .child(Config.MES_DETALLE)
                .child("cargos")

            referenciaViajesCargos.orderByChild("fecha").limitToLast(100).addValueEventListener(listener)
        }

        fun eliminarViaje(ventaMesDetalleModel: VentaMesDetalleModel): Pair<Boolean, String> {
            val year = Config.MES_DETALLE.split("-")[1]
            val referenciaViajeCargo = database.getReference(VIAJES_PATH)
                .child(year)
                .child(Config.MES_DETALLE)
                .child("cargos")
                .child(ventaMesDetalleModel.fecha)
                .child(ventaMesDetalleModel.id)

            return try {
                referenciaViajeCargo.removeValue().isComplete to "Se eliminó el cargo de viaje correctamente."
            } catch (e: Exception) {
                false to "Error al eliminar el cargo de viaje: ${e.message}"
            }
        }

        fun editarCargoViaje(ventaMesDetalleModel: VentaMesDetalleModel): Pair<Boolean, String> {
            val year = Config.MES_DETALLE.split("-")[1]
            val referenciaViajeCargo = database.getReference(VIAJES_PATH)
                .child(year)
                .child(Config.MES_DETALLE)
                .child("cargos")
                .child(ventaMesDetalleModel.fecha)
                .child(ventaMesDetalleModel.id)

            return try {
                referenciaViajeCargo.setValue(ventaMesDetalleModel).isComplete to "Se editó el cargo de viaje correctamente."
            } catch (e: Exception) {
                false to "Error al editar el cargo de viaje: ${e.message}"
            }
        }
        fun eliminarCargoViaje(fecha: String, idViaje: String): Pair<Boolean, String> {
            val referenciaViajeCargo = database.getReference(VIAJES_PATH)
                .child(UtilHelper.getDateYear())
                .child(Config.MES_DETALLE)
                .child("cargos")
                .child(fecha)
                .child(idViaje)

            return try {
                referenciaViajeCargo.removeValue().isComplete to "Se editó el cargo de viaje correctamente."
            } catch (e: Exception) {
                false to "Error al eliminar el cargo de viaje: ${e.message}"
            }
        }

        fun guardarCargosViajes(ventaMesDetalleModel: VentaMesDetalleModel): Pair<Boolean, String> {
            val year = Config.MES_DETALLE.split("-")[1]
            val referenciaViajesCargos: DatabaseReference = database.getReference(VIAJES_PATH)
                .child(year)
                .child(Config.MES_DETALLE)
                .child("cargos")
                .child(ventaMesDetalleModel.fecha)
                .child(ventaMesDetalleModel.id)

            return try {
                referenciaViajesCargos.setValue(ventaMesDetalleModel).isComplete to "Se insertó el viaje correctamente."
            } catch (e: Exception) {
                false to "Error al insertar el viaje: ${e.message}"
            }
        }

        fun formatValue(value: Double): String {
            return if (value == 0.00) {
                "0.00"
            } else {
                decimalFormat.format(value)
            }
        }
        fun editarResumenViajes(): Pair<Boolean, String> {
            val year = Config.MES_DETALLE.split("-")[1]
            val referenciaViajesCargos: DatabaseReference = database.getReference(VIAJES_PATH)
                .child(year)
                .child(Config.MES_DETALLE)

            val datosActualizados = mapOf(
                "costo" to formatValue(Config.COSTO),
                "ganancia" to formatValue(Config.GANANCIA),
                "venta" to formatValue(Config.VENTA)
            )

            return try {
                referenciaViajesCargos.updateChildren(datosActualizados).isComplete to "Se editaron los datos del viaje correctamente."
            } catch (e: Exception) {
                false to "Error al editar los datos del viaje: ${e.message}"
            }
        }

        fun guardarListaMeses(month: String, ventaMesModel: VentaMesModel): Pair<Boolean, String> {
            val mesKey = "$month-${ventaMesModel.anio}"
            val referencia = database.getReference("$VIAJES_PATH/${ventaMesModel.anio}/$mesKey")
            return try {
                referencia.setValue(ventaMesModel).isComplete to "Se insertó el viaje correctamente."
            } catch (e: Exception) {
                false to "Error al insertar el viaje: ${e.message}"
            }
        }

    }
}