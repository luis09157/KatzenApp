package com.example.katzen.Adapter.Cliente

import android.app.Activity
import android.content.Intent
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.katzen.DataBaseFirebase.FirebaseClienteUtil
import com.example.katzen.DataBaseFirebase.OnCompleteListener
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.ClienteModel
import com.example.katzen.R
import com.squareup.picasso.Picasso

class SeleccionClienteAdapter (
    activity: Activity,
    private var clienteList: List<ClienteModel>
) : ArrayAdapter<ClienteModel>(activity, R.layout.cliente_list_fragment, clienteList) {

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

        holder.btnEliminar!!.visibility = View.GONE
        holder.nombreCompletoTextView?.text = ""
        holder.telefonoTextView?.text = ""
        holder.correoTextView?.text = ""
        holder.ubicacionTextView?.text = ""
        holder.imgPerfil?.setImageResource(R.drawable.ic_perfil)

        holder.nombreCompletoTextView?.text = "${cliente.nombre} ${cliente.apellidoPaterno} ${cliente.apellidoMaterno}"
        holder.telefonoTextView?.text = cliente.telefono
        holder.correoTextView?.text = cliente.correo
        holder.ubicacionTextView?.text = cliente.urlGoogleMaps

        if (cliente.imageUrl.isNotEmpty()) {
            Picasso.get()
                .load(cliente.imageUrl)
                .placeholder(R.drawable.ic_perfil) // Establecer la imagen predeterminada
                .error(R.drawable.no_disponible_rosa) // Opcional: establecer una imagen en caso de error al cargar
                .into(holder.imgPerfil)
        } else {
            holder.imgPerfil?.setImageResource(R.drawable.no_disponible_rosa)
        }

        holder.fondoTelefono!!.setOnClickListener {
            UtilHelper.llamarCliente(activity, cliente.telefono)
        }
        holder.telefonoTextView!!.setOnClickListener {
            UtilHelper.llamarCliente(activity, cliente.telefono)
        }

        holder.correoTextView!!.setOnClickListener {
            enviarCorreoElectronico(cliente.correo)
        }
        holder.fondoCorreo!!.setOnClickListener {
            enviarCorreoElectronico(cliente.correo)
        }
        holder.ubicacionTextView!!.setOnClickListener {
            UtilHelper.abrirGoogleMaps(activity ,cliente.urlGoogleMaps)
        }
        holder.fondoUbicacion!!.setOnClickListener {
            UtilHelper.abrirGoogleMaps(activity, cliente.urlGoogleMaps)
        }
        holder.btnEliminar!!.setOnClickListener {
            FirebaseClienteUtil.eliminarCliente(cliente.id , object :
                OnCompleteListener {
                override fun onComplete(success: Boolean, message: String) {
                    if (success) {
                        DialogMaterialHelper.mostrarSuccessDialog(activity, message)
                    } else {
                        DialogMaterialHelper.mostrarErrorDialog(activity, message)
                    }
                }
            })

        }

        return itemView!!
    }


    fun enviarCorreoElectronico(email: String) {
        if (email.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, "Asunto del correo")
                putExtra(Intent.EXTRA_TEXT, "Contenido del correo")
                // Especificamos que queremos enviar el correo a través de Gmail
                setPackage("com.google.android.gm")
            }
            if (intent.resolveActivity(activity.packageManager) != null) {
                activity.startActivity(intent)
            } else {
                DialogMaterialHelper.mostrarErrorDialog(activity, "No se pudo abrir la aplicación de Gmail")
            }
        } else {
            DialogMaterialHelper.mostrarErrorDialog(activity, "No tiene un correo relacionado.")
        }
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
