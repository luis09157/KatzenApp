package com.example.katzen.Adapter.Cliente

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.katzen.DataBaseFirebase.FirebaseClienteUtil
import com.example.katzen.DataBaseFirebase.FirebasePacienteUtil
import com.example.katzen.DataBaseFirebase.OnCompleteListener
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.ClienteModel
import com.example.katzen.R
import com.google.firebase.database.DatabaseReference
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClienteListAdapter (
    activity: Activity,
    private var clienteList: List<ClienteModel>
) : ArrayAdapter<ClienteModel>(activity, R.layout.view_list_paciente, clienteList) {

    private var originalList: List<ClienteModel> = clienteList.toList()
    var activity : Activity = activity
    var TAG : String = "ClienteListAdapter"
    private lateinit var clienteReference: DatabaseReference
    private lateinit var pacienteReference: DatabaseReference

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        val holder: ViewHolder

        if (itemView == null) {
            itemView = LayoutInflater.from(activity).inflate(R.layout.view_list_paciente, parent, false)
            holder = ViewHolder()
            holder.imgPerfil = itemView.findViewById(R.id.imgPerfil)
            holder.nombreCliente = itemView.findViewById(R.id.text_nombre)
            holder.descripcion = itemView.findViewById(R.id.text_descripcion)
            holder.btnEliminar = itemView.findViewById(R.id.btnEliminar)
            itemView.tag = holder
        } else {
            holder = itemView.tag as ViewHolder
        }
        val paciente = clienteList[position]

        holder.nombreCliente?.text = ""
        holder.descripcion?.text = ""
        holder.imgPerfil?.setImageResource(R.drawable.ic_person)

        if (paciente.imageUrl.isNotEmpty()) {
            Picasso.get()
                .load(paciente.imageUrl)
                .placeholder(R.drawable.ic_person) // Establecer la imagen predeterminada
                .error(R.drawable.ic_person)
                .into(holder.imgPerfil)
        } else {
            holder.imgPerfil?.setImageResource(R.drawable.ic_person)
        }

        holder.nombreCliente?.text = "${paciente.nombre}"
        holder.descripcion?.text = "Tel: ${paciente.telefono}"

        holder.descripcion!!.setOnClickListener {
            UtilHelper.enviarMensajeWhatsApp(activity, paciente.telefono)
        }

        holder.btnEliminar?.setOnClickListener {
            activity.runOnUiThread {
                DialogMaterialHelper.mostrarConfirmDialog(activity, "¿Estás seguro de que deseas eliminar este Cliente?") { confirmed ->
                    if (confirmed) {
                        CoroutineScope(Dispatchers.IO).launch {
                            eliminarClienteYClientes(paciente)
                        }
                    } else {
                        // El usuario canceló la operación
                    }
                }
            }
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
        var nombreCliente: TextView? = null
        var descripcion: TextView? = null
        var btnEliminar: LinearLayout? = null

    }
    private fun eliminarClienteYClientes(cliente: ClienteModel) {
        // Iniciar una corrutina para realizar operaciones suspendidas
        CoroutineScope(Dispatchers.Main).launch {
            // Eliminar el cliente
            val clienteEliminado = FirebaseClienteUtil.eliminarCliente(cliente.id)
            if (clienteEliminado) {
                // Cliente eliminado exitosamente, ahora eliminar los pacientes
                val pacientesEliminados = FirebasePacienteUtil.eliminarPacientesDeCliente(cliente.id)
                if (pacientesEliminados) {
                    // Todos los pacientes del cliente fueron eliminados
                    // Mostrar un mensaje de éxito
                    DialogMaterialHelper.mostrarSuccessDialog(activity, "Cliente y sus pacientes eliminados correctamente.")
                } else {
                    // Error al eliminar los pacientes
                    DialogMaterialHelper.mostrarErrorDialog(activity, "Error al eliminar los pacientes del cliente.")
                }
            } else {
                // Error al eliminar el cliente
                DialogMaterialHelper.mostrarErrorDialog(activity, "Error al eliminar el cliente.")
            }
        }
    }


}
