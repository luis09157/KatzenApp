package com.example.katzen.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import com.example.katzen.Model.ClienteModel
import com.example.katzen.R
import com.squareup.picasso.Picasso

class ClienteAdapter(
    context: Context,
    private var clienteList: List<ClienteModel>
) : ArrayAdapter<ClienteModel>(context, R.layout.cliente_list_fragment, clienteList) {

    private var originalList: List<ClienteModel> = clienteList.toList()

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
            holder = itemView.tag as ClienteAdapter.ViewHolder
        }

        val cliente = clienteList[position]

        holder.nombreCompletoTextView?.text = "${cliente.nombre} ${cliente.apellidoPaterno} ${cliente.apellidoMaterno}"
        holder.telefonoTextView?.text = cliente.telefono
        holder.correoTextView?.text = cliente.correo
        holder.ubicacionTextView?.text = cliente.urlGoogleMaps

        if (cliente.imageUrl.isNotEmpty()) {
            Picasso.get().load(cliente.imageUrl).into(holder.imgPerfil)
        } else {
            holder.imgPerfil?.setImageResource(R.drawable.no_disponible_rosa)
        }


        return itemView!!
    }

    override fun getCount(): Int {
        return clienteList.size
    }

    override fun getItem(position: Int): ClienteModel? {
        return clienteList[position]
    }
    fun updateList(newList: List<ClienteModel>) {
        clienteList = newList
        originalList = newList.toList()
        notifyDataSetChanged()
    }

    private class ViewHolder {
        var imgPerfil: ImageView? = null
        var nombreCompletoTextView: TextView? = null
        var telefonoTextView: TextView? = null
        var correoTextView: TextView? = null
        var ubicacionTextView: TextView? = null
    }
}
