package com.example.katzen.Adapter.Paciente

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebasePacienteUtil
import com.example.katzen.Fragment.Campaña.CampañaFragment
import com.example.katzen.Fragment.Campaña.CampañaPacienteFragment
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.ImageLoaderHelper
import com.example.katzen.Helper.PrintDocumentAdapter
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.PacienteModel
import com.example.katzen.PDF.ConvertPDF
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.ninodev.katzen.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

class PacienteListAdapter(
    private val activity: Activity,
    private val onItemClick: ((PacienteModel) -> Unit)? = null
) : ListAdapter<PacienteModel, PacienteListAdapter.ViewHolder>(DIFF) {

    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

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
        submitList(newList.filter { it.id.isNotBlank() && it.nombre.isNotBlank() })
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
                if (paciente.id.isNotBlank()) {
                    showDeleteConfirmationDialog(paciente.id)
                } else {
                    Toast.makeText(activity, "ID de paciente no válido", Toast.LENGTH_SHORT).show()
                }
            }

            btnCompartir.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                ConfigLoading.showLoadingAnimation()
                CoroutineScope(Dispatchers.Main).launch {
                    compartirResponsiva(getItem(pos).id)
                }
            }

            btnPDF.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                val pacienteId = getItem(pos).id
                ConfigLoading.showLoadingAnimation()
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        printOrGenerateCampaignPdf(pacienteId)
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            ConfigLoading.hideLoadingAnimation()
                            Toast.makeText(
                                activity,
                                "Error: ${e.localizedMessage ?: "Error desconocido"}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        fun bind(paciente: PacienteModel) {
            nombrePaciente.text = if (paciente.nombre.isBlank()) "Sin nombre" else paciente.nombre
            descripcion.text = try {
                if (paciente.especie.isBlank()) {
                    "Especie no especificada"
                } else {
                    "${paciente.especie}, ${UtilHelper.obtenerEdadPaciente(paciente)}"
                }
            } catch (e: Exception) {
                "Información no disponible"
            }

            if (FLAG_IN_PACIENTE) {
                btnPDF.visibility = View.GONE
                btnCompartir.visibility = View.GONE
            } else {
                btnPDF.visibility = View.VISIBLE
                btnCompartir.visibility = View.VISIBLE
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

    private suspend fun generarEImprimirPDF(pacienteId: String) {
        try {
            val success = convertXmlToPdfAndUpload(pacienteId)
            if (success) {
                printPdfFromFirebase(pacienteId)
            } else {
                withContext(Dispatchers.Main) {
                    ConfigLoading.hideLoadingAnimation()
                    Toast.makeText(activity, "No se pudo generar el PDF", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                ConfigLoading.hideLoadingAnimation()
                Toast.makeText(activity, "Error al generar el PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun ensureAuthenticated() {
        if (auth.currentUser == null) {
            throw IllegalStateException("Debes iniciar sesión para usar esta función.")
        }
    }

    private suspend fun printOrGenerateCampaignPdf(pacienteId: String) {
        ensureAuthenticated()
        val fecha = CampañaFragment.ADD_CAMPAÑA.fecha
        val (dia, mes, año) = UtilHelper.parseFecha(fecha)
        val pdfPath = "PDF/$año/$mes/$dia/$pacienteId.pdf"
        val storageRef = storage.reference.child(pdfPath)
        try {
            storageRef.metadata.await()
            printPdfFromFirebase(pacienteId)
        } catch (_: Exception) {
            generarEImprimirPDF(pacienteId)
        }
    }

    private suspend fun convertXmlToPdfAndUpload(pacienteId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                ensureAuthenticated()
                val fecha = CampañaFragment.ADD_CAMPAÑA.fecha
                val (dia, mes, año) = UtilHelper.parseFecha(fecha)
                val pdfPath = "PDF/$año/$mes/$dia/$pacienteId.pdf"
                val convertPDF = ConvertPDF(activity)
                val pdfDir = File(activity.cacheDir, "pdfs").apply { if (!exists()) mkdirs() }
                val tempFile = File(pdfDir, "temp_${pacienteId}.pdf")
                if (tempFile.exists()) tempFile.delete()
                tempFile.createNewFile()
                val success = convertPDF.convertXmlToPdf(pacienteId, tempFile.absolutePath)
                if (success && tempFile.exists() && tempFile.length() > 0) {
                    val storageRef = storage.reference.child(pdfPath)
                    try {
                        storageRef.putFile(Uri.fromFile(tempFile)).await()
                        true
                    } catch (e: Exception) {
                        false
                    } finally {
                        if (tempFile.exists()) tempFile.delete()
                    }
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }
    }

    private suspend fun compartirResponsiva(pacienteId: String) {
        withContext(Dispatchers.IO) {
            try {
                ensureAuthenticated()
                val fecha = CampañaFragment.ADD_CAMPAÑA.fecha
                val (dia, mes, año) = UtilHelper.parseFecha(fecha)
                val pdfPath = "PDF/$año/$mes/$dia/$pacienteId.pdf"
                val storageRef = storage.reference.child(pdfPath)
                try {
                    storageRef.metadata.await()
                } catch (e: Exception) {
                    if (!convertXmlToPdfAndUpload(pacienteId)) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(activity, "No se pudo generar el PDF", Toast.LENGTH_SHORT).show()
                            ConfigLoading.hideLoadingAnimation()
                        }
                        return@withContext
                    }
                }
                val tempFile = File(activity.cacheDir, "share_${pacienteId}.pdf")
                if (tempFile.exists()) tempFile.delete()
                try {
                    storageRef.getFile(tempFile).await()
                    if (tempFile.exists() && tempFile.length() > 0) {
                        withContext(Dispatchers.Main) {
                            sharePdfViaWhatsApp(tempFile)
                            ConfigLoading.hideLoadingAnimation()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(activity, "Error al descargar el PDF", Toast.LENGTH_SHORT).show()
                            ConfigLoading.hideLoadingAnimation()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(activity, "Error al descargar el PDF", Toast.LENGTH_SHORT).show()
                        ConfigLoading.hideLoadingAnimation()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity, "Error al compartir el PDF", Toast.LENGTH_SHORT).show()
                    ConfigLoading.hideLoadingAnimation()
                }
            }
        }
    }

    private suspend fun printPdfFromFirebase(pacienteId: String) {
        withContext(Dispatchers.IO) {
            try {
                ensureAuthenticated()
                val fecha = CampañaFragment.ADD_CAMPAÑA.fecha
                val (dia, mes, año) = UtilHelper.parseFecha(fecha)
                val pdfPath = "PDF/$año/$mes/$dia/$pacienteId.pdf"
                val storageRef = storage.reference.child(pdfPath)
                val tempFile = File(activity.cacheDir, "print_${pacienteId}.pdf")
                if (tempFile.exists()) tempFile.delete()
                storageRef.getFile(tempFile).await()
                if (tempFile.exists() && tempFile.length() > 0) {
                    withContext(Dispatchers.Main) {
                        val printManager = activity.getSystemService(Activity.PRINT_SERVICE) as android.print.PrintManager
                        val printAdapter = PrintDocumentAdapter(tempFile)
                        printManager.print("Impresión_${pacienteId}", printAdapter, null)
                        ConfigLoading.hideLoadingAnimation()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(activity, "El archivo PDF no existe", Toast.LENGTH_SHORT).show()
                        ConfigLoading.hideLoadingAnimation()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity, "Error al imprimir el PDF", Toast.LENGTH_SHORT).show()
                    ConfigLoading.hideLoadingAnimation()
                }
            }
        }
    }

    private fun sharePdfViaWhatsApp(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                activity,
                "${activity.packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            activity.startActivity(Intent.createChooser(intent, "Compartir PDF a través de"))
        } catch (e: Exception) {
            Toast.makeText(activity, "Error al compartir el archivo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmationDialog(pacienteId: String) {
        DialogMaterialHelper.mostrarConfirmDialog(activity, activity.getString(R.string.mascota_desactivar_confirm)) { confirmed ->
            if (confirmed) {
                CoroutineScope(Dispatchers.IO).launch {
                    val (success, message) = FirebasePacienteUtil.desactivarMascota(pacienteId)
                    withContext(Dispatchers.Main) {
                        if (success) {
                            DialogMaterialHelper.mostrarSuccessClickDialog(activity, message) {
                                UtilFragment.changeFragment(activity, CampañaPacienteFragment(), "PacienteListAdapter")
                            }
                        } else {
                            DialogMaterialHelper.mostrarErrorDialog(activity, message)
                        }
                    }
                }
            }
        }
    }

    companion object {
        var FLAG_IN_PACIENTE: Boolean = true

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
