package com.example.katzen.Adapter.Portal

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.katzen.Helper.PortalExpedienteUi
import com.example.katzen.Model.PortalCitaModel
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.ViewPortalCitaItemBinding

class PortalCitaAdapter : ListAdapter<PortalCitaModel, PortalCitaAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewPortalCitaItemBinding.inflate(
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
        private val binding: ViewPortalCitaItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PortalCitaModel, isLast: Boolean) {
            PortalExpedienteUi.applyTimelineItem(
                binding.timelineDot,
                binding.timelineLine,
                PortalExpedienteUi.Section.CITAS,
                isLast
            )

            binding.tvCitaMotivo.text = item.motivo.ifBlank { "Cita" }
            binding.tvCitaFecha.text = PortalExpedienteUi.formatCitaDateTime(item.fecha_hora)

            if (item.estado.isBlank()) {
                binding.chipEstado.visibility = View.GONE
            } else {
                binding.chipEstado.visibility = View.VISIBLE
                binding.chipEstado.text = item.estado
                val (bg, stroke) = PortalExpedienteUi.chipColorsForEstado(
                    binding.root.context,
                    item.estado
                )
                binding.chipEstado.chipBackgroundColor = ColorStateList.valueOf(bg)
                binding.chipEstado.setTextColor(stroke)
                binding.chipEstado.chipStrokeColor = ColorStateList.valueOf(stroke)
            }

            bindOptionalRow(
                binding.rowVeterinario,
                binding.tvCitaVeterinario,
                binding.root.context.getString(R.string.portal_veterinario, item.veterinario),
                item.veterinario.isNotBlank()
            )
            bindOptionalNote(binding.tvCitaObservaciones, item.observaciones)
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
        private val DIFF = object : DiffUtil.ItemCallback<PortalCitaModel>() {
            override fun areItemsTheSame(oldItem: PortalCitaModel, newItem: PortalCitaModel) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: PortalCitaModel, newItem: PortalCitaModel) =
                oldItem == newItem
        }
    }
}
