package com.example.katzen.Adapter.Paciente

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.katzen.DataBaseFirebase.FirebasePacienteUtil
import com.example.katzen.Helper.CalendarioUtil
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.ImageLoaderHelper
import com.example.katzen.Model.PacienteModel
import com.ninodev.katzen.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SeleccionPacienteAdapter(
    private val activity: Activity,
    private val onItemClick: ((PacienteModel) -> Unit)? = null
) : ListAdapter<PacienteModel, SeleccionPacienteAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_list_paciente, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.clearImage()
        super.onViewRecycled(holder)
    }

    fun updateList(newList: List<PacienteModel>) {
        submitList(newList.toList())
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgPerfil: ImageView = itemView.findViewById(R.id.imgPerfil)
        private val progressImageLoading: ProgressBar = itemView.findViewById(R.id.progressImageLoading)
        private val nombrePaciente: TextView = itemView.findViewById(R.id.text_nombre)
        private val descripcion: TextView = itemView.findViewById(R.id.text_descripcion)
        private val btnEliminar: View = itemView.findViewById(R.id.btnEliminar)
        private val btnPDF: View = itemView.findViewById(R.id.btnPDF)
        private val btnCompartir: View = itemView.findViewById(R.id.btnCompartir)

        init {
            btnPDF.visibility = View.GONE
            btnCompartir.visibility = View.GONE

            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(getItem(pos))
                }
            }

            btnEliminar.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                val paciente = getItem(pos)
                DialogMaterialHelper.mostrarConfirmDialog(
                    activity,
                    "¿Estás seguro de que deseas eliminar este paciente?"
                ) { confirmed ->
                    if (confirmed) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val (success, message) = FirebasePacienteUtil.eliminarMascota(paciente.id)
                            withContext(Dispatchers.Main) {
                                if (success) {
                                    DialogMaterialHelper.mostrarSuccessDialog(activity, message)
                                } else {
                                    DialogMaterialHelper.mostrarErrorDialog(activity, message)
                                }
                            }
                        }
                    }
                }
            }
        }

        fun bind(paciente: PacienteModel) {
            nombrePaciente.text = paciente.nombre
            descripcion.text = try {
                val (anios, meses) = CalendarioUtil.calcularEdadMascota(paciente.edad)
                "${paciente.especie}, ${anios} años y ${meses} meses"
            } catch (e: Exception) {
                paciente.especie
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
            ImageLoaderHelper.clearListImage(imgPerfil, progressImageLoading, R.drawable.avatar_sin_imagen_mascota)
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<PacienteModel>() {
            override fun areItemsTheSame(oldItem: PacienteModel, newItem: PacienteModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: PacienteModel, newItem: PacienteModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}
