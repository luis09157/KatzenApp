package com.example.katzen.Adapter.Paciente

import PacienteModel
import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.katzen.DataBaseFirebase.FirebaseCampañaUtil
import com.example.katzen.DataBaseFirebase.FirebasePacienteUtil
import com.example.katzen.Fragment.Campaña.CampañaPacienteFragment
import com.example.katzen.Fragment.Cliente.ClienteDetalleFragment
import com.example.katzen.Helper.CalendarioUtil
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Model.CampañaModel
import com.example.katzen.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PacienteListAdapter(
    private val activity: Activity,
    private var mascotaList: List<PacienteModel>
) : ArrayAdapter<PacienteModel>(activity, R.layout.view_list_paciente, mascotaList) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var holder: ViewHolder
        val itemView = convertView ?: LayoutInflater.from(activity).inflate(R.layout.view_list_paciente, parent, false).apply {
            holder = ViewHolder().apply {
                imgPerfil = findViewById(R.id.imgPerfil)
                nombrePaciente = findViewById(R.id.text_nombre)
                descripcion = findViewById(R.id.text_descripcion)
                btnEliminar = findViewById(R.id.btnEliminar)
            }
            tag = holder
        }

        holder = itemView.tag as ViewHolder
        val paciente = mascotaList[position]

        configureImage(holder.imgPerfil, paciente.imageUrl)
        holder.nombrePaciente?.text = paciente.nombre
        holder.descripcion?.text = formatDescripcion(paciente)

        holder.btnEliminar?.setOnClickListener {
            showDeleteConfirmationDialog(paciente.id)
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
            val (anios, meses) = CalendarioUtil.calcularEdadMascota(paciente.edad)
            val aniosTexto = anios.takeIf { it >= 0 } ?: 0
            val mesesTexto = meses.takeIf { it >= 0 } ?: 0
            "${paciente.especie}, ${aniosTexto} años y ${mesesTexto} meses"
        } catch (e: Exception) {
            "Información no disponible"
        }
    }

    private fun showDeleteConfirmationDialog(pacienteId: String) {
        activity.runOnUiThread {
          if(FLAG_DELETE_PACIENTE){
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

    companion object{
        var FLAG_DELETE_PACIENTE : Boolean = true
    }
    private class ViewHolder {
        var imgPerfil: ImageView? = null
        var nombrePaciente: TextView? = null
        var descripcion: TextView? = null
        var btnEliminar: LinearLayout? = null
    }
}
