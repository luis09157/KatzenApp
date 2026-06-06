package com.example.katzen.Adapter.Portal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.katzen.Helper.PortalExpedienteUi
import com.example.katzen.Model.VacunaModel
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.ViewPortalVacunaItemBinding

class PortalVacunaAdapter : ListAdapter<VacunaModel, PortalVacunaAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewPortalVacunaItemBinding.inflate(
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
        private val binding: ViewPortalVacunaItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: VacunaModel, isLast: Boolean) {
            PortalExpedienteUi.applyTimelineItem(
                binding.timelineDot,
                binding.timelineLine,
                PortalExpedienteUi.Section.VACUNAS,
                isLast
            )

            binding.tvVacunaNombre.text = item.vacuna.ifBlank { "Vacuna" }
            binding.tvVacunaFecha.text = PortalExpedienteUi.formatDisplayDate(
                item.fecha.ifBlank { item.fechaRegistro }
            )

            bindOptionalRow(
                binding.rowDosis,
                binding.tvVacunaDosis,
                binding.root.context.getString(R.string.portal_vacuna_dosis, item.dosis.ifBlank { "—" }),
                item.dosis.isNotBlank()
            )
            bindOptionalRow(
                binding.rowVeterinario,
                binding.tvVacunaVeterinario,
                binding.root.context.getString(R.string.portal_veterinario, item.veterinario),
                item.veterinario.isNotBlank()
            )
            bindOptionalNote(binding.tvVacunaObservaciones, item.observaciones)
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
        private val DIFF = object : DiffUtil.ItemCallback<VacunaModel>() {
            override fun areItemsTheSame(oldItem: VacunaModel, newItem: VacunaModel) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: VacunaModel, newItem: VacunaModel) =
                oldItem == newItem
        }
    }
}
