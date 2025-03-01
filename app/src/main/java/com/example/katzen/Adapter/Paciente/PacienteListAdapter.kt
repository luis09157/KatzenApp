package com.example.katzen.Adapter.Paciente

import PacienteModel
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Environment
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
import com.ninodev.katzen.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PacienteListAdapter(
    private val activity: Activity,
    private var mascotaList: List<PacienteModel>
) : ArrayAdapter<PacienteModel>(activity, R.layout.view_list_paciente, mascotaList) {
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

        val paciente = mascotaList[position]

        if(FLAG_IN_PACIENTE){
            holder.btnPDF?.visibility = View.GONE
            holder.btnCompartir?.visibility = View.GONE
        }else{
            holder.btnPDF?.visibility = View.VISIBLE
            holder.btnCompartir?.visibility = View.VISIBLE

        }

        configureImage(holder.imgPerfil, paciente.imageUrl)
        holder.nombrePaciente?.text = paciente.nombre
        holder.descripcion?.text = formatDescripcion(paciente)

        holder.btnEliminar?.setOnClickListener {
            showDeleteConfirmationDialog(paciente.id)
        }

        // Integración en el botón btnPDF
        holder.btnPDF?.setOnClickListener {
            if (!FLAG_IN_PACIENTE) {
                ConfigLoading.showLoadingAnimation()
                CoroutineScope(Dispatchers.Main).launch {
                    val success = convertXmlToPdfAndReplace(paciente.id)
                    if (success) {
                        printPdfFromFile(paciente.id)
                    } else {
                        Toast.makeText(activity, "No se pudo convertir el XML a PDF", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        holder.btnCompartir?.setOnClickListener {
            if (!FLAG_IN_PACIENTE) {
                ConfigLoading.showLoadingAnimation()
                CoroutineScope(Dispatchers.Main).launch {
                    val success = convertXmlToPdfAndReplace(paciente.id)
                    if (success) {
                        compartirResponsiva(paciente.id)
                    } else {
                        Toast.makeText(activity, "No fue posible generar el archivo PDF para compartir", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


        return itemView
    }

    private fun configureImage(imageView: ImageView?, imageUrl: String?) {
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(imageView!!.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_perfil)
                .error(R.drawable.no_disponible_rosa)
                .into(imageView)
        } else {
            imageView?.setImageResource(R.drawable.no_disponible_rosa)
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
          if(FLAG_IN_PACIENTE){
              deletePacienteDelCliente(pacienteId)
          }else{
              deletePacienteDeCampaña(pacienteId)
          }
        }
    }

    override fun getCount(): Int = mascotaList.size

    override fun getItem(position: Int): PacienteModel = mascotaList[position]

    fun updateList(newList: List<PacienteModel>) {
        mascotaList = newList
        notifyDataSetChanged()
    }

    private fun deletePacienteDelCliente(pacienteId: String){
        DialogMaterialHelper.mostrarConfirmDialog(activity, "¿Estás seguro de que deseas eliminar este paciente?") { confirmed ->
            if (confirmed) {
                CoroutineScope(Dispatchers.IO).launch {
                    val (success, message) = FirebasePacienteUtil.eliminarMascota(pacienteId)
                    withContext(Dispatchers.Main) {
                        if (success) {
                            DialogMaterialHelper.mostrarSuccessClickDialog(activity, message) {
                                UtilFragment.changeFragment(activity , CampañaPacienteFragment(), "PacienteListAdapter")
                            }
                        } else {
                            DialogMaterialHelper.mostrarErrorDialog(activity, message)
                        }
                    }
                }
            }
        }
    }

    private fun deletePacienteDeCampaña(pacienteId: String) {
        DialogMaterialHelper.mostrarConfirmDialog(activity, "¿Estás seguro de que deseas eliminar este paciente de la campaña?") { confirmed ->
            if (confirmed) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Eliminar la referencia del paciente en la campaña específica en Firebase
                        val success = FirebaseCampañaUtil.eliminarPacientePorIdPacienteCampaña(pacienteId)

                        withContext(Dispatchers.Main) {
                            if (success) {
                                DialogMaterialHelper.mostrarSuccessClickDialog(activity, "Paciente eliminado de la campaña con éxito") {
                                    notifyDataSetChanged()
                                    UtilFragment.changeFragment(activity, CampañaPacienteFragment(), "PacienteListAdapter")
                                }
                            } else {
                                DialogMaterialHelper.mostrarErrorDialog(activity, "No se pudo eliminar el paciente de la campaña")
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Log.e("PacienteListAdapter", "Error al eliminar paciente de campaña: ${e.message}")
                            DialogMaterialHelper.mostrarErrorDialog(activity, "Ocurrió un error al intentar eliminar el paciente")
                        }
                    }
                }
            }
        }
    }
    private fun sharePdfViaWhatsApp(file: File) {
        val uri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.provider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            setPackage("com.whatsapp")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            activity.startActivity(Intent.createChooser(intent, "Compartir PDF a través de"))
        } catch (e: Exception) {
            Toast.makeText(activity, "WhatsApp no está instalado o hubo un error al compartir el archivo", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun convertXmlToPdfAndReplace(pacienteId: String): Boolean {
        val convertPDF = ConvertPDF(activity)
        return withContext(Dispatchers.IO) {
            try {
                val fecha = UtilHelper.setFechaACarpeta(CampañaFragment.ADD_CAMPAÑA.fecha)
                val fileName = "$pacienteId.pdf"
                val relativePath = "${Environment.DIRECTORY_DOCUMENTS}/Katzen/$fecha/$pacienteId"
                val filePath = File(Environment.getExternalStoragePublicDirectory(relativePath), fileName)

                if (filePath.exists()) {
                    filePath.delete()
                }

                val success = convertPDF.convertXmlToPdf(pacienteId)
                withContext(Dispatchers.Main) {
                    ConfigLoading.hideLoadingAnimation()
                }
                success
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    ConfigLoading.hideLoadingAnimation()
                    Toast.makeText(activity, "Error al convertir el XML a PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                false
            }
        }
    }

    fun compartirResponsiva(pacienteId: String){
        CoroutineScope(Dispatchers.IO).launch {
            val fecha = UtilHelper.setFechaACarpeta(CampañaFragment.ADD_CAMPAÑA.fecha)
            val fileName = "${pacienteId}.pdf"
            val relativePath = "${Environment.DIRECTORY_DOCUMENTS}/Katzen/$fecha/${pacienteId}"
            val filePath = File(Environment.getExternalStoragePublicDirectory(relativePath), fileName)

            withContext(Dispatchers.Main) {
                if (filePath.exists()) {
                    // El archivo existe, compartir a través de WhatsApp
                    sharePdfViaWhatsApp(filePath)
                } else {
                    // El archivo no existe, mostrar mensaje
                    Toast.makeText(activity, "El archivo PDF no existe", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun printPdfFromFile(pacienteId: String) {
        val fecha = UtilHelper.setFechaACarpeta(CampañaFragment.ADD_CAMPAÑA.fecha)
        val fileName = "$pacienteId.pdf"
        val filePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)}/Katzen/$fecha/$pacienteId/$fileName"
        } else {
            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)}/Katzen/$fecha/$pacienteId/$fileName"
        }

        val file = File(filePath)
        if (file.exists()) {
            val printManager = activity.getSystemService(Activity.PRINT_SERVICE) as android.print.PrintManager
            val printAdapter = PrintDocumentAdapter(file)
            printManager.print("Impresión de PDF", printAdapter, null)
        } else {
            Toast.makeText(activity, "El archivo PDF no existe para imprimir", Toast.LENGTH_LONG).show()
        }
    }

    companion object{
        var FLAG_IN_PACIENTE : Boolean = true
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
