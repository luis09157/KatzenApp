package com.example.katzen.Adapter.Viaje

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.katzen.Model.VentaMesModel
import com.ninodev.katzen.R

class ViajeAdapter(
    private val onItemClick: (VentaMesModel) -> Unit
) : ListAdapter<VentaMesModel, ViajeAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.vista_viajes, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    fun updateList(newList: List<VentaMesModel>) = submitList(newList.toList())

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtMes: TextView = itemView.findViewById(R.id.txt_mes)
        private val txtCosto: TextView = itemView.findViewById(R.id.txt_costo)
        private val txtGanancia: TextView = itemView.findViewById(R.id.txt_ganancia)
        private val txtVenta: TextView = itemView.findViewById(R.id.txt_venta)

        fun bind(viaje: VentaMesModel) {
            txtMes.text = viaje.mes
            txtCosto.text = "$${viaje.costo}"
            txtGanancia.text = "$${viaje.ganancia}"
            txtVenta.text = "$${viaje.venta}"
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onItemClick(getItem(pos))
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<VentaMesModel>() {
            override fun areItemsTheSame(a: VentaMesModel, b: VentaMesModel) = a.id == b.id
            override fun areContentsTheSame(a: VentaMesModel, b: VentaMesModel) = a == b
        }
    }
}
