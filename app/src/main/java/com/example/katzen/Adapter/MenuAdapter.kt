package com.example.katzen.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.katzen.Model.MenuModel
import com.ninodev.katzen.R

class MenuAdapter(
    private val onItemClick: (MenuModel) -> Unit
) : ListAdapter<MenuModel, MenuAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_view_gridview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateList(newList: List<MenuModel>) {
        submitList(newList.toList())
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imagen: ImageView = itemView.findViewById(R.id.imagen)
        private val titulo: TextView = itemView.findViewById(R.id.titulo)

        init {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(pos))
                }
            }
        }

        fun bind(menu: MenuModel) {
            titulo.text = menu.titulo
            imagen.setImageResource(menu.imagen)
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MenuModel>() {
            override fun areItemsTheSame(oldItem: MenuModel, newItem: MenuModel): Boolean {
                return oldItem.titulo == newItem.titulo
            }

            override fun areContentsTheSame(oldItem: MenuModel, newItem: MenuModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}
