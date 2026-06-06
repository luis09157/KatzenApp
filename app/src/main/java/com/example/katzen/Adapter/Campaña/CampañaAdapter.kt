package com.example.katzen.Adapter.Campaña

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.katzen.Model.CampañaModel
import com.ninodev.katzen.R

class CampañaAdapter(
    private val onItemClick: (Int, CampañaModel) -> Unit
) : ListAdapter<CampañaModel, CampañaAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.view_detalle_calendario, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position), position)

    fun updateList(newList: List<CampañaModel>) = submitList(newList.toList())

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mesTextView: TextView = itemView.findViewById(R.id.mesTextView)
        private val cantidadEventos: TextView = itemView.findViewById(R.id.eventosTextView)

        fun bind(campaña: CampañaModel, position: Int) {
            mesTextView.text = campaña.mes
            cantidadEventos.text = "${campaña.cantidadCampañas} Eventos"
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onItemClick(pos, getItem(pos))
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CampañaModel>() {
            override fun areItemsTheSame(a: CampañaModel, b: CampañaModel) = a.mes == b.mes
            override fun areContentsTheSame(a: CampañaModel, b: CampañaModel) = a == b
        }
    }
}
