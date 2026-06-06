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
import com.example.katzen.Model.ProcedimientoModel
import com.ninodev.katzen.R

class ProcedimientosAdapter(
    private val onItemClick: (ProcedimientoModel) -> Unit,
    private val onDeleteClick: (ProcedimientoModel) -> Unit
) : ListAdapter<ProcedimientoModel, ProcedimientosAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_procedimiento, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    fun updateList(newList: List<ProcedimientoModel>) = submitList(newList.toList())

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

        fun bind(procedimiento: ProcedimientoModel) {
            tvNombre.text = procedimiento.nombre
            tvPrecio.text = "Precio: $${procedimiento.precioFinal}"
            tvTipo.text = "Tipo: ${procedimiento.tipo}"
            btnEstado.text = if (procedimiento.activo) "Activo" else "Inactivo"
            btnEstado.backgroundTintList = itemView.context.getColorStateList(
                if (procedimiento.activo) R.color.green_300 else R.color.grey_300
            )
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ProcedimientoModel>() {
            override fun areItemsTheSame(a: ProcedimientoModel, b: ProcedimientoModel) = a.id == b.id
            override fun areContentsTheSame(a: ProcedimientoModel, b: ProcedimientoModel) = a == b
        }
    }
}
