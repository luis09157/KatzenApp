package com.example.katzen.Adapter.Campaña

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.katzen.Model.YearModel
import com.ninodev.katzen.R

class YearListAdapter(
    private val onItemClick: (YearModel) -> Unit
) : ListAdapter<YearModel, YearListAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.view_list_year, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    fun updateList(newList: List<YearModel>) = submitList(newList.toList())

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtYear: TextView = itemView.findViewById(R.id.txtYear)
        private val txtCount: TextView = itemView.findViewById(R.id.txtCampañasCount)

        fun bind(year: YearModel) {
            txtYear.text = "Año ${year.year}"
            txtYear.setTextColor(itemView.context.getColor(R.color.katzen_rosa))
            txtCount.text = when (year.campañasCount) {
                0 -> "No hay campañas registradas"
                1 -> "1 campaña registrada"
                else -> "${year.campañasCount} campañas registradas"
            }
            txtCount.setTextColor(itemView.context.getColor(R.color.primary_dark))
            itemView.alpha = if (year.campañasCount > 0) 1.0f else 0.7f
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onItemClick(getItem(pos))
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<YearModel>() {
            override fun areItemsTheSame(a: YearModel, b: YearModel) = a.year == b.year
            override fun areContentsTheSame(a: YearModel, b: YearModel) = a == b
        }
    }
}
