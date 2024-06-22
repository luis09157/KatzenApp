package com.example.katzen.Adapter.Campaña

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.katzen.DataBaseFirebase.FirebaseClienteUtil
import com.example.katzen.DataBaseFirebase.OnCompleteListener
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.CampañaModel
import com.example.katzen.Model.ClienteModel
import com.example.katzen.R
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CampañaAdapter(
activity: Activity,
private var campañaList: List<CampañaModel>
) : ArrayAdapter<CampañaModel>(activity, R.layout.view_detalle_calendario, campañaList) {

    private var originalList: List<CampañaModel> = campañaList.toList()
    var activity : Activity = activity
    var TAG : String = "CampañaAdapter"

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        val holder: ViewHolder

        if (itemView == null) {
            itemView = LayoutInflater.from(activity).inflate(R.layout.view_detalle_calendario, parent, false)
            holder = ViewHolder()

            holder.mesTextView = itemView.findViewById(R.id.mesTextView)
            holder.cantidadEventos = itemView.findViewById(R.id.eventosTextView)

            itemView.tag = holder
        } else {
            holder = itemView.tag as ViewHolder
        }
        val campaña = campañaList[position]

        holder.mesTextView?.text = ""
        holder.cantidadEventos?.text = ""

        holder.mesTextView?.text =  campaña.mes
        holder.cantidadEventos?.text = "${campaña.cantidadCampañas} Eventos"



        return itemView!!
    }

    override fun getCount(): Int {
        return campañaList.size
    }

    override fun getItem(position: Int): CampañaModel? {
        return campañaList[position]
    }
    fun updateList(newList: List<CampañaModel>) {
        campañaList = newList.toList()
        originalList = newList.toList()
        notifyDataSetChanged()
    }

    private class ViewHolder {
        var mesTextView: TextView? = null
        var cantidadEventos: TextView? = null

    }
}
