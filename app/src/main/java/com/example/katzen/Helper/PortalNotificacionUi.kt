package com.example.katzen.Helper

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.katzen.Model.PortalNotificacionModel
import com.ninodev.katzen.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object PortalNotificacionUi {

    enum class Category {
        VACUNA,
        CITA,
        HISTORIAL,
        GENERAL
    }

    data class CategoryTheme(
        val accentColor: Int,
        val iconBackground: Int,
        val iconRes: Int
    )

    fun categoryFor(item: PortalNotificacionModel): Category {
        val key = "${item.tipo} ${item.titulo}".lowercase(Locale.getDefault())
        return when {
            key.contains("vacuna") -> Category.VACUNA
            key.contains("cita") -> Category.CITA
            key.contains("historial") || key.contains("consulta") || key.contains("clinic") -> Category.HISTORIAL
            else -> Category.GENERAL
        }
    }

    fun themeFor(category: Category, context: android.content.Context): CategoryTheme {
        return when (category) {
            Category.VACUNA -> CategoryTheme(
                accentColor = ContextCompat.getColor(context, R.color.portal_vacunas_accent),
                iconBackground = R.drawable.bg_portal_icon_vacunas,
                iconRes = R.drawable.ic_recordatorio
            )
            Category.CITA -> CategoryTheme(
                accentColor = ContextCompat.getColor(context, R.color.portal_citas_accent),
                iconBackground = R.drawable.bg_portal_icon_citas,
                iconRes = R.drawable.ic_calendario
            )
            Category.HISTORIAL -> CategoryTheme(
                accentColor = ContextCompat.getColor(context, R.color.portal_historial_accent),
                iconBackground = R.drawable.bg_portal_icon_historial,
                iconRes = R.drawable.ic_expediente
            )
            Category.GENERAL -> CategoryTheme(
                accentColor = ContextCompat.getColor(context, R.color.portal_notif_accent),
                iconBackground = R.drawable.bg_portal_icon_notif,
                iconRes = R.drawable.ic_notifications_m3
            )
        }
    }

    fun applyTimelineItem(
        dot: View,
        line: View?,
        category: Category,
        isLast: Boolean
    ) {
        val context = dot.context
        val accent = themeFor(category, context).accentColor
        dot.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(ContextCompat.getColor(context, R.color.surface_card))
            setStroke(3.dp(context), accent)
        }
        line?.visibility = if (isLast) View.INVISIBLE else View.VISIBLE
    }

    fun applyCardTheme(
        accentBar: View,
        iconContainer: FrameLayout,
        iconView: ImageView,
        category: Category
    ) {
        val context = accentBar.context
        val theme = themeFor(category, context)
        accentBar.background?.mutate()?.let { drawable ->
            (drawable as? GradientDrawable)?.setColor(theme.accentColor)
                ?: drawable.setTint(theme.accentColor)
        }
        iconContainer.setBackgroundResource(theme.iconBackground)
        iconView.setImageResource(theme.iconRes)
        iconView.imageTintList = ColorStateList.valueOf(theme.accentColor)
    }

    fun formatNotificationTime(raw: String, context: android.content.Context): String {
        val date = parseDate(raw) ?: return "—"
        val now = Date()
        val diffMs = now.time - date.time
        if (diffMs < TimeUnit.MINUTES.toMillis(1)) {
            return context.getString(R.string.portal_notif_time_now)
        }
        if (diffMs < TimeUnit.HOURS.toMillis(1)) {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs).coerceAtLeast(1)
            return context.resources.getQuantityString(
                R.plurals.portal_notif_minutes_ago,
                minutes.toInt(),
                minutes.toInt()
            )
        }
        if (diffMs < TimeUnit.DAYS.toMillis(1)) {
            val hours = TimeUnit.MILLISECONDS.toHours(diffMs).coerceAtLeast(1)
            return context.resources.getQuantityString(
                R.plurals.portal_notif_hours_ago,
                hours.toInt(),
                hours.toInt()
            )
        }
        if (isYesterday(date, now)) {
            val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            return context.getString(R.string.portal_notif_time_yesterday, time)
        }
        if (diffMs < TimeUnit.DAYS.toMillis(7)) {
            val days = TimeUnit.MILLISECONDS.toDays(diffMs).coerceAtLeast(1)
            return context.resources.getQuantityString(
                R.plurals.portal_notif_days_ago,
                days.toInt(),
                days.toInt()
            )
        }
        val datePart = SimpleDateFormat("dd MMM yyyy", Locale("es", "MX")).format(date)
        val timePart = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        return "$datePart · $timePart"
    }

    fun bindHeader(
        subtitleView: TextView,
        badgeView: TextView,
        unreadCount: Int,
        totalCount: Int,
        context: android.content.Context
    ) {
        subtitleView.text = when {
            totalCount == 0 -> context.getString(R.string.portal_notif_empty_subtitle)
            unreadCount == 0 -> context.getString(R.string.portal_notif_all_read)
            else -> context.resources.getQuantityString(
                R.plurals.portal_notif_unread_count,
                unreadCount,
                unreadCount
            )
        }
        if (unreadCount > 0) {
            badgeView.visibility = View.VISIBLE
            badgeView.text = unreadCount.toString()
        } else {
            badgeView.visibility = View.GONE
        }
    }

    private fun parseDate(raw: String): Date? {
        if (raw.isBlank()) return null
        val trimmed = raw.trim()

        if (trimmed.endsWith("Z", ignoreCase = true)) {
            listOf("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'").forEach { pattern ->
                runCatching {
                    val parser = SimpleDateFormat(pattern, Locale.US)
                    parser.timeZone = TimeZone.getTimeZone("UTC")
                    return parser.parse(trimmed)
                }
            }
        }

        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd"
        )
        patterns.forEach { pattern ->
            runCatching {
                val parser = SimpleDateFormat(pattern, Locale.getDefault())
                return parser.parse(trimmed)
            }
        }
        return null
    }

    private fun isYesterday(date: Date, now: Date): Boolean {
        val calDate = Calendar.getInstance().apply { time = date }
        val calNow = Calendar.getInstance().apply { time = now }
        calNow.add(Calendar.DAY_OF_YEAR, -1)
        return calDate.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) &&
            calDate.get(Calendar.DAY_OF_YEAR) == calNow.get(Calendar.DAY_OF_YEAR)
    }

    private fun Int.dp(context: android.content.Context): Int =
        (this * context.resources.displayMetrics.density).toInt()
}
