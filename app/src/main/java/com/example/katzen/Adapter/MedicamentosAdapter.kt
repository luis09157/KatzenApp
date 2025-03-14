package com.example.katzen.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.katzen.Model.ProductoMedicamentoModel
import com.ninodev.katzen.R

class MedicamentosAdapter(
    private var medicamentosList: List<ProductoMedicamentoModel>,
    private val onItemClick: (ProductoMedicamentoModel) -> Unit,
    private val onDeleteClick: (ProductoMedicamentoModel) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = medicamentosList.size

    override fun getItem(position: Int): Any = medicamentosList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicamento, parent, false)
        
        val medicamento = medicamentosList[position]
        
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecio)
        val tvTipo: TextView = view.findViewById(R.id.tvTipo)
        val btnEstado: Button = view.findViewById(R.id.btnEstado)
        val btnEliminar: LinearLayout = view.findViewById(R.id.btnEliminar)
        
        tvNombre.text = medicamento.nombre
        tvPrecio.text = "Precio: $${medicamento.precio}"
        tvTipo.text = medicamento.tipo
        
        btnEstado.text = if (medicamento.activo) "Activo" else "Inactivo"
        btnEstado.backgroundTintList = view.context.getColorStateList(
            if (medicamento.activo) R.color.green_300 else R.color.grey_300
        )
        
        btnEliminar.setOnClickListener {
            onDeleteClick(medicamento)
        }
        
        view.setOnClickListener {
            onItemClick(medicamento)
        }
        
        return view
    }

    fun updateList(newList: List<ProductoMedicamentoModel>) {
        medicamentosList = newList
        notifyDataSetChanged()
    }
} 