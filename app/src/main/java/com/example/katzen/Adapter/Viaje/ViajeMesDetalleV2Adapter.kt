package com.example.katzen.Adapter.Viaje

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.VentaMesDetalleModel
import com.example.katzen.R

class ViajeMesDetalleV2Adapter (
    activity: Activity,
    private var viajeDetalleList: List<VentaMesDetalleModel>
) : ArrayAdapter<VentaMesDetalleModel>(activity, R.layout.vista_viajes_detalle, viajeDetalleList) {

    private var originalList: List<VentaMesDetalleModel> = viajeDetalleList.toList()
    var activity : Activity = activity
    var TAG : String = "ViajeMesDetalleV2Adapter"

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        val holder: ViewHolder

        if (itemView == null) {
            itemView = LayoutInflater.from(activity).inflate(R.layout.vista_viajes_detalle, parent, false)
            holder = ViewHolder()
            holder.card_cont = itemView.findViewById(R.id.card_cont)

            holder.txt_paciente = itemView.findViewById(R.id.txt_paciente)
            holder.txt_categoria = itemView.findViewById(R.id.txt_categoria)
            holder.txt_domicilio = itemView.findViewById(R.id.txt_domicilio)
            holder.txt_kilometros = itemView.findViewById(R.id.txt_kilometros)
            holder.txt_fecha = itemView.findViewById(R.id.txt_fecha)


            holder.txt_mes = itemView.findViewById(R.id.txt_mes)
            holder.txt_costo = itemView.findViewById(R.id.txt_costo)
            holder.txt_ganancia = itemView.findViewById(R.id.txt_ganancia)
            holder.txt_venta = itemView.findViewById(R.id.txt_venta)

            holder.btnEditar = itemView.findViewById(R.id.btn_edit)


            itemView.tag = holder
        } else {
            holder = itemView.tag as ViewHolder
        }
        val viaje = viajeDetalleList[position]

        holder.txt_paciente?.text = ""
        holder.txt_categoria?.text = ""
        holder.txt_domicilio?.text = ""
        holder.txt_kilometros?.text = ""
        holder.txt_fecha?.text = ""
        holder.txt_costo?.text = ""
        holder.txt_mes?.text = ""
        holder.txt_ganancia?.text = ""
        holder.txt_venta?.text = ""

        holder.txt_paciente!!.text = viaje.nombreDomicilio
        holder.txt_categoria!!.text = viaje.categoria
        holder.txt_domicilio!!.text = viaje.domicilio
        holder.txt_kilometros!!.text = viaje.kilometros
        holder.txt_fecha!!.text = viaje.fecha

        holder.txt_ganancia!!.text = "$ ${viaje.ganancia}"
        holder.txt_costo!!.text = "$ ${viaje.costo}"
        holder.txt_venta!!.text = "$ ${viaje.venta}"


        holder.card_cont!!.setOnClickListener {
            UtilHelper.abrirGoogleMaps(activity, viaje.linkMaps)
        }
        holder.btnEditar!!.setOnClickListener {
            //DialogHelper.dialogEditDomicilio(activity,viajeDetalleList[position],myTopPostsQuery,loadingHelper )
        }


        return itemView!!
    }

    override fun getCount(): Int {
        return viajeDetalleList.size
    }

    override fun getItem(position: Int): VentaMesDetalleModel? {
        return viajeDetalleList[position]
    }
    fun updateList(newList: List<VentaMesDetalleModel>) {
        viajeDetalleList = newList
        originalList = newList.toList()
        notifyDataSetChanged()
    }

    private class ViewHolder {
        var card_cont: CardView? = null
        var txt_paciente: TextView? = null
        var txt_categoria: TextView? = null
        var txt_domicilio: TextView? = null
        var txt_kilometros: TextView? = null
        var txt_fecha: TextView? = null

        var txt_mes: TextView? = null
        var txt_costo: TextView? = null
        var txt_ganancia: TextView? = null
        var txt_venta: TextView? = null

        var btnEditar: CardView? = null

    }
}