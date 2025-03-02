package com.example.katzen.Adapter.Campaña

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.example.katzen.DataBaseFirebase.FirebaseCampañaUtil
import com.example.katzen.Fragment.Campaña.CampañaFragment
import com.example.katzen.Helper.CalendarioUtil
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Model.CampañaModel
import com.ninodev.katzen.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CampañaEventoAdapter(
    activity: Activity,
    private var campañaList: List<CampañaModel>
) : ArrayAdapter<CampañaModel>(activity, R.layout.view_campania_evento_detalle, campañaList) {

    private var originalList: List<CampañaModel> = campañaList.toList()
    var activity : Activity = activity
    var TAG : String = "CampañaEventoAdapter"

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        val holder: ViewHolder

        if (itemView == null) {
            itemView = LayoutInflater.from(activity).inflate(R.layout.view_campania_evento_detalle, parent, false)
            holder = ViewHolder()

            holder.diaTextView = itemView.findViewById(R.id.textViewDia)
            holder.fechaTextView = itemView.findViewById(R.id.textViewFecha)
            holder.cantidadPacientesTextView = itemView.findViewById(R.id.textViewCantidadPacientes)
            holder.btnEliminar = itemView.findViewById(R.id.btnEliminar)

            itemView.tag = holder
        } else {
            holder = itemView.tag as ViewHolder
        }

        val campaña = campañaList[position]

        holder.fechaTextView?.text = campaña.fecha
        holder.cantidadPacientesTextView?.text = "${campaña.cantidadPacientes} Pacientes"
        holder.diaTextView?.text = CalendarioUtil.obtenerDiaDesdeString(campaña.fecha)

        holder.btnEliminar?.setOnClickListener {
            activity.runOnUiThread {
                DialogMaterialHelper.mostrarConfirmDialog(activity, "¿Estás seguro de que deseas eliminar la campaña?") { confirmed ->
                    if (confirmed) {
                        CoroutineScope(Dispatchers.IO).launch {
                            CampañaFragment.ADD_CAMPAÑA.id = campaña.id
                            val (success, message) = FirebaseCampañaUtil.eliminarCampaña()
                            withContext(Dispatchers.Main) {
                                if (success) {
                                    DialogMaterialHelper.mostrarSuccessDialog(activity, message)
                                    notifyDataSetChanged()
                                    //updateList(originalList.filter { it.id != campaña.id })
                                } else {
                                    DialogMaterialHelper.mostrarErrorDialog(activity, message)
                                }
                            }
                        }
                    }
                }
            }
        }

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
        var fechaTextView: TextView? = null
        var cantidadPacientesTextView: TextView? = null
        var diaTextView: TextView? = null
        var btnEliminar: LinearLayout? = null

    }
}
