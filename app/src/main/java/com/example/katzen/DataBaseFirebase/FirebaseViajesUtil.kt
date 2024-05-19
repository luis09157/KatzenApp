package com.example.katzen.DataBaseFirebase

import com.example.katzen.Config.Config
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.VentaMesDetalleModel
import com.example.katzen.Model.VentaMesModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DecimalFormat

class FirebaseViajesUtil {
    companion object {
        private const val VIAJES_PATH = "Katzen/Gasolina"
        private const val VIAJES_IMAGES_PATH = "Viajes"
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val referenciaViaje: DatabaseReference = database.getReference(VIAJES_PATH)
        fun obtenerListaViajes(listener: ValueEventListener) {
            referenciaViaje.child(UtilHelper.getDateYear()).addValueEventListener(listener)
        }
        fun obtenerListaCargosViajes( listener: ValueEventListener) {
            val referenciaViajesCargos: DatabaseReference = database.getReference(VIAJES_PATH)
                .child(UtilHelper.getDateYear())
                .child(Config.MES_DETALLE)
                .child("cargos")

            referenciaViajesCargos.orderByChild("fecha").limitToLast(100).addValueEventListener(listener)

        }

        fun eliminarViaje(ventaMesDetalleModel: VentaMesDetalleModel): Pair<Boolean, String> {
            val referenciaViajeCargo = database.getReference(VIAJES_PATH)
                .child(UtilHelper.getDateYear())
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
            val referenciaViajeCargo = database.getReference(VIAJES_PATH)
                .child(UtilHelper.getDateYear())
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
            val referenciaViajesCargos: DatabaseReference = database.getReference(VIAJES_PATH)
                .child(UtilHelper.getDateYear())
                .child(UtilHelper.obtenerMesYAnio(ventaMesDetalleModel.fecha))
                .child("cargos")
                .child(ventaMesDetalleModel.fecha)
                .child(ventaMesDetalleModel.id)

            return try {
                referenciaViajesCargos.setValue(ventaMesDetalleModel).isComplete to "Se insertó el viaje correctamente."
            } catch (e: Exception) {
                false to "Error al insertar el viaje: ${e.message}"
            }
        }

        fun editarResumenViajes(): Pair<Boolean, String> {
            val referenciaViajesCargos: DatabaseReference = database.getReference(VIAJES_PATH)
                .child(UtilHelper.getDateYear())
                .child(Config.MES_DETALLE)

            val decimalFormat = DecimalFormat("#.##")

            val datosActualizados = mapOf(
                "costo" to decimalFormat.format(Config.COSTO).toString(),
                "ganancia" to decimalFormat.format(Config.GANANCIA).toString(),
                "venta" to decimalFormat.format(Config.VENTA).toString()
            )

            return try {
                referenciaViajesCargos.updateChildren(datosActualizados).isComplete to "Se editaron los datos del viaje correctamente."
            } catch (e: Exception) {
                false to "Error al editar los datos del viaje: ${e.message}"
            }
        }

        fun guardarListaMeses(mes : String ,ventaMesModel: VentaMesModel): Pair<Boolean, String> {
            val referenciaViajesCargos: DatabaseReference = database.getReference(VIAJES_PATH)
                .child(UtilHelper.getDateYear())
                .child(mes)

            return try {
                referenciaViajesCargos.setValue(ventaMesModel).isComplete to "Se insertó el viaje correctamente."
            } catch (e: Exception) {
                false to "Error al insertar el viaje: ${e.message}"
            }
        }

    }
}