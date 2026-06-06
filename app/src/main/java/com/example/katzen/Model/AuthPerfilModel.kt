package com.example.katzen.Model

data class AuthPerfilModel(
    val authUid: String = "",
    val email: String = "",
    val role: String = "",
    val staffRole: String = "",
    val clienteId: String = "",
    val staffRefId: String = "",
    val roles: List<String> = emptyList(),
    val activo: Boolean = true
) {
    private fun normalizedRoles(): Set<String> {
        val fromList = roles.map { it.trim().lowercase() }.filter { it.isNotBlank() }.toSet()
        val legacy = role.trim().lowercase().takeIf { it.isNotBlank() }?.let { setOf(it) }.orEmpty()
        return fromList + legacy
    }

    fun hasStaffAccess(): Boolean =
        activo && ("staff" in normalizedRoles() || role == "staff" || role == "dual")

    fun hasClientAccess(): Boolean {
        if (!activo || clienteId.isBlank()) return false
        val r = normalizedRoles()
        return "client" in r || role == "client" || role == "dual"
    }

    fun isDual(): Boolean = hasStaffAccess() && hasClientAccess()

    fun isStaff(): Boolean = hasStaffAccess() && !hasClientAccess()

    fun isClient(): Boolean = hasClientAccess() && !hasStaffAccess()

    fun isConfigured(): Boolean = hasStaffAccess() || hasClientAccess()
}
