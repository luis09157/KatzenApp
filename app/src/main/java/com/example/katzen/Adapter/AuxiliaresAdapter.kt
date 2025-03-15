package com.example.katzen.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.katzen.Model.AuxiliarModel
import com.ninodev.katzen.R

class AuxiliaresAdapter(
    private var auxiliaresList: List<AuxiliarModel>,
    private val onItemClick: (AuxiliarModel) -> Unit,
    private val onDeleteClick: (AuxiliarModel) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = auxiliaresList.size

    override fun getItem(position: Int): Any = auxiliaresList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.item_auxiliar, parent, false)
        
        val auxiliar = auxiliaresList[position]
        
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvCodigo: TextView = view.findViewById(R.id.tvCodigo)
        val btnEstado: Button = view.findViewById(R.id.btnEstado)
        val btnEliminar: LinearLayout = view.findViewById(R.id.btnEliminar)
        
        tvNombre.text = auxiliar.nombre
        tvCodigo.text = "Código: ${auxiliar.codigoInterno}"
        
        btnEstado.text = if (auxiliar.activo) "Activo" else "Inactivo"
        btnEstado.backgroundTintList = view.context.getColorStateList(
            if (auxiliar.activo) R.color.green_300 else R.color.grey_300
        )
        
        btnEliminar.setOnClickListener {
            onDeleteClick(auxiliar)
        }
        
        view.setOnClickListener {
            onItemClick(auxiliar)
        }
        
        return view
    }

    fun updateList(newList: List<AuxiliarModel>) {
        auxiliaresList = newList
        notifyDataSetChanged()
    }
} 