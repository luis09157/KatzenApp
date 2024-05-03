package com.example.katzen.Helper

import android.app.Activity
import android.app.AlertDialog
import android.widget.Toast
import com.example.katzen.Helper.DialogHelper.Companion.getDateNow
import com.example.katzen.Model.VentaMesDetalleModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date

class FirebaseHelper {
    companion object{
        private lateinit var database: DatabaseReference
        var queryMascota: DatabaseReference? = null
        fun getIdFirebase(): String{
            val dateFormatter = SimpleDateFormat("ddMMyyyyHHMMSS")
            return dateFormatter.format(Date())
        }
        fun getRefFirebaseMascotaAdd(){
            database = Firebase.database.reference
            queryMascota = database.child("Katzen").child("Mascota").child("paciente")
        }
        fun getRefFirebaseMascotas() : DatabaseReference {
            database = Firebase.database.reference
            queryMascota = database.child("Katzen").child("Mascota")
            return queryMascota as DatabaseReference
        }
        fun newAdress(alertDialog: AlertDialog, vMDM : VentaMesDetalleModel,
                      activity: Activity,myTopPostsQuery: DatabaseReference,loadingHelper: LoadingHelper) {
            try {
                val key = getIdFirebase()
                vMDM.key = key

                vMDM.key_fecha_hora = UtilHelper.getDate()
                myTopPostsQuery!!.child(getDateNow()).child(key).setValue(vMDM)
                Toast.makeText(activity,"Direccion Guardada.", Toast.LENGTH_LONG).show()
                alertDialog.hide()
            }
            catch (e: Exception) {
                loadingHelper.not_loading_result()
                Toast.makeText(activity,"Hubo una problema al guardar el domicilio, intenta nuevamente.",
                    Toast.LENGTH_LONG).show()
            }

        }
        fun editAdress(alertDialog: AlertDialog, vMDM : VentaMesDetalleModel,
                      activity: Activity,myTopPostsQuery: DatabaseReference,loadingHelper: LoadingHelper) {
            try {
                myTopPostsQuery!!.child(vMDM.key_date).child(vMDM.key).setValue(vMDM)
                Toast.makeText(activity,"Direccion editada.", Toast.LENGTH_LONG).show()
                alertDialog.hide()
            }
            catch (e: Exception) {
                loadingHelper.not_loading_result()
                Toast.makeText(activity,"Hubo una problema al guardar el domicilio, intenta nuevamente.",
                    Toast.LENGTH_LONG).show()
            }

        }
    }
}