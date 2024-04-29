package com.example.katzen.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.katzen.Model.ClienteModel
import com.example.katzen.R
import com.squareup.picasso.Picasso

class ClienteAdapter(context: Context, private val productList: List<ClienteModel>) :
    ArrayAdapter<ClienteModel>(context, R.layout.cliente_list_fragment, productList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        val holder: ViewHolder

        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.cliente_list_fragment, parent, false)
            holder = ViewHolder()
            holder.imgPerfil = itemView.findViewById(R.id.imgPerfil)
            holder.nombreCompletoTextView = itemView.findViewById(R.id.textViewNombreCompleto)
            holder.telefonoTextView = itemView.findViewById(R.id.textViewTelefono)
            holder.correoTextView = itemView.findViewById(R.id.textViewCorreo)
            holder.ubicacionTextView = itemView.findViewById(R.id.textViewUbicacion)
            itemView.tag = holder
        } else {
            holder = itemView.tag as ViewHolder
        }

        val producto = productList[position]

        holder.nombreCompletoTextView?.text = ""
        holder.telefonoTextView?.text = ""
        holder.correoTextView?.text = ""
        holder.ubicacionTextView?.text = ""
        holder.imgPerfil?.setImageResource(R.drawable.img_venta)

        // Asigna los valores del producto a las vistas correspondientes
        holder.nombreCompletoTextView?.text =  "${producto.nombre} ${producto.apellidoPaterno} ${producto.apellidoMaterno}"
        holder.telefonoTextView?.text = producto.telefono
        holder.correoTextView?.text = producto.correo
        holder.ubicacionTextView?.text = producto.urlGoogleMaps

        // Cargar la imagen del producto utilizando Picasso
        if (producto.imageUrl.isNotEmpty()) {
            // Si hay una URL de imagen disponible, cargar desde la URL
            Picasso.get().load(producto.imageUrl).into(holder.imgPerfil)
        } else {
            // Si no hay una URL de imagen, cargar desde el recurso drawable
            holder.imgPerfil?.setImageResource(R.drawable.no_disponible_rosa)
        }

        return itemView!!
    }

    private class ViewHolder {
        var imgPerfil: ImageView? = null
        var nombreCompletoTextView: TextView? = null
        var telefonoTextView: TextView? = null
        var correoTextView: TextView? = null
        var ubicacionTextView: TextView? = null
    }
}
