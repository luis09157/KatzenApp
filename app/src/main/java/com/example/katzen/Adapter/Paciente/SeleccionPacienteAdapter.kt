package com.example.katzen.Adapter.Paciente

import PacienteModel
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.katzen.DataBaseFirebase.FirebasePacienteUtil
import com.example.katzen.Helper.CalendarioUtil
import com.example.katzen.Helper.DialogMaterialHelper
import com.ninodev.katzen.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SeleccionPacienteAdapter (
    activity: Activity,
    private var mascotaList: List<PacienteModel>
) : ArrayAdapter<PacienteModel>(activity, R.layout.view_list_paciente, mascotaList) {

    private var originalList: List<PacienteModel> = mascotaList.toList()
    var activity : Activity = activity
    var TAG : String = "PacienteListAdapter"

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        val holder: ViewHolder

        if (itemView == null) {
            itemView = LayoutInflater.from(activity).inflate(R.layout.view_list_paciente, parent, false)
            holder = ViewHolder()
            holder.imgPerfil = itemView.findViewById(R.id.imgPerfil)
            holder.nombrePaciente = itemView.findViewById(R.id.text_nombre)
            holder.descripcion = itemView.findViewById(R.id.text_descripcion)
            holder.btnEliminar = itemView.findViewById(R.id.btnEliminar)
            itemView.tag = holder
        } else {
            holder = itemView.tag as ViewHolder
        }
        val paciente = mascotaList[position]

        holder.nombrePaciente?.text = ""
        holder.descripcion?.text = ""
        holder.imgPerfil?.setImageResource(R.drawable.ic_perfil)

        if (paciente.imageUrl.isNotEmpty()) {
            Glide.with(holder.imgPerfil!!.context)
                .load(paciente.imageUrl)
                .placeholder(R.drawable.ic_perfil) // Establecer la imagen predeterminada
                .error(R.drawable.no_disponible_rosa) // Opcional: establecer una imagen en caso de error al cargar
                .into(holder.imgPerfil!!)
        } else {
            holder.imgPerfil?.setImageResource(R.drawable.no_disponible_rosa)
        }


        holder.nombrePaciente?.text = "${paciente.nombre}"
        try {
            val (anios, meses) = CalendarioUtil.calcularEdadMascota(paciente.edad)
            holder.descripcion?.text =  "${paciente.especie}, ${anios} años y ${meses} meses"
        }catch (e : Exception){
            print(e.message)
        }


        holder.btnEliminar?.setOnClickListener {
            activity.runOnUiThread {
                DialogMaterialHelper.mostrarConfirmDialog(activity, "¿Estás seguro de que deseas eliminar este paciente?") { confirmed ->
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
                    } else {
                        // El usuario canceló la operación
                    }
                }
            }
        }



        return itemView!!
    }

    override fun getCount(): Int {
        return mascotaList.size
    }

    override fun getItem(position: Int): PacienteModel? {
        return mascotaList[position]
    }
    fun updateList(newList: List<PacienteModel>) {
        mascotaList = newList
        originalList = newList.toList()
        notifyDataSetChanged()
    }

    private class ViewHolder {
        var imgPerfil: ImageView? = null
        var nombrePaciente: TextView? = null
        var descripcion: TextView? = null
        var btnEliminar: LinearLayout? = null

    }
}
