package com.example.katzen.PDF

import PacienteModel
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.katzen.DataBaseFirebase.FirebaseCampañaUtil
import com.example.katzen.Fragment.Campaña.CampañaFragment
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.ClienteModel
import com.example.katzen.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ConvertPDF(private val activity: Activity) {

    val TAG = "ConvertPDF"
    companion object {
        const val REQUEST_CODE_WRITE_STORAGE = 9157
    }

    fun askPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            true // No necesitas permisos explícitos en Android 11 y posteriores si usas MediaStore
        } else {
            ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun showPermissionRationaleDialog() {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Permiso necesario")
        builder.setMessage("Esta aplicación necesita acceder al almacenamiento externo para guardar los archivos PDF. Por favor, otorga el permiso.")
        builder.setPositiveButton("Aceptar") { dialog, _ ->
            // Solicitar el permiso nuevamente
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_WRITE_STORAGE
            )
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    suspend fun convertXmlToPdf(pacienteId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val (paciente, cliente) = getData(pacienteId)
                if (paciente != null && cliente != null) {
                    // Inflar el archivo de diseño XML y configurar las vistas
                    val view: View = LayoutInflater.from(activity).inflate(R.layout.fragment_autorizo_pdf, null)
                    configureTextViews(view, paciente, cliente)

                    // Definir las dimensiones del PDF en puntos (tamaño A4)
                    val pageWidth = 695f
                    val pageHeight = 942f

                    // Medir y ajustar la vista para que se ajuste a las dimensiones del PDF
                    measureAndLayoutView(view, pageWidth.toInt(), pageHeight.toInt())

                    // Crear un nuevo PdfDocument
                    val document = PdfDocument()

                    try {
                        // Crear la página del PDF y dibujar la vista con escala
                        createPdfPage(document, view, pageWidth, pageHeight)

                        // Guardar el PDF en el almacenamiento
                        savePdfToStorage(document, pacienteId)

                        // Retornar true si la conversión fue exitosa
                        true
                    } catch (e: IOException) {
                        e.printStackTrace()
                        // Retornar false en caso de error al crear el PDF
                        false
                    } finally {
                        document.close()
                    }
                } else {
                    // Retornar false si los datos de Firebase no se pudieron obtener
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Retornar false en caso de error al obtener datos
                false
            }
        }
    }

    private suspend fun getData(pacienteId: String): Pair<PacienteModel?, ClienteModel?> {
        return FirebaseCampañaUtil.obtenerPacienteYCliente(pacienteId)
    }

    private fun configureTextViews(view: View, paciente: PacienteModel, cliente: ClienteModel) {
        val txtExpediente = view.findViewById<TextView>(R.id.txt_expediente)
        val txtFecha = view.findViewById<TextView>(R.id.txt_fecha)
        val txtNombrePaciente = view.findViewById<TextView>(R.id.txt_nombre_paciente)
        val txtEspecie = view.findViewById<TextView>(R.id.txt_especie)
        val txtSexo = view.findViewById<TextView>(R.id.txt_sexo)
        val txtEdad = view.findViewById<TextView>(R.id.txt_edad)
        val txtRaza = view.findViewById<TextView>(R.id.txt_raza)

        val txt_nombre_cliente = view.findViewById<TextView>(R.id.txt_nombre_cliente)
        val txt_calle_numero = view.findViewById<TextView>(R.id.txt_calle_numero)
        val txt_colonia = view.findViewById<TextView>(R.id.txt_colonia)
        val txt_municipio = view.findViewById<TextView>(R.id.txt_municipio)
        val txt_numero_telefono = view.findViewById<TextView>(R.id.txt_numero_telefono)

        // Setear los valores obtenidos de Firebase
        txtExpediente.text = cliente.expediente
        txtFecha.text = paciente.fecha
        txtNombrePaciente.text = paciente.nombre
        txtEspecie.text = paciente.especie
        txtSexo.text = paciente.sexo
        txtEdad.text = UtilHelper.obtenerEdadPaciente(paciente)
        txtRaza.text = paciente.raza

        txt_nombre_cliente.text = "${cliente.nombre} ${cliente.apellidoPaterno} ${cliente.apellidoMaterno}"
        txt_calle_numero.text = "${cliente.calle} #${cliente.numero}"
        txt_colonia.text = cliente.colonia
        txt_municipio.text = cliente.municipio
        txt_numero_telefono.text = cliente.telefono
    }

    private fun measureAndLayoutView(view: View, pageWidth: Int, pageHeight: Int) {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(pageWidth, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(pageHeight, View.MeasureSpec.EXACTLY)
        )
        view.layout(0, 0, pageWidth, pageHeight)
    }

    private fun createPdfPage(document: PdfDocument, view: View, pageWidth: Float, pageHeight: Float) {
        // Crear la página del PDF con el tamaño definido
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), 1).create()
        val page = document.startPage(pageInfo)

        // Escalar el contenido al 80%
        val canvas = page.canvas

        // Dibujar la vista en el canvas del PDF
        view.draw(canvas)

        document.finishPage(page)
    }

    private fun savePdfToStorage(document: PdfDocument, idPaciente: String) {
        val fecha = UtilHelper.setFechaACarpeta(CampañaFragment.ADD_CAMPAÑA.fecha)
        val fileName = "$idPaciente.pdf"
        val contentResolver = activity.contentResolver

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val relativePath = "${Environment.DIRECTORY_DOCUMENTS}/Katzen/$fecha/$idPaciente/"
                val uriQuery = contentResolver.query(
                    MediaStore.Files.getContentUri("external"),
                    arrayOf(MediaStore.MediaColumns._ID),
                    "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?",
                    arrayOf(fileName, relativePath),
                    null
                )

                uriQuery?.use {
                    if (it.moveToFirst()) {
                        Log.i(TAG, "El archivo PDF ya existe en MediaStore, no se creará uno nuevo.")
                        return
                    }
                }

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                }

                val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                    ?: throw IOException("Error al crear el archivo PDF en MediaStore")

                contentResolver.openOutputStream(uri).use { outputStream ->
                    document.writeTo(outputStream)
                }
            } else {
                val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val targetDir = File(documentsDir, "Katzen/$fecha/$idPaciente")

                if (!targetDir.exists()) {
                    targetDir.mkdirs()
                }

                val filePath = File(targetDir, fileName)
                if (filePath.exists()) {
                    Log.i(TAG, "El archivo PDF ya existe en el sistema de archivos, no se creará uno nuevo.")
                    return
                }

                FileOutputStream(filePath).use { fos ->
                    document.writeTo(fos)
                }
            }
            Toast.makeText(activity, "PDF guardado exitosamente", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(activity, "Error al guardar el PDF", Toast.LENGTH_LONG).show()
        }
    }
}
