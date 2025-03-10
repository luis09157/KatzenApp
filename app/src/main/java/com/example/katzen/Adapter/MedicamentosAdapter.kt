package com.example.katzen.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.katzen.Model.ProductoMedicamentoModel
import com.ninodev.katzen.databinding.ItemMedicamentoBinding

class MedicamentosAdapter(
    private val medicamentos: List<ProductoMedicamentoModel>,
    private val onItemClick: (ProductoMedicamentoModel) -> Unit
) : RecyclerView.Adapter<MedicamentosAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemMedicamentoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMedicamentoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val medicamento = medicamentos[position]
        with(holder.binding) {
            tvNombre.text = medicamento.nombre
            tvPrecio.text = "Precio: $${medicamento.precio}"
            tvTipo.text = medicamento.tipo
            chipEstado.isChecked = medicamento.activo
            
            root.setOnClickListener { onItemClick(medicamento) }
        }
    }

    override fun getItemCount() = medicamentos.size
} 