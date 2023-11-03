package com.example.katzen.Helper

import android.app.Activity
import android.app.AlertDialog
import android.widget.Toast
import com.example.katzen.Helper.DialogHelper.Companion.getDateNow
import com.example.katzen.Model.VentaMesDetalleModel
import com.google.firebase.database.DatabaseReference
import java.text.SimpleDateFormat
import java.util.Date

class FirebaseHelper {
    companion object{
        fun getIdFirebase(): String{
            val dateFormatter = SimpleDateFormat("ddMMyyyyHHMMSS")
            return dateFormatter.format(Date())
        }
        fun newAdress(alertDialog: AlertDialog, vMDM : VentaMesDetalleModel,
                      activity: Activity,myTopPostsQuery: DatabaseReference,loadingHelper: LoadingHelper) {
            try {
                myTopPostsQuery!!.child(getDateNow()).child(getIdFirebase()).setValue(vMDM)
                Toast.makeText(activity,"Direccion Guardada.", Toast.LENGTH_LONG).show()
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