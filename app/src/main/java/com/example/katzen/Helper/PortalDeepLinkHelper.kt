package com.example.katzen.Helper

import android.content.Intent
import com.example.katzen.Fragment.Portal.PortalCitasListFragment
import com.example.katzen.Fragment.Portal.PortalHistorialListFragment
import com.example.katzen.Fragment.Portal.PortalMascotaDetalleFragment
import com.example.katzen.Fragment.Portal.PortalMascotasFragment
import com.example.katzen.Fragment.Portal.PortalNotificacionesFragment
import com.example.katzen.Fragment.Portal.PortalVacunasListFragment
import com.example.katzen.Model.PortalNotificacionModel
import com.example.katzen.PortalMainActivity
import com.ninodev.katzen.R

object PortalDeepLinkHelper {

    const val EXTRA_CLIENTE_ID = "portal_cliente_id"
    const val EXTRA_TIPO = "portal_tipo"
    const val EXTRA_MASCOTA_ID = "portal_mascota_id"
    const val EXTRA_REFERENCIA_ID = "portal_referencia_id"

    enum class Section {
        NOTIFICACIONES,
        MASCOTA_DETALLE,
        VACUNAS,
        CITAS,
        HISTORIAL
    }

    data class Target(
        val section: Section,
        val mascotaId: String = "",
        val referenciaId: String = ""
    )

    fun putExtras(
        intent: Intent,
        clienteId: String,
        tipo: String = "",
        mascotaId: String = "",
        referenciaId: String = ""
    ) {
        intent.putExtra(EXTRA_CLIENTE_ID, clienteId)
        intent.putExtra(EXTRA_TIPO, tipo)
        intent.putExtra(EXTRA_MASCOTA_ID, mascotaId)
        intent.putExtra(EXTRA_REFERENCIA_ID, referenciaId)
    }

    fun parseIntent(intent: Intent?): Target? {
        if (intent == null) return null
        val tipo = intent.getStringExtra(EXTRA_TIPO).orEmpty()
        val mascotaId = intent.getStringExtra(EXTRA_MASCOTA_ID).orEmpty()
        val referenciaId = intent.getStringExtra(EXTRA_REFERENCIA_ID).orEmpty()
        if (tipo.isBlank() && mascotaId.isBlank() && referenciaId.isBlank()) return null
        return targetFrom(tipo, mascotaId, referenciaId)
    }

    fun targetFromNotificacion(item: PortalNotificacionModel): Target =
        targetFrom(item.tipo, item.mascotaId, item.referenciaId)

    private fun targetFrom(tipo: String, mascotaId: String, referenciaId: String): Target {
        val key = tipo.lowercase()
        val section = when {
            key.contains("vacuna") && mascotaId.isNotBlank() -> Section.VACUNAS
            key.contains("cita") && mascotaId.isNotBlank() -> Section.CITAS
            key.contains("historial") && mascotaId.isNotBlank() -> Section.HISTORIAL
            mascotaId.isNotBlank() -> Section.MASCOTA_DETALLE
            else -> Section.NOTIFICACIONES
        }
        return Target(section, mascotaId, referenciaId)
    }

    fun clearIntentExtras(intent: Intent?) {
        intent?.removeExtra(EXTRA_CLIENTE_ID)
        intent?.removeExtra(EXTRA_TIPO)
        intent?.removeExtra(EXTRA_MASCOTA_ID)
        intent?.removeExtra(EXTRA_REFERENCIA_ID)
    }

    fun navigate(activity: PortalMainActivity, target: Target) {
        when (target.section) {
            Section.NOTIFICACIONES -> {
                activity.selectBottomNav(R.id.portal_nav_notificaciones)
                activity.replaceRootFragment(PortalNotificacionesFragment())
            }
            Section.MASCOTA_DETALLE -> {
                activity.selectBottomNav(R.id.portal_nav_mascotas)
                activity.replaceRootFragment(PortalMascotasFragment())
                activity.openExpedienteSection(
                    PortalMascotaDetalleFragment.newInstance(target.mascotaId)
                )
            }
            Section.VACUNAS -> {
                activity.selectBottomNav(R.id.portal_nav_mascotas)
                activity.replaceRootFragment(PortalMascotasFragment())
                activity.openExpedienteSection(
                    PortalVacunasListFragment.newInstance(target.mascotaId, "")
                )
            }
            Section.CITAS -> {
                activity.selectBottomNav(R.id.portal_nav_mascotas)
                activity.replaceRootFragment(PortalMascotasFragment())
                activity.openExpedienteSection(
                    PortalCitasListFragment.newInstance(target.mascotaId, "")
                )
            }
            Section.HISTORIAL -> {
                activity.selectBottomNav(R.id.portal_nav_mascotas)
                activity.replaceRootFragment(PortalMascotasFragment())
                activity.openExpedienteSection(
                    PortalHistorialListFragment.newInstance(target.mascotaId, "")
                )
            }
        }
    }
}
