package com.example.katzen.Model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class StoreModel(
    var id: String = "", var nombre: String? = null) {
}