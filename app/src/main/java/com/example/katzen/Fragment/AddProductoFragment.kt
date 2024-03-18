package com.example.katzen.Fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.katzen.Config.Config
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.Helper.CalendarioUtil
import com.example.katzen.Helper.UtilHelper.Companion.hideKeyboard
import com.example.katzen.R
import com.example.katzen.databinding.AddProductoFragmentBinding

class AddProductoFragment : Fragment() {

    private var _binding: AddProductoFragmentBinding? = null
    private val binding get() = _binding!!

    private val REQUEST_IMAGE_PICK = 100

    private var imagenUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddProductoFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        init()
        initLoading()

        binding.editTextFecha.setOnFocusChangeListener { view, isFocus ->
            if (isFocus) {
                CalendarioUtil.mostrarCalendario(requireContext(), binding.editTextFecha)
            }
        }
        binding.editTextFecha.setOnClickListener {
            CalendarioUtil.mostrarCalendario(requireContext(), binding.editTextFecha)
        }

        binding.btnSeleccionarFoto.setOnClickListener {
            seleccionarFoto()
        }

        binding.btnGuardar.setOnClickListener {
            ConfigLoading.showLoadingAnimation()
            val producto = ValidadorProducto.validarYCrearProducto(
                requireContext(),
                binding.editTextNombre,
                binding.editTextPrecioVenta,
                binding.editTextCosto,
                binding.editTextFecha,
                imagenUri
            )

            if (producto != null) {
                producto.rutaImagen = imagenUri.toString() // Asignar la URI de la imagen al producto
                FirebaseProductoUtil.guardarProducto(requireContext(), producto, imagenUri!!)
                root.hideKeyboard()
            } else {
                ConfigLoading.hideLoadingAnimation()
                // Alguno de los campos es inválido, no guardes el producto o muestra un mensaje de error adicional si lo deseas.
            }

        }

        return root
    }

    fun initLoading(){
        ConfigLoading.LOTTIE_ANIMATION_VIEW = binding.lottieAnimationView
        ConfigLoading.CONT_ADD_PRODUCTO = binding.contAddProducto
        ConfigLoading.FRAGMENT_NO_DATA = binding.fragmentNoData.contNoData
    }

    private fun init() {
        binding.editTextFecha.setText(CalendarioUtil.obtenerFechaHoraActual())
    }

    private fun seleccionarFoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            imagenUri = data.data
            binding.imageViewProducto.setImageURI(imagenUri)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
