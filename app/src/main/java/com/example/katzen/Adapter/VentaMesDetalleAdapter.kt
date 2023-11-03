package com.example.katzen.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.katzen.Model.VentaMesDetalleModel
import com.example.katzen.R
import com.google.android.material.floatingactionbutton.FloatingActionButton


class VentaMesDetalleAdapter(context: Context, val listVentaMes: ArrayList<VentaMesDetalleModel>) : BaseAdapter() {
    private val layoutInflater = LayoutInflater.from(context)
    private val context = context
    private val TAG = "VentaMesDetalleAdapter"

    override fun getCount(): Int {
        return listVentaMes.size
    }

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View? {
        val viewHolder: ViewHolder
        val rowView: View?

        if (view == null) {
            rowView = layoutInflater.inflate(R.layout.vista_viajes_detalle, viewGroup, false)

            viewHolder = ViewHolder(rowView)
            rowView.tag = viewHolder

        } else {
            rowView = view
            viewHolder = rowView.tag as ViewHolder
        }

        viewHolder.txt_categoria.text = listVentaMes.get(position).categoria
        viewHolder.txt_domicilio.text = listVentaMes.get(position).domicilio
        viewHolder.txt_kilometros.text = listVentaMes.get(position).kilometros
        viewHolder.txt_fecha.text = listVentaMes.get(position).fecha

        viewHolder.txt_ganancia.text = "$ " +listVentaMes.get(position).ganancia
        viewHolder.txt_costo.text = "$ " +listVentaMes.get(position).costo
        viewHolder.txt_venta.text = "$ " +listVentaMes.get(position).venta

        viewHolder.btn_edit.setOnClickListener {

        }


        return rowView
    }

    override fun getItem(position: Int): Any {
        return 0
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    private class ViewHolder(view: View?) {
        val txt_fecha = view?.findViewById(R.id.txt_fecha) as TextView
        val txt_categoria = view?.findViewById(R.id.txt_categoria) as TextView
        val txt_domicilio = view?.findViewById(R.id.txt_domicilio) as TextView
        val txt_kilometros = view?.findViewById(R.id.txt_kilometros) as TextView

        val txt_ganancia = view?.findViewById(R.id.txt_ganancia) as TextView
        val txt_costo = view?.findViewById(R.id.txt_costo) as TextView
        val txt_venta = view?.findViewById(R.id.txt_venta) as TextView

        val btn_edit = view?.findViewById(R.id.btn_edit) as FloatingActionButton
    }
}