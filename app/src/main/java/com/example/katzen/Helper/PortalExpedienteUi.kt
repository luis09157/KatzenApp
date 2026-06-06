package com.example.katzen.Helper

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.PortalListSectionFragmentBinding
import java.text.SimpleDateFormat
import java.util.Locale

object PortalExpedienteUi {

    enum class Section {
        VACUNAS,
        CITAS,
        HISTORIAL
    }

    data class SectionTheme(
        val accentColor: Int,
        val iconBackground: Int,
        val headerBackground: Int,
        val iconRes: Int
    )

    fun themeFor(section: Section, context: android.content.Context): SectionTheme {
        return when (section) {
            Section.VACUNAS -> SectionTheme(
                accentColor = ContextCompat.getColor(context, R.color.portal_vacunas_accent),
                iconBackground = R.drawable.bg_portal_icon_vacunas,
                headerBackground = R.drawable.bg_portal_section_header_vacunas,
                iconRes = R.drawable.ic_recordatorio
            )
            Section.CITAS -> SectionTheme(
                accentColor = ContextCompat.getColor(context, R.color.portal_citas_accent),
                iconBackground = R.drawable.bg_portal_icon_citas,
                headerBackground = R.drawable.bg_portal_section_header_citas,
                iconRes = R.drawable.ic_calendario
            )
            Section.HISTORIAL -> SectionTheme(
                accentColor = ContextCompat.getColor(context, R.color.portal_historial_accent),
                iconBackground = R.drawable.bg_portal_icon_historial,
                headerBackground = R.drawable.bg_portal_section_header_historial,
                iconRes = R.drawable.ic_expediente
            )
        }
    }

    fun applyMenuItem(menuRoot: View, section: Section) {
        val context = menuRoot.context
        val theme = themeFor(section, context)
        menuRoot.findViewById<View>(R.id.view_accent_bar)?.background?.mutate()?.let { drawable ->
            (drawable as? GradientDrawable)?.setColor(theme.accentColor)
                ?: drawable.setTint(theme.accentColor)
        }
        menuRoot.findViewById<FrameLayout>(R.id.icon_container)
            ?.setBackgroundResource(theme.iconBackground)
        menuRoot.findViewById<ImageView>(R.id.img_menu_icon)?.apply {
            setImageResource(theme.iconRes)
            imageTintList = ColorStateList.valueOf(theme.accentColor)
        }
        menuRoot.findViewById<TextView>(R.id.tv_count_badge)?.setTextColor(theme.accentColor)
    }

    fun applyStatPill(statRoot: View, section: Section, count: Int, label: String) {
        val context = statRoot.context
        val theme = themeFor(section, context)
        statRoot.findViewById<TextView>(R.id.tv_stat_count)?.apply {
            text = count.toString()
            setTextColor(theme.accentColor)
        }
        statRoot.findViewById<TextView>(R.id.tv_stat_label)?.text = label
    }

    fun applySectionHeader(binding: PortalListSectionFragmentBinding, section: Section) {
        val context = binding.root.context
        val theme = themeFor(section, context)
        binding.layoutSectionHeader.setBackgroundResource(theme.headerBackground)
        binding.iconSectionContainer.setBackgroundResource(theme.iconBackground)
        binding.imgSectionIcon.apply {
            setImageResource(theme.iconRes)
            imageTintList = ColorStateList.valueOf(theme.accentColor)
        }
    }

    fun applyTimelineItem(
        dot: View,
        line: View?,
        section: Section,
        isLast: Boolean
    ) {
        val context = dot.context
        val accent = themeFor(section, context).accentColor
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(ContextCompat.getColor(context, R.color.surface_card))
            setStroke(3.dp(context), accent)
        }
        dot.background = drawable
        line?.visibility = if (isLast) View.INVISIBLE else View.VISIBLE
    }

    fun formatDisplayDate(raw: String): String {
        if (raw.isBlank()) return "—"
        val trimmed = raw.trim()
        listOf(
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd",
            "dd/MM/yyyy"
        ).forEach { pattern ->
            runCatching {
                val parser = SimpleDateFormat(pattern, Locale.getDefault())
                val date = parser.parse(trimmed) ?: return@runCatching
                return SimpleDateFormat("dd MMM yyyy", Locale("es", "MX")).format(date)
            }
        }
        return trimmed
    }

    fun formatCitaDateTime(raw: String): String {
        if (raw.isBlank()) return "—"
        val normalized = raw.replace("T", " ")
        listOf(
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd"
        ).forEach { pattern ->
            runCatching {
                val parser = SimpleDateFormat(pattern, Locale.getDefault())
                val date = parser.parse(normalized.trim()) ?: return@runCatching
                val datePart = SimpleDateFormat("dd MMM yyyy", Locale("es", "MX")).format(date)
                val timePart = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
                return if (pattern == "yyyy-MM-dd") datePart else "$datePart · $timePart"
            }
        }
        return raw.replace("T", " · ")
    }

    fun chipColorsForEstado(context: android.content.Context, estado: String): Pair<Int, Int> {
        val normalized = estado.lowercase(Locale.getDefault())
        return when {
            normalized.contains("confirm") -> Pair(
                ContextCompat.getColor(context, R.color.green_50),
                ContextCompat.getColor(context, R.color.green_700)
            )
            normalized.contains("cancel") || normalized.contains("rechaz") -> Pair(
                ContextCompat.getColor(context, R.color.red_50),
                ContextCompat.getColor(context, R.color.red_700)
            )
            normalized.contains("pend") || normalized.contains("program") -> Pair(
                ContextCompat.getColor(context, R.color.amber_50),
                ContextCompat.getColor(context, R.color.amber_800)
            )
            else -> Pair(
                ContextCompat.getColor(context, R.color.portal_citas_surface),
                ContextCompat.getColor(context, R.color.portal_citas_accent)
            )
        }
    }

    fun setEmptyState(
        emptyLayout: LinearLayout,
        visible: Boolean,
        title: String? = null,
        hint: String? = null
    ) {
        emptyLayout.visibility = if (visible) View.VISIBLE else View.GONE
        if (title != null) {
            emptyLayout.findViewById<TextView>(R.id.tv_empty_section)?.text = title
        }
        if (hint != null) {
            emptyLayout.findViewById<TextView>(R.id.tv_empty_section_hint)?.text = hint
        }
    }

    private fun Int.dp(context: android.content.Context): Int =
        (this * context.resources.displayMetrics.density).toInt()
}
