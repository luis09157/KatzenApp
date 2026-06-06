package com.example.katzen.Adapter.Cliente

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.katzen.DataBaseFirebase.FirebaseClienteUtil
import com.example.katzen.DataBaseFirebase.OnCompleteListener
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.ImageLoaderHelper
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.ClienteModel
import com.ninodev.katzen.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClienteAdapter(
    private val activity: Activity,
    private val onItemClick: ((ClienteModel) -> Unit)? = null
) : ListAdapter<ClienteModel, ClienteAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cliente_list_fragment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.clearImage()
        super.onViewRecycled(holder)
    }

    fun updateList(newList: List<ClienteModel>) {
        submitList(newList.toList())
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgPerfil: ImageView = itemView.findViewById(R.id.imgPerfil)
        private val progressImageLoading: ProgressBar = itemView.findViewById(R.id.progressImageLoading)
        private val nombreCompletoTextView: TextView = itemView.findViewById(R.id.textViewNombreCompleto)
        private val expediente: TextView = itemView.findViewById(R.id.textExpediente)
        private val telefono: TextView = itemView.findViewById(R.id.textTelefono)
        private val fondoTelefono: View = itemView.findViewById(R.id.fondoTelefono)
        private val fondoCorreo: View = itemView.findViewById(R.id.fondoCorreo)
        private val fondoUbicacion: View = itemView.findViewById(R.id.fondoUbicacion)
        private val btnEliminar: View = itemView.findViewById(R.id.btnEliminar)

        init {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(getItem(pos))
                }
            }

            fondoTelefono.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    UtilHelper.llamarCliente(activity, getItem(pos).telefono)
                }
            }

            fondoCorreo.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    UtilHelper.enviarCorreoElectronicoGmail(activity, getItem(pos).correo)
                }
            }

            fondoUbicacion.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    UtilHelper.abrirGoogleMaps(activity, getItem(pos).urlGoogleMaps)
                }
            }

            btnEliminar.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                val cliente = getItem(pos)
                DialogMaterialHelper.mostrarConfirmDialog(
                    activity,
                    activity.getString(R.string.cliente_desactivar_confirm)
                ) { confirmed ->
                    if (confirmed) {
                        CoroutineScope(Dispatchers.IO).launch {
                            FirebaseClienteUtil.desactivarCliente(cliente.id, object : OnCompleteListener {
                                override fun onComplete(success: Boolean, message: String) {
                                    if (success) {
                                        val updated = currentList.filter { it.id != cliente.id }
                                        activity.runOnUiThread {
                                            updateList(updated)
                                            DialogMaterialHelper.mostrarSuccessDialog(activity, message)
                                        }
                                    } else {
                                        DialogMaterialHelper.mostrarErrorDialog(activity, message)
                                    }
                                }
                            })
                        }
                    }
                }
            }
        }

        fun bind(cliente: ClienteModel) {
            nombreCompletoTextView.text = formatNombreCompleto(cliente)
            expediente.text = activity.getString(
                R.string.cliente_expediente,
                cliente.expediente.ifBlank { "—" }
            )
            telefono.text = activity.getString(
                R.string.cliente_telefono,
                cliente.telefono.ifBlank { "—" }
            )

            ImageLoaderHelper.loadListImage(
                imageView = imgPerfil,
                progressBar = progressImageLoading,
                imageUrl = cliente.imageUrl,
                placeholderRes = R.drawable.avatar_sin_imagen,
                errorRes = R.drawable.avatar_sin_imagen,
                storageFolder = "Clientes",
                imageFileName = cliente.imageFileName
            )
        }

        fun clearImage() {
            ImageLoaderHelper.clearListImage(imgPerfil, progressImageLoading, R.drawable.avatar_sin_imagen)
        }
    }

    companion object {
        fun formatNombreCompleto(cliente: ClienteModel): String {
            val nombre = "${cliente.nombre} ${cliente.apellidoPaterno} ${cliente.apellidoMaterno}".trim()
            return nombre.ifBlank { "Sin nombre" }
        }

        private val DIFF = object : DiffUtil.ItemCallback<ClienteModel>() {
            override fun areItemsTheSame(oldItem: ClienteModel, newItem: ClienteModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ClienteModel, newItem: ClienteModel): Boolean {
                return oldItem.id == newItem.id &&
                    oldItem.nombre == newItem.nombre &&
                    oldItem.apellidoPaterno == newItem.apellidoPaterno &&
                    oldItem.apellidoMaterno == newItem.apellidoMaterno &&
                    oldItem.expediente == newItem.expediente &&
                    oldItem.telefono == newItem.telefono &&
                    oldItem.correo == newItem.correo &&
                    oldItem.urlGoogleMaps == newItem.urlGoogleMaps &&
                    oldItem.imageUrl == newItem.imageUrl &&
                    oldItem.imageFileName == newItem.imageFileName
            }
        }
    }
}
