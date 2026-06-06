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
import com.example.katzen.Model.ServicioModel
import com.ninodev.katzen.R

class ServiciosAdapter(
    private val onItemClick: (ServicioModel) -> Unit,
    private val onDeleteClick: (ServicioModel) -> Unit
) : ListAdapter<ServicioModel, ServiciosAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_servicio, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    fun updateList(newList: List<ServicioModel>) = submitList(newList.toList())

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

        fun bind(servicio: ServicioModel) {
            tvNombre.text = servicio.nombre
            tvPrecio.text = "Precio: $${servicio.precioFinal}"
            tvCodigo.text = "Código: ${servicio.codigoInterno}"
            btnEstado.text = if (servicio.activo) "Activo" else "Inactivo"
            btnEstado.backgroundTintList = itemView.context.getColorStateList(
                if (servicio.activo) R.color.green_300 else R.color.grey_300
            )
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ServicioModel>() {
            override fun areItemsTheSame(a: ServicioModel, b: ServicioModel) = a.id == b.id
            override fun areContentsTheSame(a: ServicioModel, b: ServicioModel) = a == b
        }
    }
}
