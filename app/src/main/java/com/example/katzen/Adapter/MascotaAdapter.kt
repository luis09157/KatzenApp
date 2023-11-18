package com.example.katzen.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.katzen.Model.MascotaModel
import com.example.katzen.R
import java.text.DecimalFormat

class MascotaAdapter(context: Context, val listMascota: ArrayList<MascotaModel>) : BaseAdapter() {
    private val layoutInflater = LayoutInflater.from(context)
    private val context = context
    private val TAG = "MascotaAdapter"

    override fun getCount(): Int {
        return listMascota.size
    }

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View? {
        val viewHolder: ViewHolder
        val rowView: View?

        if (view == null) {
            rowView = layoutInflater.inflate(R.layout.vista_mascota, viewGroup, false)

            viewHolder = ViewHolder(rowView)
            rowView.tag = viewHolder

        } else {
            rowView = view
            viewHolder = rowView.tag as ViewHolder
        }

        viewHolder.txt_nombre.text = listMascota.get(position).nombre
        viewHolder.txt_especie.text = listMascota.get(position).especie
        viewHolder.txt_sexo.text = listMascota.get(position).sexo


        return rowView
    }

    override fun getItem(position: Int): Any {
        return 0
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    private class ViewHolder(view: View?) {
        val txt_nombre = view?.findViewById(R.id.txt_nombre) as TextView
        val txt_especie = view?.findViewById(R.id.txt_especie) as TextView
        val txt_sexo = view?.findViewById(R.id.txt_sexo) as TextView
    }
}