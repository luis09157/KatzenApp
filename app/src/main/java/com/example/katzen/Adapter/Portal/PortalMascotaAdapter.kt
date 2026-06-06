package com.example.katzen.Adapter.Portal

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.katzen.Helper.ImageLoaderHelper
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.PacienteModel
import com.ninodev.katzen.R
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView

/**
 * Lista de mascotas del portal cliente: solo lectura, sin acciones de staff.
 */
class PortalMascotaAdapter(
    private val activity: Activity,
    private val onItemClick: (PacienteModel) -> Unit
) : ListAdapter<PacienteModel, PortalMascotaAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_list_paciente, parent, false)
        view.findViewById<View>(R.id.btnEliminar).visibility = View.GONE
        view.findViewById<View>(R.id.btnPDF).visibility = View.GONE
        view.findViewById<View>(R.id.btnCompartir).visibility = View.GONE
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.clearImage()
        super.onViewRecycled(holder)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgPerfil: ImageView = itemView.findViewById(R.id.imgPerfil)
        private val progressImageLoading: ProgressBar = itemView.findViewById(R.id.progressImageLoading)
        private val nombrePaciente: TextView = itemView.findViewById(R.id.text_nombre)
        private val descripcion: TextView = itemView.findViewById(R.id.text_descripcion)

        init {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(pos))
                }
            }
        }

        fun bind(paciente: PacienteModel) {
            nombrePaciente.text = paciente.nombre.ifBlank { itemView.context.getString(R.string.portal_mascota) }
            descripcion.text = try {
                if (paciente.especie.isBlank()) {
                    itemView.context.getString(R.string.portal_especie_no_especificada)
                } else {
                    "${paciente.especie}, ${UtilHelper.obtenerEdadPaciente(paciente)}"
                }
            } catch (_: Exception) {
                itemView.context.getString(R.string.perfil_no_disponible)
            }

            ImageLoaderHelper.loadListImage(
                imageView = imgPerfil,
                progressBar = progressImageLoading,
                imageUrl = paciente.imageUrl,
                placeholderRes = R.drawable.avatar_sin_imagen_mascota,
                errorRes = R.drawable.avatar_sin_imagen_mascota,
                storageFolder = "Mascotas",
                imageFileName = paciente.imageFileName
            )
        }

        fun clearImage() {
            ImageLoaderHelper.clearListImage(
                imgPerfil,
                progressImageLoading,
                R.drawable.avatar_sin_imagen_mascota
            )
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<PacienteModel>() {
            override fun areItemsTheSame(oldItem: PacienteModel, newItem: PacienteModel) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: PacienteModel, newItem: PacienteModel) =
                oldItem == newItem
        }
    }
}
