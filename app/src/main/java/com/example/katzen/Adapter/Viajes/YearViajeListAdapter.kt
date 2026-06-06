package com.example.katzen.Adapter.Viajes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.katzen.Model.YearViajeModel
import com.ninodev.katzen.R

class YearViajeListAdapter(
    private val onItemClick: (YearViajeModel) -> Unit
) : ListAdapter<YearViajeModel, YearViajeListAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.view_list_year, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    fun updateList(newList: List<YearViajeModel>) = submitList(newList.toList())

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtYear: TextView = itemView.findViewById(R.id.txtYear)
        private val txtCount: TextView = itemView.findViewById(R.id.txtCampañasCount)

        fun bind(year: YearViajeModel) {
            txtYear.text = "Año ${year.year}"
            txtYear.setTextColor(itemView.context.getColor(R.color.katzen_rosa))
            txtCount.text = when (year.viajesCount) {
                0 -> "No hay viajes registrados"
                1 -> "1 viaje registrado"
                else -> "${year.viajesCount} viajes registrados"
            }
            txtCount.setTextColor(itemView.context.getColor(R.color.primary_dark))
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onItemClick(getItem(pos))
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<YearViajeModel>() {
            override fun areItemsTheSame(a: YearViajeModel, b: YearViajeModel) = a.year == b.year
            override fun areContentsTheSame(a: YearViajeModel, b: YearViajeModel) = a == b
        }
    }
}
