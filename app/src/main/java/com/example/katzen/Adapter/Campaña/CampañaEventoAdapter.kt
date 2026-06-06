package com.example.katzen.Adapter.Campaña

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.katzen.DataBaseFirebase.FirebaseCampañaUtil
import com.example.katzen.Fragment.Campaña.CampañaFragment
import com.example.katzen.Helper.CalendarioUtil
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Model.CampañaModel
import com.ninodev.katzen.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CampañaEventoAdapter(
    private val activity: Activity,
    private val onItemClick: ((CampañaModel) -> Unit)? = null
) : ListAdapter<CampañaModel, CampañaEventoAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_campania_evento_detalle, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateList(newList: List<CampañaModel>) {
        submitList(newList.toList())
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val diaTextView: TextView = itemView.findViewById(R.id.textViewDia)
        private val fechaTextView: TextView = itemView.findViewById(R.id.textViewFecha)
        private val cantidadPacientesTextView: TextView = itemView.findViewById(R.id.textViewCantidadPacientes)
        private val btnEliminar: View = itemView.findViewById(R.id.btnEliminar)

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
                val campaña = getItem(pos)
                DialogMaterialHelper.mostrarConfirmDialog(
                    activity,
                    "¿Estás seguro de que deseas eliminar la campaña?"
                ) { confirmed ->
                    if (confirmed) {
                        CoroutineScope(Dispatchers.IO).launch {
                            CampañaFragment.ADD_CAMPAÑA.id = campaña.id
                            val (success, message) = FirebaseCampañaUtil.eliminarCampaña()
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

        fun bind(campaña: CampañaModel) {
            fechaTextView.text = campaña.fecha
            cantidadPacientesTextView.text = "${campaña.cantidadPacientes} Pacientes"
            diaTextView.text = CalendarioUtil.obtenerDiaDesdeString(campaña.fecha)
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CampañaModel>() {
            override fun areItemsTheSame(oldItem: CampañaModel, newItem: CampañaModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: CampañaModel, newItem: CampañaModel): Boolean {
                return oldItem.id == newItem.id &&
                    oldItem.fecha == newItem.fecha &&
                    oldItem.cantidadPacientes == newItem.cantidadPacientes
            }
        }
    }
}
