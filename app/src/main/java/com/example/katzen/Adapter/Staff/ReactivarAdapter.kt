package com.example.katzen.Adapter.Staff

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ninodev.katzen.R

data class ReactivarItem(
    val id: String,
    val titulo: String,
    val subtitulo: String
)

class ReactivarAdapter(
    private val onReactivar: (ReactivarItem) -> Unit
) : ListAdapter<ReactivarItem, ReactivarAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_item_reactivar, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titulo: TextView = itemView.findViewById(R.id.tv_titulo)
        private val subtitulo: TextView = itemView.findViewById(R.id.tv_subtitulo)
        private val btnReactivar: View = itemView.findViewById(R.id.btn_reactivar)

        fun bind(item: ReactivarItem) {
            titulo.text = item.titulo
            subtitulo.text = item.subtitulo
            btnReactivar.setOnClickListener { onReactivar(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ReactivarItem>() {
            override fun areItemsTheSame(oldItem: ReactivarItem, newItem: ReactivarItem) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ReactivarItem, newItem: ReactivarItem) =
                oldItem == newItem
        }
    }
}
