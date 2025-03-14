package com.example.katzen.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.ninodev.katzen.Model.ProductoAlimentoModel
import com.ninodev.katzen.R
// Importación alternativa si el paquete es diferente
// import com.ninodev.katzen.Model.ProductoAlimentoModel

class AlimentosAdapter(
    private var alimentosList: List<ProductoAlimentoModel>,
    private val onItemClick: (ProductoAlimentoModel) -> Unit,
    private val onDeleteClick: (ProductoAlimentoModel) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = alimentosList.size

    override fun getItem(position: Int): Any = alimentosList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alimento, parent, false)
        
        val alimento = alimentosList[position]
        
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecio)
        val tvCodigo: TextView = view.findViewById(R.id.tvCodigo)
        val btnEstado: Button = view.findViewById(R.id.btnEstado)
        val btnEliminar: LinearLayout = view.findViewById(R.id.btnEliminar)
        
        tvNombre.text = alimento.nombre
        tvPrecio.text = "Precio: $${alimento.precioFinal}"
        tvCodigo.text = "Código: ${alimento.codigoInterno}"
        
        btnEstado.text = if (alimento.activo) "Activo" else "Inactivo"
        btnEstado.backgroundTintList = view.context.getColorStateList(
            if (alimento.activo) R.color.green_300 else R.color.grey_300
        )
        
        btnEliminar.setOnClickListener {
            onDeleteClick(alimento)
        }
        
        view.setOnClickListener {
            onItemClick(alimento)
        }
        
        return view
    }

    fun updateList(newList: List<ProductoAlimentoModel>) {
        alimentosList = newList
        notifyDataSetChanged()
    }
} 