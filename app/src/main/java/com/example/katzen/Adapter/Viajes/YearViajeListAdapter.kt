package com.example.katzen.Adapter.Viajes

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.example.katzen.Model.YearViajeModel
import com.ninodev.katzen.R

class YearViajeListAdapter(
    private val activity: FragmentActivity,
    private var yearList: List<YearViajeModel>
) : ArrayAdapter<YearViajeModel>(activity, R.layout.view_list_year, yearList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView ?: LayoutInflater.from(activity)
            .inflate(R.layout.view_list_year, parent, false)

        val year = yearList[position]
        
        Log.d("YearViajeListAdapter", "Creando vista para año: ${year.year}")
        
        // Configurar el año
        itemView.findViewById<TextView>(R.id.txtYear).apply {
            text = "Año ${year.year}"
            setTextColor(context.getColor(R.color.katzen_rosa))
        }

        // Configurar el contador de viajes
        itemView.findViewById<TextView>(R.id.txtCampañasCount).apply {
            text = when (year.viajesCount) {
                0 -> "No hay viajes registrados"
                1 -> "1 viaje registrado"
                else -> "${year.viajesCount} viajes registrados"
            }
            setTextColor(context.getColor(R.color.primary_dark))
        }

        return itemView
    }

    override fun getCount(): Int {
        Log.d("YearViajeListAdapter", "getCount llamado, tamaño: ${yearList.size}")
        return yearList.size
    }

    fun updateList(newList: List<YearViajeModel>) {
        yearList = newList
        notifyDataSetChanged()
    }
} 