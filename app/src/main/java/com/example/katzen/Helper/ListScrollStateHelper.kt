package com.example.katzen.Helper

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Conserva la posición de scroll al entrar a un detalle y volver con el back stack.
 * Guarda el id del ítem seleccionado y restaura tras [restoreIfPending].
 */
object ListScrollStateHelper {

    private data class SavedSelection(
        val itemId: String,
        val fallbackPosition: Int,
        val offsetPx: Int
    )

    private val savedSelections = mutableMapOf<String, SavedSelection>()
    private val pendingRestore = mutableSetOf<String>()

    fun saveSelection(listKey: String, recyclerView: RecyclerView, itemId: String) {
        if (itemId.isBlank()) return

        val layoutManager = recyclerView.layoutManager
        val fallbackPosition = when (layoutManager) {
            is LinearLayoutManager -> layoutManager.findFirstVisibleItemPosition()
            is GridLayoutManager -> layoutManager.findFirstVisibleItemPosition()
            else -> RecyclerView.NO_POSITION
        }.coerceAtLeast(0)

        val offsetPx = when (layoutManager) {
            is LinearLayoutManager -> layoutManager.findViewByPosition(fallbackPosition)?.top ?: 0
            is GridLayoutManager -> layoutManager.findViewByPosition(fallbackPosition)?.top ?: 0
            else -> 0
        }

        savedSelections[listKey] = SavedSelection(
            itemId = itemId,
            fallbackPosition = fallbackPosition,
            offsetPx = offsetPx
        )
        pendingRestore.add(listKey)
    }

    fun restoreIfPending(listKey: String, recyclerView: RecyclerView, itemIds: List<String>) {
        if (!pendingRestore.remove(listKey)) return

        val state = savedSelections[listKey] ?: return
        if (itemIds.isEmpty()) return

        val position = state.itemId.let { id ->
            itemIds.indexOf(id).takeIf { it >= 0 }
        } ?: state.fallbackPosition.coerceIn(0, itemIds.lastIndex)

        recyclerView.post {
            if (recyclerView.layoutManager == null) return@post
            scrollToPositionWithOffset(recyclerView, position, state.offsetPx)
        }
    }

    fun clear(listKey: String) {
        savedSelections.remove(listKey)
        pendingRestore.remove(listKey)
    }

    private fun scrollToPositionWithOffset(
        recyclerView: RecyclerView,
        position: Int,
        offsetPx: Int
    ) {
        when (val layoutManager = recyclerView.layoutManager) {
            is GridLayoutManager -> layoutManager.scrollToPositionWithOffset(position, offsetPx)
            is LinearLayoutManager -> layoutManager.scrollToPositionWithOffset(position, offsetPx)
            else -> layoutManager?.scrollToPosition(position)
        }
    }
}

object ListScrollKeys {
    const val PACIENTES = "list_pacientes"
    const val CLIENTES = "list_clientes"
    const val CLIENTE_DETALLE_PACIENTES = "list_cliente_detalle_pacientes"
    const val CAMPANIA_PACIENTES = "list_campania_pacientes"
    const val CAMPANIA_EVENTOS = "list_campania_eventos"
    const val CAMPANIA_ANIOS = "list_campania_anios"
    const val CAMPANIA_MESES = "list_campania_meses"
    const val VIAJES_ANIOS = "list_viajes_anios"
    const val VIAJES_MESES = "list_viajes_meses"
    const val VIAJES_DETALLE = "list_viajes_detalle"
    const val MENU_PRINCIPAL = "list_menu_principal"
    const val MENU_PRODUCTOS = "list_menu_productos"
    const val SELECCION_CLIENTES = "list_seleccion_clientes"
    const val SELECCION_PACIENTES = "list_seleccion_pacientes"
    const val MEDICAMENTOS = "list_medicamentos"
    const val ALIMENTOS = "list_alimentos"
    const val PRODUCTOS_VARIOS = "list_productos_varios"
    const val PRODUCTOS_ESTETICA = "list_productos_estetica"
    const val SERVICIOS = "list_servicios"
    const val PROCEDIMIENTOS = "list_procedimientos"
    const val AUXILIARES = "list_auxiliares"
    const val VENTAS = "list_ventas"
    const val INVENTARIO = "list_inventario"
    const val VACUNAS = "list_vacunas"
}
