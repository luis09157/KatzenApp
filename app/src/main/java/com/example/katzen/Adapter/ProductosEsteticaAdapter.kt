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
import com.example.katzen.Model.ProductoEsteticaModel
import com.ninodev.katzen.R

class ProductosEsteticaAdapter(
    private val onItemClick: (ProductoEsteticaModel) -> Unit,
    private val onDeleteClick: (ProductoEsteticaModel) -> Unit
) : ListAdapter<ProductoEsteticaModel, ProductosEsteticaAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_producto_estetica, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    fun updateList(newList: List<ProductoEsteticaModel>) = submitList(newList.toList())

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

        fun bind(producto: ProductoEsteticaModel) {
            tvNombre.text = producto.nombre
            tvPrecio.text = "Precio: $${producto.precioFinal}"
            tvCodigo.text = "Código: ${producto.codigoInterno}"
            btnEstado.text = if (producto.activo) "Activo" else "Inactivo"
            btnEstado.backgroundTintList = itemView.context.getColorStateList(
                if (producto.activo) R.color.green_300 else R.color.grey_300
            )
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ProductoEsteticaModel>() {
            override fun areItemsTheSame(a: ProductoEsteticaModel, b: ProductoEsteticaModel) = a.id == b.id
            override fun areContentsTheSame(a: ProductoEsteticaModel, b: ProductoEsteticaModel) = a == b
        }
    }
}
