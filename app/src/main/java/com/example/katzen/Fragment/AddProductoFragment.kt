package com.example.katzen.Fragment

import FirebaseProductoUtil
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.katzen.Config.Config
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.Helper.CalendarioUtil
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilHelper.Companion.hideKeyBoardWorld
import com.example.katzen.Helper.UtilHelper.Companion.hideKeyboard
import com.example.katzen.Model.Producto
import com.example.katzen.R
import com.example.katzen.databinding.AddProductoFragmentBinding
import com.squareup.picasso.Picasso

class AddProductoFragment : Fragment() {
    val TAG : String  = "AddProductoFragment"
    private var _binding: AddProductoFragmentBinding? = null
    private val binding get() = _binding!!

    private val REQUEST_IMAGE_PICK = 100

    private var imagenUri: Uri? = null


    fun setProducto(producto: Producto){
        Config.PRODUCTO_EDIT = producto
    }
    fun enableBtnEditar(){
        binding.btnEditar.visibility = View.VISIBLE
        binding.btnGuardar.visibility = View.GONE
    }
    fun enableBtnGuardar(){
        binding.btnEditar.visibility = View.GONE
        binding.btnGuardar.visibility = View.VISIBLE
    }
    fun initData() {
        Config.PRODUCTO_EDIT?.let { prod ->
            with(binding) {
                editTextNombre.editText?.setText(prod.nombre)
                editTextCosto.editText?.setText(prod.costo.toString())
                editTextPrecioVenta.editText?.setText(prod.precioVenta.toString())
                editTextDescripcion.setText(prod.descripcion)
                editTextFecha.editText?.setText(prod.fecha)
                Picasso.get().load(prod.rutaImagen).into(imageViewProducto)
            }
        }
        enableBtnEditar()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddProductoFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        init()
        enableBtnGuardar()
        initLoading()


        root.setOnClickListener { hideKeyBoardWorld(requireActivity(),it) }
        binding.editTextFecha2.setOnFocusChangeListener { view, isFocus ->
            hideKeyBoardWorld(requireActivity(),root)
            if (isFocus) {
                CalendarioUtil.mostrarCalendario(requireContext(), binding.editTextFecha)
                view.hideKeyboard()
            }
        }
        binding.editTextFecha2.setOnClickListener { view ->
            CalendarioUtil.mostrarCalendario(requireContext(), binding.editTextFecha)
            view.hideKeyboard()
        }


        binding.btnSeleccionarFoto.setOnClickListener {
            seleccionarFoto()
        }

        binding.btnGuardar.setOnClickListener {
            ConfigLoading.showLoadingAnimation()
            btnGuardar(validarProducto())
        }
        binding.btnEditar.setOnClickListener {
            ConfigLoading.showLoadingAnimation()
            btnEditar(validarProducto())
        }
        binding.spUnidadMedida.setOnClickListener {
            it.hideKeyboard()
        }

        if (Config.PRODUCTO_EDIT.nombre != "") {
            initData()
        }

        return root
    }

    fun validarProducto() : Producto {
        val producto = ValidadorProducto.validarYCrearProducto(
            requireContext(),
            binding.editTextNombre,
            binding.editTextPrecioVenta,
            binding.editTextCosto,
            binding.editTextFecha,
            binding.editTextDescripcion,
            imagenUri
        )

        return producto!!
    }
    fun btnEditar(producto : Producto){

        if (producto != null) {
            try {
                producto.rutaImagen = imagenUri.toString()
                FirebaseProductoUtil.editarProducto(requireContext(), producto, imagenUri)
            }catch (e : Exception){
                ConfigLoading.hideLoadingAnimation()
                DialogMaterialHelper.mostrarErrorDialog(requireContext(), requireContext().getString(R.string.error_editing_product))
                Log.e(TAG, "Error al obtener la URI de la imagen: ${e.message}")
            }

        } else {
            DialogMaterialHelper.mostrarErrorDialog(requireContext(), requireContext().getString(R.string.error_editing_product))
            ConfigLoading.hideLoadingAnimation()
        }
    }
    fun btnGuardar(producto: Producto){
        if (producto != null) {
            producto.rutaImagen = imagenUri.toString()
            FirebaseProductoUtil.guardarProducto(requireContext(), producto, imagenUri!!)
        } else {
            ConfigLoading.hideLoadingAnimation()
        }
    }

    fun initLoading(){
        ConfigLoading.LOTTIE_ANIMATION_VIEW = binding.lottieAnimationView
        ConfigLoading.CONT_ADD_PRODUCTO = binding.contAddProducto
        ConfigLoading.FRAGMENT_NO_DATA = binding.fragmentNoData.contNoData
    }

    private fun init() {
        val adapter = ArrayAdapter(requireActivity(),
            android.R.layout.simple_list_item_1,Config.UNIDAD_MEDIDA)
        binding.spUnidadMedida.setAdapter(adapter)
        Config.IMG_CHANGE = false
        binding.editTextFecha.editText!!.setText(CalendarioUtil.obtenerFechaHoraActual())
    }

    private fun seleccionarFoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            imagenUri = data.data
            if(binding.btnEditar.isVisible){
                Config.IMG_CHANGE = true
            }else{
                Config.IMG_CHANGE = false
            }
            binding.imageViewProducto.setImageURI(imagenUri)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
