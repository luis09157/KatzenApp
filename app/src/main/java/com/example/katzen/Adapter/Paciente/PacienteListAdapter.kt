package com.example.katzen.Adapter.Paciente

import PacienteModel
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseCampañaUtil
import com.example.katzen.DataBaseFirebase.FirebasePacienteUtil
import com.example.katzen.Fragment.Campaña.CampañaFragment
import com.example.katzen.Fragment.Campaña.CampañaPacienteFragment
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.PrintDocumentAdapter
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper
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
    private var mascotaList: List<PacienteModel>
) : ArrayAdapter<PacienteModel>(activity, R.layout.view_list_paciente, mascotaList) {

    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var holder: ViewHolder
        val itemView = convertView ?: LayoutInflater.from(activity).inflate(R.layout.view_list_paciente, parent, false).also {
            holder = ViewHolder().apply {
                imgPerfil = it.findViewById(R.id.imgPerfil)
                nombrePaciente = it.findViewById(R.id.text_nombre)
                descripcion = it.findViewById(R.id.text_descripcion)
                btnEliminar = it.findViewById(R.id.btnEliminar)
                btnPDF = it.findViewById(R.id.btnPDF)
                btnCompartir = it.findViewById(R.id.btnCompartir)
            }
            it.tag = holder
        }
        holder = itemView.tag as ViewHolder

        try {
            val paciente = mascotaList[position]
            
            // Verificar que los datos no sean nulos
            if (paciente.nombre.isNullOrEmpty()) {
                Log.e("PacienteListAdapter", "Nombre de paciente vacío en posición $position")
                holder.nombrePaciente?.text = "Sin nombre"
            } else {
                holder.nombrePaciente?.text = paciente.nombre
            }

            // Verificar y manejar la descripción
            holder.descripcion?.text = try {
                if (paciente.especie.isNullOrEmpty()) {
                    Log.w("PacienteListAdapter", "Especie vacía en paciente ${paciente.id}")
                    "Especie no especificada"
                } else {
                    "${paciente.especie}, ${UtilHelper.obtenerEdadPaciente(paciente)}"
                }
            } catch (e: Exception) {
                Log.e("PacienteListAdapter", "Error al formatear descripción: ${e.message}")
                "Información no disponible"
            }

            // Manejo seguro de la imagen
            try {
                configureImage(holder.imgPerfil, paciente.imageUrl)
            } catch (e: Exception) {
                Log.e("PacienteListAdapter", "Error al cargar imagen: ${e.message}")
                holder.imgPerfil?.setImageResource(R.drawable.no_disponible_rosa)
            }

            // Control de visibilidad de botones
            if (FLAG_IN_PACIENTE) {
                holder.btnPDF?.visibility = View.GONE
                holder.btnCompartir?.visibility = View.GONE
            } else {
                holder.btnPDF?.visibility = View.VISIBLE
                holder.btnCompartir?.visibility = View.VISIBLE
            }

            // Configuración de listeners con manejo de errores
            holder.btnEliminar?.setOnClickListener {
                if (!paciente.id.isNullOrEmpty()) {
                    showDeleteConfirmationDialog(paciente.id)
                } else {
                    Toast.makeText(activity, "ID de paciente no válido", Toast.LENGTH_SHORT).show()
                }
            }

            holder.btnCompartir?.setOnClickListener {
                ConfigLoading.showLoadingAnimation()
                CoroutineScope(Dispatchers.Main).launch {
                    compartirResponsiva(paciente.id)
                }
            }

            holder.btnPDF?.setOnClickListener {
                ConfigLoading.showLoadingAnimation()
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        // Primero verificamos si ya existe el PDF en Firebase
                        val storageRef = storage.reference.child("pdfs/$paciente.id.pdf")
                        try {
                            // Intentar obtener metadata del archivo
                            val metadata = storageRef.metadata.await()
                            if (metadata != null) {
                                // El PDF ya existe, procedemos a imprimir
                                Log.d("PacienteListAdapter", "PDF encontrado en Firebase, procediendo a imprimir")
                                printPdfFromFirebase(paciente.id)
                            } else {
                                // El PDF no existe, necesitamos crearlo
                                generarEImprimirPDF(paciente.id)
                            }
                        } catch (e: Exception) {
                            // Si hay error, probablemente el archivo no existe
                            Log.d("PacienteListAdapter", "PDF no encontrado en Firebase, generando nuevo")
                            generarEImprimirPDF(paciente.id)
                        }
                    } catch (e: Exception) {
                        Log.e("PacienteListAdapter", "Error en proceso de PDF", e)
                        withContext(Dispatchers.Main) {
                            ConfigLoading.hideLoadingAnimation()
                            Toast.makeText(activity, "Error: ${e.localizedMessage ?: "Error desconocido"}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("PacienteListAdapter", "Error al configurar vista: ${e.message}")
            // Configurar vista con valores por defecto
            holder.nombrePaciente?.text = "Error al cargar datos"
            holder.descripcion?.text = "Información no disponible"
            holder.imgPerfil?.setImageResource(R.drawable.no_disponible_rosa)
        }

        return itemView
    }

    private suspend fun generarEImprimirPDF(pacienteId: String) {
        try {
            Log.d("PacienteListAdapter", "Iniciando generación de PDF")
            val success = convertXmlToPdfAndUpload(pacienteId)
            if (success) {
                Log.d("PacienteListAdapter", "PDF generado exitosamente, procediendo a imprimir")
                printPdfFromFirebase(pacienteId)
            } else {
                withContext(Dispatchers.Main) {
                    ConfigLoading.hideLoadingAnimation()
                    Toast.makeText(activity, "No se pudo generar el PDF", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("PacienteListAdapter", "Error en generación de PDF", e)
            withContext(Dispatchers.Main) {
                ConfigLoading.hideLoadingAnimation()
                Toast.makeText(activity, "Error al generar el PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun ensureAuthenticated() {
        if (auth.currentUser == null) {
            try {
                Log.d("PacienteListAdapter", "Iniciando autenticación anónima...")
                val result = auth.signInAnonymously().await()
                Log.d("PacienteListAdapter", "Autenticación exitosa: ${result.user?.uid}")
            } catch (e: Exception) {
                Log.e("PacienteListAdapter", "Error en autenticación: ${e.message}")
                e.printStackTrace()
                throw e
            }
        } else {
            Log.d("PacienteListAdapter", "Usuario ya autenticado: ${auth.currentUser?.uid}")
        }
    }

    private suspend fun convertXmlToPdfAndUpload(pacienteId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                ensureAuthenticated()
                val fecha = CampañaFragment.ADD_CAMPAÑA.fecha
                val (dia, mes, año) = UtilHelper.parseFecha(fecha)
                val pdfPath = "PDF/$año/$mes/$dia/$pacienteId.pdf"
                
                Log.d("PacienteListAdapter", "Iniciando conversión de PDF para paciente: $pacienteId")
                val convertPDF = ConvertPDF(activity)
                
                // Crear la estructura de carpetas en el caché
                val pdfDir = File(activity.cacheDir, "pdfs").apply { 
                    if (!exists()) mkdirs() 
                }
                
                val tempFile = File(pdfDir, "temp_${pacienteId}.pdf")
                
                // Limpiar y crear archivo temporal
                if (tempFile.exists()) tempFile.delete()
                tempFile.createNewFile()
                
                Log.d("PacienteListAdapter", "Intentando convertir XML a PDF...")
                val success = convertPDF.convertXmlToPdf(pacienteId, tempFile.absolutePath)
                
                if (success && tempFile.exists() && tempFile.length() > 0) {
                    // Crear la ruta estructurada en Firebase
                    val storageRef = storage.reference.child(pdfPath)
                    
                    try {
                        Log.d("PacienteListAdapter", "Iniciando subida a Firebase en: $pdfPath")
                        storageRef.putFile(Uri.fromFile(tempFile)).await()
                        Log.d("PacienteListAdapter", "PDF subido exitosamente")
                        true
                    } catch (e: Exception) {
                        Log.e("PacienteListAdapter", "Error al subir PDF: ${e.message}")
                        e.printStackTrace()
                        false
                    } finally {
                        if (tempFile.exists()) {
                            tempFile.delete()
                        }
                    }
                } else {
                    Log.e("PacienteListAdapter", "La conversión del PDF falló")
                    false
                }
            } catch (e: Exception) {
                Log.e("PacienteListAdapter", "Error en proceso de conversión: ${e.message}")
                e.printStackTrace()
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
                
                // Verificar si el PDF existe
                try {
                    storageRef.metadata.await()
                    Log.d("PacienteListAdapter", "PDF encontrado, procediendo a compartir")
                } catch (e: Exception) {
                    // El PDF no existe, necesitamos generarlo
                    Log.d("PacienteListAdapter", "PDF no encontrado, generando nuevo")
                    val success = convertXmlToPdfAndUpload(pacienteId)
                    if (!success) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(activity, "No se pudo generar el PDF", Toast.LENGTH_SHORT).show()
                            ConfigLoading.hideLoadingAnimation()
                        }
                        return@withContext
                    }
                }

                // Una vez que sabemos que el PDF existe, procedemos a compartirlo
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
                    Log.e("PacienteListAdapter", "Error al descargar PDF para compartir: ${e.message}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(activity, "Error al descargar el PDF", Toast.LENGTH_SHORT).show()
                        ConfigLoading.hideLoadingAnimation()
                    }
                }
            } catch (e: Exception) {
                Log.e("PacienteListAdapter", "Error al compartir PDF: ${e.message}")
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
                Log.e("PacienteListAdapter", "Error al imprimir PDF: ${e.message}")
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
            Log.e("PacienteListAdapter", "Error al compartir: ${e.message}", e)
            Toast.makeText(activity, "Error al compartir el archivo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configureImage(imageView: ImageView?, imageUrl: String?) {
        try {
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(imageView?.context ?: return)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_perfil)
                    .error(R.drawable.no_disponible_rosa)
                    .into(imageView)
            } else {
                imageView?.setImageResource(R.drawable.no_disponible_rosa)
            }
        } catch (e: Exception) {
            Log.e("PacienteListAdapter", "Error al configurar imagen: ${e.message}")
            imageView?.setImageResource(R.drawable.no_disponible_rosa)
        }
    }

    override fun getCount(): Int = mascotaList.size

    override fun getItem(position: Int): PacienteModel = mascotaList[position]

    fun updateList(newList: List<PacienteModel>) {
        try {
            Log.d("PacienteListAdapter", "Actualizando lista con ${newList.size} elementos")
            // Verificar datos válidos
            val validList = newList.filter { paciente ->
                val isValid = !paciente.id.isNullOrEmpty() && !paciente.nombre.isNullOrEmpty()
                if (!isValid) {
                    Log.w("PacienteListAdapter", "Paciente inválido encontrado: ${paciente.id}")
                }
                isValid
            }
            mascotaList = validList
            notifyDataSetChanged()
        } catch (e: Exception) {
            Log.e("PacienteListAdapter", "Error al actualizar lista: ${e.message}")
        }
    }

    private fun formatDescripcion(paciente: PacienteModel): String {
        return try {
            "${paciente.especie}, ${UtilHelper.obtenerEdadPaciente(paciente)}"
        } catch (e: Exception) {
            "Información no disponible"
        }
    }
    private fun showDeleteConfirmationDialog(pacienteId: String) {
        activity.runOnUiThread {
            DialogMaterialHelper.mostrarConfirmDialog(activity, "¿Estás seguro de que deseas eliminar este paciente?") { confirmed ->
                if (confirmed) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val (success, message) = FirebasePacienteUtil.eliminarMascota(pacienteId)
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
    }


    companion object {
        var FLAG_IN_PACIENTE: Boolean = true
    }

    private class ViewHolder {
        var imgPerfil: ImageView? = null
        var nombrePaciente: TextView? = null
        var descripcion: TextView? = null
        var btnEliminar: LinearLayout? = null
        var btnPDF: LinearLayout? = null
        var btnCompartir: LinearLayout? = null
    }
}
