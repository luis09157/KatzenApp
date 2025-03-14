package com.example.katzen.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.katzen.Model.ProcedimientoModel
import com.ninodev.katzen.R

class ProcedimientosAdapter(
    private var procedimientosList: List<ProcedimientoModel>,
    private val onItemClick: (ProcedimientoModel) -> Unit,
    private val onDeleteClick: (ProcedimientoModel) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = procedimientosList.size

    override fun getItem(position: Int): Any = procedimientosList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.item_procedimiento, parent, false)
        
        val procedimiento = procedimientosList[position]
        
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecio)
        val tvTipo: TextView = view.findViewById(R.id.tvTipo)
        val btnEstado: Button = view.findViewById(R.id.btnEstado)
        val btnEliminar: LinearLayout = view.findViewById(R.id.btnEliminar)
        
        tvNombre.text = procedimiento.nombre
        tvPrecio.text = "Precio: $${procedimiento.precioFinal}"
        tvTipo.text = "Tipo: ${procedimiento.tipo}"
        
        btnEstado.text = if (procedimiento.activo) "Activo" else "Inactivo"
        btnEstado.backgroundTintList = view.context.getColorStateList(
            if (procedimiento.activo) R.color.green_300 else R.color.grey_300
        )
        
        btnEliminar.setOnClickListener {
            onDeleteClick(procedimiento)
        }
        
        view.setOnClickListener {
            onItemClick(procedimiento)
        }
        
        return view
    }

    fun updateList(newList: List<ProcedimientoModel>) {
        procedimientosList = newList
        notifyDataSetChanged()
    }
} 