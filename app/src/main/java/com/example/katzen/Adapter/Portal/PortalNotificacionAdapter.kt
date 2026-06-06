package com.example.katzen.Adapter.Portal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.katzen.Helper.PortalNotificacionUi
import com.example.katzen.Model.PortalNotificacionModel
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.ViewPortalNotificacionBinding

class PortalNotificacionAdapter(
    private val onItemClick: ((PortalNotificacionModel) -> Unit)? = null
) : ListAdapter<PortalNotificacionModel, PortalNotificacionAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewPortalNotificacionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, position == itemCount - 1)
        holder.itemView.setOnClickListener { onItemClick?.invoke(item) }
    }

    class ViewHolder(
        private val binding: ViewPortalNotificacionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PortalNotificacionModel, isLast: Boolean) {
            val category = PortalNotificacionUi.categoryFor(item)
            val context = binding.root.context

            PortalNotificacionUi.applyTimelineItem(
                binding.timelineDot,
                binding.timelineLine,
                category,
                isLast
            )
            PortalNotificacionUi.applyCardTheme(
                binding.viewAccentBar,
                binding.iconContainer,
                binding.imgNotifIcon,
                category
            )

            binding.tvNotifTitulo.text = item.titulo.ifBlank { context.getString(R.string.portal_notif_default_title) }
            binding.tvNotifFecha.text = PortalNotificacionUi.formatNotificationTime(item.fecha, context)
            binding.tvNotifMensaje.text = item.mensaje.ifBlank { "—" }

            val unread = !item.leida
            binding.viewUnreadDot.visibility = if (unread) View.VISIBLE else View.GONE
            binding.chipNueva.visibility = if (unread) View.VISIBLE else View.GONE

            binding.cardNotificacion.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    if (unread) R.color.surface_card else R.color.grey_50
                )
            )
            binding.cardNotificacion.strokeColor = ContextCompat.getColor(
                context,
                if (unread) R.color.divider else R.color.grey_200
            )
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<PortalNotificacionModel>() {
            override fun areItemsTheSame(
                oldItem: PortalNotificacionModel,
                newItem: PortalNotificacionModel
            ) = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: PortalNotificacionModel,
                newItem: PortalNotificacionModel
            ) = oldItem == newItem
        }
    }
}
