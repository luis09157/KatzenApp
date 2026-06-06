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
import com.example.katzen.Model.ProductoAlimentoModel
import com.ninodev.katzen.R

class AlimentosAdapter(
    private val onItemClick: (ProductoAlimentoModel) -> Unit,
    private val onDeleteClick: (ProductoAlimentoModel) -> Unit
) : ListAdapter<ProductoAlimentoModel, AlimentosAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alimento, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateList(newList: List<ProductoAlimentoModel>) {
        submitList(newList.toList())
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        private val tvPrecio: TextView = itemView.findViewById(R.id.tvPrecio)
        private val tvCodigo: TextView = itemView.findViewById(R.id.tvCodigo)
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

        fun bind(alimento: ProductoAlimentoModel) {
            tvNombre.text = alimento.nombre
            tvPrecio.text = "Precio: $${alimento.precioFinal}"
            tvCodigo.text = "Código: ${alimento.codigoInterno}"
            btnEstado.text = if (alimento.activo) "Activo" else "Inactivo"
            btnEstado.backgroundTintList = itemView.context.getColorStateList(
                if (alimento.activo) R.color.green_300 else R.color.grey_300
            )
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ProductoAlimentoModel>() {
            override fun areItemsTheSame(oldItem: ProductoAlimentoModel, newItem: ProductoAlimentoModel) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ProductoAlimentoModel, newItem: ProductoAlimentoModel) =
                oldItem == newItem
        }
    }
}
