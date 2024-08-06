package com.example.katzen.Adapter.Cliente

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.example.katzen.DataBaseFirebase.FirebaseClienteUtil
import com.example.katzen.DataBaseFirebase.OnCompleteListener
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.ClienteModel
import com.example.katzen.R

class SeleccionClienteAdapter(
    activity: Activity,
    private var clienteList: List<ClienteModel>
) : ArrayAdapter<ClienteModel>(activity, R.layout.cliente_list_fragment, clienteList) {

    private var originalList: List<ClienteModel> = clienteList.toList()
    private var activity: Activity = activity
    private val TAG: String = "ClienteAdapter"

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        val holder: ViewHolder

        if (itemView == null) {
            itemView = LayoutInflater.from(activity).inflate(R.layout.cliente_list_fragment, parent, false)
            holder = ViewHolder().apply {
                imgPerfil = itemView.findViewById(R.id.imgPerfil)
                nombreCompletoTextView = itemView.findViewById(R.id.textViewNombreCompleto)
                expediente = itemView.findViewById(R.id.textExpediente)
                fondoTelefono = itemView.findViewById(R.id.fondoTelefono)
                fondoCorreo = itemView.findViewById(R.id.fondoCorreo)
                fondoUbicacion = itemView.findViewById(R.id.fondoUbicacion)
                btnEliminar = itemView.findViewById(R.id.btnEliminar)
            }
            itemView.tag = holder
        } else {
            holder = itemView.tag as ViewHolder
        }

        val cliente = clienteList[position]

        try {
            holder.btnEliminar?.visibility = View.GONE
            holder.nombreCompletoTextView?.text = ""
            holder.imgPerfil?.setImageResource(R.drawable.ic_person)

            holder.nombreCompletoTextView?.text = "${cliente.nombre} ${cliente.apellidoPaterno} ${cliente.apellidoMaterno}"
            holder.expediente?.text = cliente.expediente

            if (cliente.imageUrl.isNotEmpty()) {
                Glide.with(holder.imgPerfil!!.context)
                    .load(cliente.imageUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(holder.imgPerfil!!)
            } else {
                holder.imgPerfil?.setImageResource(R.drawable.ic_person)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            holder.imgPerfil?.setImageResource(R.drawable.ic_person)
        }

        holder.fondoTelefono?.setOnClickListener {
            try {
                UtilHelper.llamarCliente(activity, cliente.telefono)
            } catch (e: Exception) {
                e.printStackTrace()
                DialogMaterialHelper.mostrarErrorDialog(activity, "Error al intentar llamar al cliente.")
            }
        }

        holder.fondoCorreo?.setOnClickListener {
            try {
                enviarCorreoElectronico(cliente.correo)
            } catch (e: Exception) {
                e.printStackTrace()
                DialogMaterialHelper.mostrarErrorDialog(activity, "Error al intentar enviar el correo electrónico.")
            }
        }

        holder.fondoUbicacion?.setOnClickListener {
            try {
                UtilHelper.abrirGoogleMaps(activity, cliente.urlGoogleMaps)
            } catch (e: Exception) {
                e.printStackTrace()
                DialogMaterialHelper.mostrarErrorDialog(activity, "Error al intentar abrir Google Maps.")
            }
        }

        holder.btnEliminar?.setOnClickListener {
            try {
                FirebaseClienteUtil.eliminarCliente(cliente.id, object : OnCompleteListener {
                    override fun onComplete(success: Boolean, message: String) {
                        if (success) {
                            DialogMaterialHelper.mostrarSuccessDialog(activity, message)
                        } else {
                            DialogMaterialHelper.mostrarErrorDialog(activity, message)
                        }
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
                DialogMaterialHelper.mostrarErrorDialog(activity, "Error al intentar eliminar el cliente.")
            }
        }

        return itemView!!
    }

    private fun enviarCorreoElectronico(email: String) {
        try {
            if (email.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                    putExtra(Intent.EXTRA_SUBJECT, "Asunto del correo")
                    putExtra(Intent.EXTRA_TEXT, "Contenido del correo")
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
        } catch (e: Exception) {
            e.printStackTrace()
            DialogMaterialHelper.mostrarErrorDialog(activity, "Error al intentar enviar el correo electrónico.")
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
        var expediente: TextView? = null
        var fondoTelefono: ImageView? = null
        var fondoCorreo: ImageView? = null
        var fondoUbicacion: ImageView? = null
        var btnEliminar: LinearLayout? = null
    }
}
