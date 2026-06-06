package com.example.katzen.Helper

import com.ninodev.katzen.R

/**
 * Permisos staff según staffRole (claims / AuthPerfiles).
 * Admin tiene acceso total; roles desconocidos conservan acceso clínico básico.
 */
object StaffRoleHelper {

    enum class Permission {
        MANAGE_CLIENTS,
        MANAGE_PATIENTS,
        MANAGE_CATALOG,
        SALES,
        INVENTORY,
        REACTIVATE_RECORDS,
        OPERATIONS,
        CAMPAIGNS
    }

    private val clinicalRoles = setOf("doctor", "vet", "medico", "médico")
    private val receptionRoles = setOf("recepcion", "reception", "recepción")

    fun normalize(staffRole: String?): String =
        staffRole?.trim()?.lowercase().orEmpty()

    fun hasPermission(staffRole: String?, permission: Permission): Boolean {
        val role = normalize(staffRole)
        if (role == "admin") return true

        return when (permission) {
            Permission.MANAGE_CLIENTS,
            Permission.MANAGE_PATIENTS -> true
            Permission.SALES -> role in receptionRoles || isClinicalOrDefault(role)
            Permission.MANAGE_CATALOG,
            Permission.INVENTORY,
            Permission.CAMPAIGNS -> isClinicalOrDefault(role)
            Permission.REACTIVATE_RECORDS,
            Permission.OPERATIONS -> false
        }
    }

    private fun isClinicalOrDefault(role: String): Boolean =
        role in clinicalRoles || role.isBlank() || role == "staff"

    fun canAccessNavItem(staffRole: String?, itemId: Int): Boolean {
        val permission = when (itemId) {
            R.id.nav_home,
            R.id.nav_perfil,
            R.id.nav_cerrar_sesion -> return true
            R.id.nav_cliente -> Permission.MANAGE_CLIENTS
            R.id.nav_paciente -> Permission.MANAGE_PATIENTS
            R.id.nav_productos -> Permission.MANAGE_CATALOG
            R.id.nav_venta -> Permission.SALES
            R.id.nav_inventario -> Permission.INVENTORY
            R.id.nav_inactivos -> Permission.REACTIVATE_RECORDS
            R.id.nav_fuel,
            R.id.nav_payment_card,
            R.id.nav_viajes -> Permission.OPERATIONS
            R.id.nav_campania -> Permission.CAMPAIGNS
            else -> return true
        }
        return hasPermission(staffRole, permission)
    }

    fun canAccessMenuTitle(staffRole: String?, title: String, titles: MenuTitles): Boolean {
        val itemId = when (title) {
            titles.gasolina -> R.id.nav_fuel
            titles.pagoTarjeta -> R.id.nav_payment_card
            titles.paciente -> R.id.nav_paciente
            titles.cliente -> R.id.nav_cliente
            titles.productos -> R.id.nav_productos
            titles.venta -> R.id.nav_venta
            titles.inventario -> R.id.nav_inventario
            titles.viajes -> R.id.nav_viajes
            titles.campania -> R.id.nav_campania
            else -> return true
        }
        return canAccessNavItem(staffRole, itemId)
    }

    data class MenuTitles(
        val gasolina: String,
        val pagoTarjeta: String,
        val paciente: String,
        val cliente: String,
        val productos: String,
        val venta: String,
        val inventario: String,
        val viajes: String,
        val campania: String
    )

    fun roleLabelRes(staffRole: String?): Int = when (normalize(staffRole)) {
        "admin" -> R.string.perfil_rol_admin
        "doctor", "vet", "medico", "médico" -> R.string.perfil_rol_doctor
        "recepcion", "reception", "recepción" -> R.string.perfil_rol_recepcion
        else -> R.string.perfil_rol_staff
    }
}
