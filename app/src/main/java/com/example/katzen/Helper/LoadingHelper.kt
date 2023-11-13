package com.example.katzen.Helper

import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import com.google.android.material.progressindicator.CircularProgressIndicator

class LoadingHelper(
    loading: CircularProgressIndicator, contentList: LinearLayout, list: ListView,
    btnAddTravel: Button?, contentNotResult: LinearLayout) {
    val loading = loading
    val contentList = contentList
    val list = list
    val btnAddTravel = btnAddTravel
    val contentNotResult = contentNotResult
    fun loading(){
        loading.visibility = View.VISIBLE
        contentList.visibility = View.GONE
        list.visibility = View.VISIBLE
        btnAddTravel?.visibility = View.VISIBLE
        contentNotResult.visibility = View.GONE
    }
    fun not_loading(){
        loading.visibility = View.GONE
        contentList.visibility = View.VISIBLE
        contentNotResult.visibility = View.GONE
    }
    fun not_loading_result(){
        loading.visibility = View.GONE
        contentList.visibility = View.VISIBLE
        list.visibility = View.GONE
        btnAddTravel?.visibility = View.VISIBLE
        contentNotResult.visibility = View.VISIBLE
    }
}