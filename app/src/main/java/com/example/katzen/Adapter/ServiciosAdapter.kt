package com.example.katzen.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.example.katzen.Model.ServicioModel
import com.ninodev.katzen.R

class ServiciosAdapter(
    private var serviciosList: List<ServicioModel>,
    private val onItemClick: (ServicioModel) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = serviciosList.size

    override fun getItem(position: Int): Any = serviciosList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.item_servicio, parent, false)
        
        val servicio = serviciosList[position]
        
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecio)
        val tvCodigo: TextView = view.findViewById(R.id.tvCodigo)
        val btnEstado: Button = view.findViewById(R.id.btnEstado)
        
        tvNombre.text = servicio.nombre
        tvPrecio.text = "Precio: $${servicio.precioFinal}"
        tvCodigo.text = "Código: ${servicio.codigoInterno}"
        
        btnEstado.text = if (servicio.activo) "Activo" else "Inactivo"
        btnEstado.backgroundTintList = view.context.getColorStateList(
            if (servicio.activo) R.color.green_300 else R.color.grey_300
        )
        
        view.setOnClickListener {
            onItemClick(servicio)
        }
        
        return view
    }

    fun updateList(newList: List<ServicioModel>) {
        serviciosList = newList
        notifyDataSetChanged()
    }
} 