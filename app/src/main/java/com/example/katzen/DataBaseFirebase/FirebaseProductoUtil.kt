package com.example.katzen.DataBaseFirebase

import com.example.katzen.Model.Producto
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FirebaseProductoUtil {
    companion object {
        private const val PRODUCTOS_PATH = "productos" // Ruta donde se guardarán los productos

        @JvmStatic
        fun guardarProducto(producto: Producto) {
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val referenciaProductos: DatabaseReference = database.getReference(PRODUCTOS_PATH)

            val productoId = producto.id // Puedes usar el ID generado automáticamente o proporcionar uno personalizado

            // Guardar el producto en la base de datos
            referenciaProductos.child(productoId).setValue(producto)
                .addOnSuccessListener {
                    // Operación exitosa
                    println("Producto guardado exitosamente en la base de datos.")
                }
                .addOnFailureListener { exception ->
                    // Manejar errores
                    println("Error al guardar el producto en la base de datos: $exception")
                }
        }
    }
}
