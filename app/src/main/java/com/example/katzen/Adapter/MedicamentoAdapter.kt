package com.example.katzen.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.katzen.Model.ProductoMedicamentoModel
import com.ninodev.katzen.R

/**
 * Adapter para mostrar los medicamentos en un RecyclerView
 */
class MedicamentoAdapter(
    private val medicamentos: List<ProductoMedicamentoModel>,
    private val onItemClick: (ProductoMedicamentoModel) -> Unit
) : RecyclerView.Adapter<MedicamentoAdapter.ViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicamento_seleccion, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val medicamento = medicamentos[position]
        holder.bind(medicamento)
    }
    
    override fun getItemCount(): Int = medicamentos.size
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombreMedicamento)
        private val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcionMedicamento)
        private val tvPrecio: TextView = itemView.findViewById(R.id.tvPrecioMedicamento)
        
        fun bind(medicamento: ProductoMedicamentoModel) {
            tvNombre.text = medicamento.nombre
            
            // Mostrar descripción si existe
            if (medicamento.descripcion.isNotEmpty()) {
                tvDescripcion.text = medicamento.descripcion
                tvDescripcion.visibility = View.VISIBLE
            } else {
                tvDescripcion.visibility = View.GONE
            }
            
            // Mostrar precio si existe
            if (medicamento.precio.isNotEmpty()) {
                tvPrecio.text = "Precio: $${medicamento.precio}"
                tvPrecio.visibility = View.VISIBLE
            } else {
                tvPrecio.visibility = View.GONE
            }
            
            // Configurar click
            itemView.setOnClickListener {
                onItemClick(medicamento)
            }
        }
    }
} 