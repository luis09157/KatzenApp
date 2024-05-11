package com.example.katzen.DataBaseFirebase

import android.util.Log
import com.example.katzen.Config.Config
import com.example.katzen.Helper.DialogHelper
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.VentaMesDetalleModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import okhttp3.internal.Util

class FirebaseViajesUtil {
    companion object {
        private const val VIAJES_PATH = "Katzen/Gasolina"
        private const val VIAJES_IMAGES_PATH = "Viajes"
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val referenciaViaje: DatabaseReference = database.getReference(VIAJES_PATH)
        fun obtenerListaViajes(listener: ValueEventListener) {
            referenciaViaje.child(UtilHelper.getDateYear()).addValueEventListener(listener)
        }
        fun obtenerListaCargosViajes(mes: String, listener: ValueEventListener) {
            val referenciaViajesCargos: DatabaseReference = database.getReference(VIAJES_PATH)
                .child(UtilHelper.getDateYear())
                .child(Config.MES_DETALLE)
                .child("cargos")

            referenciaViajesCargos.addValueEventListener(listener)
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

            val datosActualizados = mapOf(
                "costo" to Config.COSTO.toString(),
                "ganancia" to Config.GANANCIA.toString(),
                "venta" to Config.GANANCIA.toString()
            )

            return try {
                referenciaViajesCargos.updateChildren(datosActualizados).isComplete to "Se editaron los datos del viaje correctamente."
            } catch (e: Exception) {
                false to "Error al editar los datos del viaje: ${e.message}"
            }
        }


    }
}