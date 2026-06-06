package com.example.katzen.Adapter.Paciente

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.katzen.Model.VacunaModel
import com.ninodev.katzen.R

class VacunasAdapter(
    private val onItemClick: (VacunaModel) -> Unit,
    private val onDeleteClick: (VacunaModel) -> Unit
) : ListAdapter<VacunaModel, VacunasAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_vacuna, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    fun updateList(newList: List<VacunaModel>) = submitList(newList.toList())

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombreVacuna: TextView = itemView.findViewById(R.id.tvNombreVacuna)
        private val tvFechaAplicacion: TextView = itemView.findViewById(R.id.tvFechaAplicacion)
        private val imgRecordatorio: ImageView = itemView.findViewById(R.id.imgRecordatorio)
        private val tvProximaAplicacion: TextView = itemView.findViewById(R.id.tvProximaAplicacion)
        private val tvObservaciones: TextView = itemView.findViewById(R.id.tvObservaciones)
        private val btnEliminar: View = itemView.findViewById(R.id.btnEliminar)
        private val tvDosisAplicada: TextView = itemView.findViewById(R.id.tvDosisAplicada)

        init {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onItemClick(getItem(pos))
            }
            btnEliminar.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) onDeleteClick(getItem(pos))
            }
        }

        fun bind(vacuna: VacunaModel) {
            tvNombreVacuna.text = vacuna.vacuna
            tvFechaAplicacion.text = "Fecha: ${vacuna.fecha}"

            if (vacuna.dosis.isNotEmpty()) {
                tvDosisAplicada.visibility = View.VISIBLE
                tvDosisAplicada.text = "Dosis: ${vacuna.dosis} ml"
            } else {
                tvDosisAplicada.visibility = View.GONE
            }

            if (vacuna.recordatorio && vacuna.fechaRecordatorio.isNotEmpty()) {
                tvProximaAplicacion.text = "Próx: ${vacuna.fechaRecordatorio}"
                tvProximaAplicacion.visibility = View.VISIBLE
                imgRecordatorio.visibility = View.VISIBLE
            } else {
                tvProximaAplicacion.visibility = View.GONE
                imgRecordatorio.visibility = View.GONE
            }

            if (vacuna.observaciones.isNotEmpty()) {
                tvObservaciones.text = "Observaciones: ${vacuna.observaciones}"
                tvObservaciones.visibility = View.VISIBLE
            } else {
                tvObservaciones.visibility = View.GONE
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<VacunaModel>() {
            override fun areItemsTheSame(a: VacunaModel, b: VacunaModel) = a.id == b.id
            override fun areContentsTheSame(a: VacunaModel, b: VacunaModel) = a == b
        }
    }
}
