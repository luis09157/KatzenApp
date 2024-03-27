package com.example.katzen.Fragment

import FirebaseProductoUtil
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.katzen.Config.Config
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.Helper.CalendarioUtil
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilHelper.Companion.hideKeyBoardWorld
import com.example.katzen.Helper.UtilHelper.Companion.hideKeyboard
import com.example.katzen.Model.ProductoModel
import com.example.katzen.R
import com.example.katzen.databinding.AddProductoFragmentBinding
import com.squareup.picasso.Picasso

class AddProductoFragment : Fragment() {
    val TAG : String  = "AddProductoFragment"
    private var _binding: AddProductoFragmentBinding? = null
    private val binding get() = _binding!!

    private val REQUEST_IMAGE_PICK = 100

    private var imagenUri: Uri? = null


    fun setProducto(producto: ProductoModel){
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
            it.hideKeyboard()
            ConfigLoading.showLoadingAnimation()
            try {
                btnGuardar(validarProducto())
            }catch (e : Exception){
                ConfigLoading.hideLoadingAnimation()
                 Log.e(TAG,e.message.toString())
            }
        }
        binding.btnEditar.setOnClickListener {
            it.hideKeyboard()
            ConfigLoading.showLoadingAnimation()
            try {
                btnEditar(validarProducto())
            }catch (e : Exception){
                ConfigLoading.hideLoadingAnimation()
                Log.e(TAG,e.message.toString())
            }
        }

        if (Config.PRODUCTO_EDIT.nombre != "") {
            initData()
        }

        return root
    }

    fun validarProducto() : ProductoModel {
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
    fun btnEditar(producto : ProductoModel){

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
    fun btnGuardar(producto: ProductoModel){
        if (producto != null) {
            producto.rutaImagen = imagenUri.toString()
            FirebaseProductoUtil.guardarProducto(requireContext(), producto, imagenUri!!)
        } else {
            ConfigLoading.hideLoadingAnimation()
        }
        cleanInputs()
    }

    fun initLoading(){
        ConfigLoading.LOTTIE_ANIMATION_VIEW = binding.lottieAnimationView
        ConfigLoading.CONT_ADD_PRODUCTO = binding.contAddProducto
        ConfigLoading.FRAGMENT_NO_DATA = binding.fragmentNoData.contNoData
    }

    private fun init() {
        Config.IMG_CHANGE = false
        binding.editTextFecha.editText!!.setText(CalendarioUtil.obtenerFechaHoraActual())
        convertirAMayusculas(binding.editTextNombre.editText!!)
        convertirAMayusculas(binding.editTextDescripcion)
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

    fun cleanInputs(){
        binding.imageViewProducto.setImageResource(R.drawable.ic_imagen)
        binding.editTextNombre.editText!!.setText("")
        binding.editTextCosto.editText!!.setText("")
        binding.editTextDescripcion!!.setText("")
        binding.editTextPrecioVenta.editText!!.setText("")
        binding.editTextFecha.editText!!.setText(CalendarioUtil.obtenerFechaHoraActual())
    }
    fun convertirAMayusculas(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No es necesario implementar esta función
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No es necesario implementar esta función
            }

            override fun afterTextChanged(s: Editable?) {
                // Convierte el texto a mayúsculas y lo establece en el EditText
                s?.let {
                    val newText = it.toString().toUpperCase()
                    if (newText != it.toString()) {
                        editText.setText(newText)
                        editText.setSelection(newText.length)
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
