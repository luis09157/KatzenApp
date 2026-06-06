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
import com.example.katzen.Helper.ImageLoaderHelper
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.ClienteModel
import com.ninodev.katzen.R

class ClienteListAdapter(
    private val activity: Activity
) : ListAdapter<ClienteModel, ClienteListAdapter.ViewHolder>(DIFF) {

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
        private val contAcciones: View = itemView.findViewById(R.id.contAcciones)

        init {
            contAcciones.visibility = View.GONE

            telefono.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    UtilHelper.enviarMensajeWhatsApp(activity, getItem(pos).telefono)
                }
            }
        }

        fun bind(cliente: ClienteModel) {
            nombreCompletoTextView.text = ClienteAdapter.formatNombreCompleto(cliente)
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
        private val DIFF = object : DiffUtil.ItemCallback<ClienteModel>() {
            override fun areItemsTheSame(oldItem: ClienteModel, newItem: ClienteModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ClienteModel, newItem: ClienteModel): Boolean {
                return oldItem.id == newItem.id &&
                    oldItem.nombre == newItem.nombre &&
                    oldItem.telefono == newItem.telefono &&
                    oldItem.expediente == newItem.expediente &&
                    oldItem.imageUrl == newItem.imageUrl &&
                    oldItem.imageFileName == newItem.imageFileName
            }
        }
    }
}
