package com.example.katzen.Adapter.Producto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.katzen.Helper.ImageLoaderHelper
import com.example.katzen.Model.ProductoModel
import com.ninodev.katzen.R

class ProductoInventarioAdapter(
    private val onItemClick: (ProductoModel) -> Unit
) : ListAdapter<ProductoModel, ProductoInventarioAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu_inventario, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.clearImage()
        super.onViewRecycled(holder)
    }

    fun updateList(newList: List<ProductoModel>) {
        submitList(newList.toList())
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imagen)
        private val nombreTextView: TextView = itemView.findViewById(R.id.textViewNombre)
        private val cantidadTextView: TextView = itemView.findViewById(R.id.textViewCantidad)

        init {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(pos))
                }
            }
        }

        fun bind(producto: ProductoModel) {
            nombreTextView.text = producto.nombre
            cantidadTextView.text = "${producto.cantidadInventario} Piezas"

            if (producto.rutaImagen.isNotEmpty()) {
                ImageLoaderHelper.loadListImage(
                    imageView = imageView,
                    progressBar = null,
                    imageUrl = producto.rutaImagen,
                    placeholderRes = R.drawable.ic_loading_michi,
                    errorRes = R.drawable.no_disponible_rosa
                )
            } else {
                imageView.setImageResource(R.drawable.no_disponible_rosa)
            }
        }

        fun clearImage() {
            ImageLoaderHelper.clearListImage(imageView, null, R.drawable.ic_loading_michi)
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ProductoModel>() {
            override fun areItemsTheSame(oldItem: ProductoModel, newItem: ProductoModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ProductoModel, newItem: ProductoModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}
