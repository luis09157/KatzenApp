package com.example.katzen.Helper

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File

class MediaHelper(private val fragment: Fragment) {

    interface MediaCallback {
        fun onMediaSelected(uri: Uri?)
    }

    private var callback: MediaCallback? = null
    private var photoUri: Uri? = null

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val GALLERY_PERMISSION_CODE = 101
        private const val CAMERA_REQUEST_CODE = 102
        private const val GALLERY_REQUEST_CODE = 103
    }

    fun setMediaCallback(callback: MediaCallback) {
        this.callback = callback
    }

    fun showMediaOptionsDialog() {
        val options = arrayOf("Tomar foto", "Seleccionar de galería")

        val builder = android.app.AlertDialog.Builder(fragment.requireContext())
        builder.setTitle("Selecciona una opción")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openCamera() // Tomar foto
                1 -> openGallery() // Seleccionar de galería
            }
        }
        builder.show()
    }

    private fun openCamera() {
        if (checkPermission(Manifest.permission.CAMERA)) {
            startCamera()
        } else {
            requestPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE)
        }
    }

    private fun openGallery() {
        if (checkPermissionForGallery()) {
            startGallery()
        } else {
            requestGalleryPermission()
        }
    }

    private fun startCamera() {
        val photoFile = File(
            fragment.requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "photo_${System.currentTimeMillis()}.jpg"
        )
        photoUri = FileProvider.getUriForFile(
            fragment.requireContext(),
            "${fragment.requireContext().packageName}.provider",
            photoFile
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        fragment.startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun startGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        fragment.startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val uri = when (requestCode) {
                CAMERA_REQUEST_CODE -> photoUri
                GALLERY_REQUEST_CODE -> data?.data
                else -> null
            }
            callback?.onMediaSelected(uri)
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            fragment.requireContext(), permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(permission: String, requestCode: Int) {
        fragment.requestPermissions(arrayOf(permission), requestCode)
    }

    private fun checkPermissionForGallery(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            fragment.requestPermissions(
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                GALLERY_PERMISSION_CODE
            )
        } else {
            fragment.requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                GALLERY_PERMISSION_CODE
            )
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                CAMERA_PERMISSION_CODE -> openCamera()
                GALLERY_PERMISSION_CODE -> openGallery()
            }
        } else {
            when (requestCode) {
                CAMERA_PERMISSION_CODE -> Toast.makeText(
                    fragment.requireContext(),
                    "Permiso denegado para acceder a la cámara",
                    Toast.LENGTH_SHORT
                ).show()
                GALLERY_PERMISSION_CODE -> Toast.makeText(
                    fragment.requireContext(),
                    "Permiso denegado para acceder a la galería",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}