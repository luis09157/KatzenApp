package com.example.katzen.Adapter.Paciente

import PacienteModel
import android.app.Activity
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.katzen.DataBaseFirebase.FirebaseClienteUtil
import com.example.katzen.DataBaseFirebase.FirebaseMascotaUtil
import com.example.katzen.DataBaseFirebase.OnCompleteListener
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.R
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PacienteAdapter(
    activity: Activity,
    private var mascotaList: List<PacienteModel>
) : ArrayAdapter<PacienteModel>(activity, R.layout.mascota_list_fragment, mascotaList) {

    private var originalList: List<PacienteModel> = mascotaList.toList()
    var activity : Activity = activity
    var TAG : String = "MascotaAdapter"

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        val holder: ViewHolder

        if (itemView == null) {
            itemView = LayoutInflater.from(activity).inflate(R.layout.mascota_list_fragment, parent, false)
            holder = ViewHolder()
            holder.imgPerfil = itemView.findViewById(R.id.imgPerfil)
            holder.nombreCompletoTextView = itemView.findViewById(R.id.textViewNombreCompleto)

            holder.especieTextView = itemView.findViewById(R.id.txt_especie)
            holder.razaTextView = itemView.findViewById(R.id.text_raza)
            holder.pesoTextView = itemView.findViewById(R.id.txt_peso)
            holder.sexoTextView = itemView.findViewById(R.id.text_sexo)

            holder.fondoTelefono = itemView.findViewById(R.id.fondoTelefono)
            holder.fondoCorreo = itemView.findViewById(R.id.fondoCorreo)
            holder.fondoUbicacion = itemView.findViewById(R.id.fondoUbicacion)
            holder.btnEliminar = itemView.findViewById(R.id.btnEliminar)
            itemView.tag = holder
        } else {
            holder = itemView.tag as ViewHolder
        }
        val paciente = mascotaList[position]

        holder.nombreCompletoTextView?.text = ""
        holder.razaTextView?.text = ""
        holder.especieTextView?.text = ""
        holder.imgPerfil?.setImageResource(R.drawable.ic_perfil)

        if (paciente.imageUrl.isNotEmpty()) {
            Picasso.get()
                .load(paciente.imageUrl)
                .placeholder(R.drawable.ic_perfil) // Establecer la imagen predeterminada
                .error(R.drawable.no_disponible_rosa) // Opcional: establecer una imagen en caso de error al cargar
                .into(holder.imgPerfil)
        } else {
            holder.imgPerfil?.setImageResource(R.drawable.no_disponible_rosa)
        }

        holder.nombreCompletoTextView?.text = "${paciente.nombre}"
        holder.razaTextView?.text = "RAZA: ${paciente.raza}"
        holder.especieTextView?.text = "ESPECIE: ${paciente.especie}"
        holder.pesoTextView?.text = "PESO: ${paciente.peso}"
        holder.sexoTextView?.text = "SEXO: ${paciente.sexo}"


        holder.btnEliminar?.setOnClickListener {
            activity.runOnUiThread {
                DialogMaterialHelper.mostrarConfirmDialog(activity, "¿Estás seguro de que deseas eliminar este paciente?") { confirmed ->
                    if (confirmed) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val (success, message) = FirebaseMascotaUtil.eliminarMascota(paciente.id)
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
        var nombreCompletoTextView: TextView? = null
        var especieTextView: TextView? = null
        var razaTextView: TextView? = null
        var pesoTextView: TextView? = null
        var sexoTextView: TextView? = null

        var fondoTelefono: ImageView? = null
        var fondoCorreo: ImageView? = null
        var fondoUbicacion: ImageView? = null
        var btnEliminar: CardView? = null

    }
}
