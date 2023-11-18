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
        val df = DecimalFormat("#.##")

        /*viewHolder.txt_mes.text = listVentaMes.get(position).mes
        viewHolder.txt_ganancia.text = "$ " +listVentaMes.get(position).ganancia
        viewHolder.txt_costo.text = "$ " +listVentaMes.get(position).costo
        viewHolder.txt_venta.text = "$ " +listVentaMes.get(position).venta*/


        return rowView
    }

    override fun getItem(position: Int): Any {
        return 0
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    private class ViewHolder(view: View?) {
        /*val txt_mes = view?.findViewById(R.id.txt_mes) as TextView
        val txt_costo = view?.findViewById(R.id.txt_costo) as TextView
        val txt_ganancia = view?.findViewById(R.id.txt_ganancia) as TextView
        val txt_venta = view?.findViewById(R.id.txt_venta) as TextView*/
    }
}