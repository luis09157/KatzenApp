package com.example.katzen.DataBaseFirebase

import android.app.Activity
import android.net.Uri
import com.example.katzen.Config.Config
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Model.ProductoModel
import com.ninodev.katzen.R
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class FirebaseProductoUtil {
    companion object {
        private const val PRODUCTOS_IMAGES_PATH = "Productos"

        private fun writePath(producto: ProductoModel): String =
            FirebaseCatalogoUtil.resolveWritePath(producto.categoria)

        @JvmStatic
        fun guardarProducto(activity: Activity, producto: ProductoModel, imagenUri: Uri) {
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val productPath = writePath(producto)
            val referenciaProductos: DatabaseReference = database.getReference(productPath)
            val storage: FirebaseStorage = FirebaseStorage.getInstance()
            val storageRef: StorageReference = storage.reference.child(PRODUCTOS_IMAGES_PATH)

            // Verificar si el nombre del producto ya está registrado
            referenciaProductos.orderByChild("nombre").equalTo(producto.nombre).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // El nombre del producto ya está registrado
                        DialogMaterialHelper.mostrarSuccessDialog(activity, activity.getString(R.string.dialog_product_name_exists))
                        ConfigLoading.hideLoadingAnimation()
                    } else {
                        // El nombre del producto no está registrado, guardar la imagen en Firebase Storage
                        val imagenRef = storageRef.child(UUID.randomUUID().toString()) // Generar un nombre único para la imagen
                        val uploadTask = imagenRef.putFile(imagenUri)

                        // Manejar el resultado de la subida de la imagen
                        uploadTask.addOnSuccessListener { uploadTask ->
                            // Obtener la URL de descarga de la imagen
                            imagenRef.downloadUrl.addOnSuccessListener { uri ->
                                val imageUrl = uri.toString()
                                producto.rutaImagen = imageUrl // Asignar la URL de la imagen al producto

                                // Guardar el producto en la base de datos
                                val productoId = producto.id // Puedes usar el ID generado automáticamente o proporcionar uno personalizado
                                referenciaProductos.child(productoId).setValue(producto)
                                    .addOnSuccessListener {
                                        // Operación exitosa
                                        DialogMaterialHelper.mostrarSuccessDialog(activity, activity.getString(R.string.dialog_product_saved_success))
                                        ConfigLoading.hideLoadingAnimation()
                                    }
                                    .addOnFailureListener { exception ->
                                        // Manejar errores
                                        DialogMaterialHelper.mostrarErrorDialog(activity, activity.getString(R.string.dialog_error_saving_product, exception))
                                        ConfigLoading.hideLoadingAnimation()
                                    }
                            }
                        }.addOnFailureListener { exception ->
                            // Manejar errores de la subida de la imagen
                            DialogMaterialHelper.mostrarErrorDialog(activity, activity.getString(R.string.dialog_error_uploading_image, exception))
                            ConfigLoading.hideLoadingAnimation()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejar errores
                    DialogMaterialHelper.mostrarErrorDialog(activity, activity.getString(R.string.dialog_database_query_error, error))
                    ConfigLoading.hideLoadingAnimation()
                }
            })
        }
        @JvmStatic
        fun obtenerListaProductos(listener: ValueEventListener) {
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val referenciaProductos: DatabaseReference =
                database.getReference(FirebaseCatalogoUtil.resolveWritePath("Legacy"))
            referenciaProductos.addValueEventListener(listener)
        }

        @JvmStatic
        fun removerListener(listener: ValueEventListener) {
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            database.getReference(FirebaseCatalogoUtil.resolveWritePath("Legacy"))
                .removeEventListener(listener)
        }
        @JvmStatic
        fun obtenerProducto(productoId: String, listener: ValueEventListener) {
            val database: FirebaseDatabase = FirebaseDatabase.getInstance()
            val referenciaProducto: DatabaseReference = database.getReference(
                "${FirebaseCatalogoUtil.resolveWritePath("Legacy")}/$productoId"
            )
            referenciaProducto.addListenerForSingleValueEvent(listener)
        }
        @JvmStatic
        fun editarProducto(activity: Activity, producto: ProductoModel, imagenUri: Uri?) {
            val database = FirebaseDatabase.getInstance()
            val referenciaProductos = database.getReference(writePath(producto))
            producto.id = Config.PRODUCTO_EDIT.id

            if (Config.IMG_CHANGE) {
                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.reference.child(PRODUCTOS_IMAGES_PATH)

                // Guardar la imagen en Firebase Storage
                val imagenRef = storageRef.child(UUID.randomUUID().toString()) // Generar un nombre único para la imagen
                val uploadTask = imagenRef.putFile(imagenUri!!)

                // Manejar el resultado de la subida de la imagen
                uploadTask.addOnSuccessListener { _ ->
                    // Obtener la URL de descarga de la imagen
                    imagenRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        producto.rutaImagen = imageUrl // Asignar la URL de la imagen al producto
                        actualizarProductoEnBaseDatos(activity, producto, referenciaProductos)
                    }.addOnFailureListener { exception ->
                        manejarErrorSubidaImagen(activity, exception)
                    }
                }.addOnFailureListener { exception ->
                    manejarErrorSubidaImagen(activity, exception)
                }
            } else {
                // Si no hay cambio de imagen, simplemente actualizar el producto sin subir nueva imagen
                producto.rutaImagen = Config.PRODUCTO_EDIT.rutaImagen
                actualizarProductoEnBaseDatos(activity, producto, referenciaProductos)
            }
        }

        private fun actualizarProductoEnBaseDatos(activity: Activity, producto: ProductoModel, referenciaProductos: DatabaseReference) {
            referenciaProductos.child(producto.id).setValue(producto)
                .addOnSuccessListener {
                    // Operación exitosa
                    DialogMaterialHelper.mostrarSuccessDialog(activity, activity.getString(R.string.dialog_product_updated_success))
                    ConfigLoading.hideLoadingAnimation()
                }
                .addOnFailureListener { exception ->
                    // Manejar errores
                    DialogMaterialHelper.mostrarErrorDialog(activity, activity.getString(R.string.dialog_error_updating_product, exception))
                    ConfigLoading.hideLoadingAnimation()
                }
        }

        private fun manejarErrorSubidaImagen(activity: Activity, exception: Exception) {
            // Manejar errores de la subida de la imagen
            DialogMaterialHelper.mostrarErrorDialog(activity, activity.getString(R.string.dialog_error_uploading_image, exception))
            ConfigLoading.hideLoadingAnimation()
        }

    }
}
