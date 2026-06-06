package com.example.katzen.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.katzen.Model.ProductoMedicamentoModel
import com.ninodev.katzen.R

class MedicamentosAdapter(
    private val onItemClick: (ProductoMedicamentoModel) -> Unit,
    private val onDeleteClick: (ProductoMedicamentoModel) -> Unit
) : ListAdapter<ProductoMedicamentoModel, MedicamentosAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicamento, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateList(newList: List<ProductoMedicamentoModel>) {
        submitList(newList.toList())
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        private val tvPrecio: TextView = itemView.findViewById(R.id.tvPrecio)
        private val tvTipo: TextView = itemView.findViewById(R.id.tvTipo)
        private val btnEstado: Button = itemView.findViewById(R.id.btnEstado)
        private val btnEliminar: View = itemView.findViewById(R.id.btnEliminar)

        init {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onItemClick(getItem(pos))
            }
            btnEliminar.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onDeleteClick(getItem(pos))
            }
        }

        fun bind(medicamento: ProductoMedicamentoModel) {
            tvNombre.text = medicamento.nombre
            tvPrecio.text = "Precio: $${medicamento.precio}"
            tvTipo.text = medicamento.tipo
            btnEstado.text = if (medicamento.activo) "Activo" else "Inactivo"
            btnEstado.backgroundTintList = itemView.context.getColorStateList(
                if (medicamento.activo) R.color.green_300 else R.color.grey_300
            )
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ProductoMedicamentoModel>() {
            override fun areItemsTheSame(oldItem: ProductoMedicamentoModel, newItem: ProductoMedicamentoModel) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ProductoMedicamentoModel, newItem: ProductoMedicamentoModel) =
                oldItem == newItem
        }
    }
}
