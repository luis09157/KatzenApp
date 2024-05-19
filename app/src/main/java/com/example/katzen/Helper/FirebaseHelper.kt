package com.example.katzen.Helper

import android.app.Activity
import android.app.AlertDialog
import android.widget.Toast
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
        fun getRefFirebaseMascotas() : DatabaseReference {
            database = Firebase.database.reference
            queryMascota = database.child("Katzen").child("Mascota")
            return queryMascota as DatabaseReference
        }
    }
}