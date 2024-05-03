package com.example.katzen.Adapter.Paciente

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.katzen.Model.ClienteModel
import com.example.katzen.R

class PacienteMenuAdapter(
    activity: Activity,
    private var clienteList: List<ClienteModel>
) : ArrayAdapter<ClienteModel>(activity, R.layout.mascota_list_fragment, clienteList) {

    private var originalList: List<ClienteModel> = clienteList.toList()
    var activity : Activity = activity
    var TAG : String = "ClienteAdapter"

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        val holder: ViewHolder

        if (itemView == null) {
            itemView = LayoutInflater.from(activity).inflate(R.layout.cliente_list_fragment, parent, false)
            holder = ViewHolder()
            holder.imgPerfil = itemView.findViewById(R.id.imgPerfil)
            holder.nombreCompletoTextView = itemView.findViewById(R.id.textViewNombreCompleto)
            holder.telefonoTextView = itemView.findViewById(R.id.textViewTelefono)
            holder.correoTextView = itemView.findViewById(R.id.textViewCorreo)
            holder.ubicacionTextView = itemView.findViewById(R.id.textViewUbicacion)

            holder.fondoTelefono = itemView.findViewById(R.id.fondoTelefono)
            holder.fondoCorreo = itemView.findViewById(R.id.fondoCorreo)
            holder.fondoUbicacion = itemView.findViewById(R.id.fondoUbicacion)
            holder.btnEliminar = itemView.findViewById(R.id.btnEliminar)
            itemView.tag = holder
        } else {
            holder = itemView.tag as ViewHolder
        }
        val cliente = clienteList[position]

        holder.nombreCompletoTextView?.text = ""
        holder.telefonoTextView?.text = ""
        holder.correoTextView?.text = ""
        holder.ubicacionTextView?.text = ""
        holder.imgPerfil?.setImageResource(R.drawable.ic_perfil)

        holder.nombreCompletoTextView?.text = "${cliente.nombre} ${cliente.apellidoPaterno} ${cliente.apellidoMaterno}"
        holder.telefonoTextView?.text = cliente.telefono
        holder.correoTextView?.text = cliente.correo
        holder.ubicacionTextView?.text = cliente.urlGoogleMaps



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
        var fondoTelefono: ImageView? = null
        var fondoCorreo: ImageView? = null
        var fondoUbicacion: ImageView? = null
        var btnEliminar: CardView? = null

    }
}
