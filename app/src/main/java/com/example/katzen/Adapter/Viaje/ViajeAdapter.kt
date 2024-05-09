package com.example.katzen.Adapter.Viaje

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.katzen.Model.VentaMesModel
import com.example.katzen.R

class ViajeAdapter (
    activity: Activity,
    private var viajeList: List<VentaMesModel>
) : ArrayAdapter<VentaMesModel>(activity, R.layout.vista_viajes, viajeList) {

    private var originalList: List<VentaMesModel> = viajeList.toList()
    var activity : Activity = activity
    var TAG : String = "ClienteAdapter"

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        val holder: ViewHolder

        if (itemView == null) {
            itemView = LayoutInflater.from(activity).inflate(R.layout.vista_viajes, parent, false)
            holder = ViewHolder()
            holder.txt_mes = itemView.findViewById(R.id.txt_mes)
            holder.txt_costo = itemView.findViewById(R.id.txt_costo)
            holder.txt_ganancia = itemView.findViewById(R.id.txt_ganancia)
            holder.txt_venta = itemView.findViewById(R.id.txt_venta)

            itemView.tag = holder
        } else {
            holder = itemView.tag as ViewHolder
        }
        val viaje = viajeList[position]

        holder.txt_costo?.text = ""
        holder.txt_mes?.text = ""
        holder.txt_ganancia?.text = ""
        holder.txt_venta?.text = ""


        holder.txt_mes?.text = viaje.mes
        holder.txt_costo?.text = "$${viaje.costo}"
        holder.txt_ganancia?.text = "$${viaje.ganancia}"
        holder.txt_venta?.text = "$${viaje.venta}"


        return itemView!!
    }

    override fun getCount(): Int {
        return viajeList.size
    }

    override fun getItem(position: Int): VentaMesModel? {
        return viajeList[position]
    }
    fun updateList(newList: List<VentaMesModel>) {
        viajeList = newList
        originalList = newList.toList()
        notifyDataSetChanged()
    }

    private class ViewHolder {
        var txt_mes: TextView? = null
        var txt_costo: TextView? = null
        var txt_ganancia: TextView? = null
        var txt_venta: TextView? = null

    }
}