package com.example.katzen.Adapter.Campaña

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.example.katzen.Model.YearModel
import com.ninodev.katzen.R

class YearListAdapter(
    private val activity: FragmentActivity,
    private var yearList: List<YearModel>
) : ArrayAdapter<YearModel>(activity, R.layout.view_list_year, yearList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView = convertView ?: LayoutInflater.from(activity)
            .inflate(R.layout.view_list_year, parent, false)

        val year = yearList[position]
        
        Log.d("YearListAdapter", "Creando vista para año: ${year.year}")
        
        // Configurar el año
        itemView.findViewById<TextView>(R.id.txtYear).apply {
            text = "Año ${year.year}"
            setTextColor(context.getColor(R.color.katzen_rosa))
        }

        // Configurar el contador de campañas
        itemView.findViewById<TextView>(R.id.txtCampañasCount).apply {
            text = when (year.campañasCount) {
                0 -> "No hay campañas registradas"
                1 -> "1 campaña registrada"
                else -> "${year.campañasCount} campañas registradas"
            }
            setTextColor(context.getColor(R.color.primary_dark))
        }

        // Aplicar estilo visual según si hay campañas o no
        itemView.alpha = if (year.campañasCount > 0) 1.0f else 0.7f

        return itemView
    }

    override fun getCount(): Int {
        Log.d("YearListAdapter", "getCount llamado, tamaño: ${yearList.size}")
        return yearList.size
    }

    fun updateList(newList: List<YearModel>) {
        yearList = newList
        notifyDataSetChanged()
    }
} 