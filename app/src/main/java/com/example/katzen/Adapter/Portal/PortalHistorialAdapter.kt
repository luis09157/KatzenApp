package com.example.katzen.Adapter.Portal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.katzen.Helper.PortalExpedienteUi
import com.example.katzen.Model.PortalHistorialModel
import com.ninodev.katzen.databinding.ViewPortalHistorialItemBinding

class PortalHistorialAdapter :
    ListAdapter<PortalHistorialModel, PortalHistorialAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewPortalHistorialItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position == itemCount - 1)
    }

    class ViewHolder(
        private val binding: ViewPortalHistorialItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PortalHistorialModel, isLast: Boolean) {
            PortalExpedienteUi.applyTimelineItem(
                binding.timelineDot,
                binding.timelineLine,
                PortalExpedienteUi.Section.HISTORIAL,
                isLast
            )

            binding.tvHistorialDiagnostico.text = item.diagnostico.ifBlank { "Consulta" }
            binding.tvHistorialFecha.text = PortalExpedienteUi.formatDisplayDate(item.fecha_registro)

            bindOptionalRow(
                binding.rowTratamiento,
                binding.tvHistorialTratamiento,
                "Tratamiento: ${item.tratamiento}",
                item.tratamiento.isNotBlank()
            )
            bindOptionalRow(
                binding.rowMedicamentos,
                binding.tvHistorialMedicamentos,
                "Medicamentos: ${item.medicamentos}",
                item.medicamentos.isNotBlank()
            )
            bindOptionalNote(binding.tvHistorialNotas, buildNotas(item))
        }

        private fun buildNotas(item: PortalHistorialModel): String {
            return listOfNotNull(
                item.notas.takeIf { it.isNotBlank() },
                item.medico_atendio.takeIf { it.isNotBlank() }?.let { "Médico: $it" },
                item.historia_clinica.takeIf { it.isNotBlank() }?.let { "Historia: $it" },
                item.hallazgos.takeIf { it.isNotBlank() }?.let { "Hallazgos: $it" }
            ).joinToString("\n")
        }

        private fun bindOptionalRow(row: View, label: android.widget.TextView, text: String, visible: Boolean) {
            row.visibility = if (visible) View.VISIBLE else View.GONE
            label.text = text
        }

        private fun bindOptionalNote(view: android.widget.TextView, text: String) {
            view.text = text
            view.visibility = if (text.isNotBlank()) View.VISIBLE else View.GONE
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<PortalHistorialModel>() {
            override fun areItemsTheSame(oldItem: PortalHistorialModel, newItem: PortalHistorialModel) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: PortalHistorialModel, newItem: PortalHistorialModel) =
                oldItem == newItem
        }
    }
}
