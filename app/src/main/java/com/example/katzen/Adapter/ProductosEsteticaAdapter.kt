package com.example.katzen.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.katzen.Model.ProductoEsteticaModel
import com.ninodev.katzen.R

class ProductosEsteticaAdapter(
    private var productosList: List<ProductoEsteticaModel>,
    private val onItemClick: (ProductoEsteticaModel) -> Unit,
    private val onDeleteClick: (ProductoEsteticaModel) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = productosList.size

    override fun getItem(position: Int): Any = productosList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto_estetica, parent, false)
        
        val producto = productosList[position]
        
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecio)
        val tvCodigo: TextView = view.findViewById(R.id.tvCodigo)
        val btnEstado: Button = view.findViewById(R.id.btnEstado)
        val btnEliminar: LinearLayout = view.findViewById(R.id.btnEliminar)
        
        tvNombre.text = producto.nombre
        tvPrecio.text = "Precio: $${producto.precioFinal}"
        tvCodigo.text = "Código: ${producto.codigoInterno}"
        
        btnEstado.text = if (producto.activo) "Activo" else "Inactivo"
        btnEstado.backgroundTintList = view.context.getColorStateList(
            if (producto.activo) R.color.green_300 else R.color.grey_300
        )
        
        btnEliminar.setOnClickListener {
            onDeleteClick(producto)
        }
        
        view.setOnClickListener {
            onItemClick(producto)
        }
        
        return view
    }

    fun updateList(newList: List<ProductoEsteticaModel>) {
        productosList = newList
        notifyDataSetChanged()
    }
} 