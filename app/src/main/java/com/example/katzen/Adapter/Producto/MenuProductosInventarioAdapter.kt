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

class MenuProductosInventarioAdapter(
    private val onItemClick: (ProductoModel) -> Unit = {}
) : ListAdapter<ProductoModel, MenuProductosInventarioAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.producto_venta_view_fragment, parent, false)
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
        private val precioTextView: TextView = itemView.findViewById(R.id.textViewPrecio)

        fun bind(producto: ProductoModel) {
            nombreTextView.text = producto.nombre
            precioTextView.text = "$${producto.precioVenta}"

            if (producto.rutaImagen.isNotEmpty()) {
                ImageLoaderHelper.loadListImage(
                    imageView = imageView,
                    progressBar = null,
                    imageUrl = producto.rutaImagen,
                    placeholderRes = R.drawable.ic_imagen,
                    errorRes = R.drawable.ic_imagen
                )
            } else {
                imageView.setImageResource(R.drawable.no_disponible_rosa)
            }

            itemView.setOnClickListener { onItemClick(producto) }
        }

        fun clearImage() {
            ImageLoaderHelper.clearListImage(imageView, null, R.drawable.ic_imagen)
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
