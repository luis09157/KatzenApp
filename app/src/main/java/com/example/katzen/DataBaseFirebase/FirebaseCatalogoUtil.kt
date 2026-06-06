package com.example.katzen.DataBaseFirebase

import com.example.katzen.Helper.RtdbActiveRecords
import com.example.katzen.Model.ProductoModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Lee catálogo legacy (Katzen/Producto) y moderno (Katzen/Productos por categoría)
 * sin modificar la base de datos.
 */
object FirebaseCatalogoUtil {

    private const val LEGACY_PATH = "Katzen/Producto"

    private val categoriaToPath = mapOf(
        "legacy" to LEGACY_PATH,
        "medicamentos" to "Katzen/Productos/Medicamentos",
        "alimentos" to "Katzen/Productos/Alimentos",
        "servicios" to "Katzen/Productos/Servicios",
        "varios" to "Katzen/Productos/Varios",
        "estetica" to "Katzen/Productos/Estetica",
        "estética" to "Katzen/Productos/Estetica",
        "procedimientos" to "Katzen/Productos/Procedimientos",
        "auxiliares" to "Katzen/Productos/Auxiliares"
    )

    fun resolveWritePath(categoria: String): String {
        val key = normalizeCategoria(categoria)
        return categoriaToPath[key] ?: LEGACY_PATH
    }

    fun normalizeCategoria(categoria: String): String =
        categoria.trim().lowercase().removeAccents()

    private fun String.removeAccents(): String = replace('é', 'e').replace('É', 'e')

    private val database = FirebaseDatabase.getInstance()

    suspend fun obtenerCatalogoUnificado(): List<ProductoModel> = coroutineScope {
        val legacy = async { loadPath(LEGACY_PATH, "Legacy") }
        val medicamentos = async { loadPath(categoriaToPath.getValue("medicamentos"), "Medicamentos") }
        val alimentos = async { loadPath(categoriaToPath.getValue("alimentos"), "Alimentos") }
        val servicios = async { loadPath(categoriaToPath.getValue("servicios"), "Servicios") }
        val varios = async { loadPath(categoriaToPath.getValue("varios"), "Varios") }
        val estetica = async { loadPath(categoriaToPath.getValue("estetica"), "Estética") }
        val procedimientos = async { loadPath(categoriaToPath.getValue("procedimientos"), "Procedimientos") }
        val auxiliares = async { loadPath(categoriaToPath.getValue("auxiliares"), "Auxiliares") }

        (legacy.await() + medicamentos.await() + alimentos.await() + servicios.await() +
            varios.await() + estetica.await() + procedimientos.await() + auxiliares.await())
            .distinctBy { "${it.categoria}:${it.id}" }
            .sortedBy { it.nombre.lowercase() }
    }

    private suspend fun loadPath(path: String, categoriaDefault: String): List<ProductoModel> {
        return suspendCancellableCoroutine { continuation ->
            database.getReference(path).get()
                .addOnSuccessListener { snapshot ->
                    val items = snapshot.children.mapNotNull { child ->
                        mapSnapshotToProducto(child, categoriaDefault)
                    }
                    continuation.resume(items)
                }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
    }

    private fun mapSnapshotToProducto(snapshot: DataSnapshot, categoriaDefault: String): ProductoModel? {
        if (!RtdbActiveRecords.isActive(snapshot)) return null

        snapshot.getValue(ProductoModel::class.java)?.let { legacy ->
            if (legacy.id.isBlank()) legacy.id = snapshot.key.orEmpty()
            if (legacy.categoria.isBlank()) legacy.categoria = categoriaDefault
            if (legacy.nombre.isNotBlank()) return legacy
        }

        val nombre = snapshot.child("nombre").getValue(String::class.java).orEmpty()
        if (nombre.isBlank()) return null

        val precioVenta = parseDouble(
            snapshot.child("precioVenta").value,
            snapshot.child("precio").value,
            snapshot.child("precioFinal").value,
            snapshot.child("precioUnitario").value
        )
        val costo = parseDouble(
            snapshot.child("costo").value,
            snapshot.child("costoCompra").value,
            snapshot.child("precio_compra").value
        )

        return ProductoModel(
            id = snapshot.key.orEmpty(),
            nombre = nombre,
            descripcion = snapshot.child("descripcion").getValue(String::class.java).orEmpty(),
            precioVenta = precioVenta,
            costo = costo,
            ganancia = (precioVenta - costo).coerceAtLeast(0.0),
            fecha = snapshot.child("fechaRegistro").getValue(String::class.java)
                ?: snapshot.child("fecha").getValue(String::class.java).orEmpty(),
            rutaImagen = snapshot.child("imagenUrl").getValue(String::class.java)
                ?: snapshot.child("rutaImagen").getValue(String::class.java).orEmpty(),
            categoria = snapshot.child("categoria").getValue(String::class.java)
                ?: snapshot.child("tipo").getValue(String::class.java)
                ?: categoriaDefault,
            proveedor = snapshot.child("proveedor").getValue(String::class.java).orEmpty()
        )
    }

    private fun parseDouble(vararg values: Any?): Double {
        for (value in values) {
            when (value) {
                is Number -> return value.toDouble()
                is String -> value.replace(",", ".").toDoubleOrNull()?.let { return it }
            }
        }
        return 0.0
    }
}
