package com.example.katzen.Fragment.Cliente

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseClienteUtil
import com.example.katzen.DataBaseFirebase.FirebaseStorageManager
import com.example.katzen.Fragment.Paciente.AddPacienteFragment
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UpperCaseTextWatcher
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper.Companion.hideKeyboard
import com.example.katzen.Model.ClienteModel
import com.example.katzen.R
import com.example.katzen.databinding.AddClienteFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EditClienteFragment : Fragment() {
    val TAG : String  = "EditClienteFragment"

    private var _binding: AddClienteFragmentBinding? = null
    private val binding get() = _binding!!
    val FOLDER_NAME = "Clientes"
    val PATH_FIREBASE = "Katzen/Cliente"
    companion object{
        var CLIENTE_EDIT : ClienteModel = ClienteModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddClienteFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        requireActivity().title = "Editar Cliente"
        binding.btnGuardar.text = "Editar"

        initLoading()
        initListeners()
        init()
        initValues()

        return root
    }
    fun initLoading(){
        ConfigLoading.LOTTIE_ANIMATION_VIEW = binding.lottieAnimationView
        ConfigLoading.CONT_ADD_PRODUCTO = binding.contAddProducto
        ConfigLoading.FRAGMENT_NO_DATA = binding.fragmentNoData.contNoData
    }
    fun init(){
        FirebaseStorageManager.URI_IMG_SELECTED = Uri.EMPTY

       /* UpperCaseTextWatcher.UpperText(binding.textNombre)
        UpperCaseTextWatcher.UpperText(binding.textAppellidoPaterno)
        UpperCaseTextWatcher.UpperText(binding.textAppellidoMaterno)
        UpperCaseTextWatcher.UpperText(binding.textCalle)
        UpperCaseTextWatcher.UpperText(binding.textMunicipio)
        UpperCaseTextWatcher.UpperText(binding.textTelefono)
        UpperCaseTextWatcher.UpperText(binding.textColonia)*/
    }
    fun initValues() {
        // Verifica si CLIENTE_EDIT no es nulo antes de acceder a sus propiedades
        CLIENTE_EDIT?.let { cliente ->
            if (cliente.imageUrl.isNotEmpty()) {
                Glide.with(binding.imgPerfil.context)
                    .load(cliente.imageUrl)
                    .placeholder(R.drawable.ic_perfil) // Establecer la imagen predeterminada
                    .error(R.drawable.no_disponible_rosa) // Establecer una imagen en caso de error al cargar
                    .into(binding.imgPerfil)
            } else {
                binding.imgPerfil?.setImageResource(R.drawable.no_disponible_rosa)
            }

            binding.textNombre.setText(cliente.nombre)
            binding.textAppellidoPaterno.setText(cliente.apellidoPaterno)
            binding.textAppellidoMaterno.setText(cliente.apellidoMaterno)
            binding.textExpediente.setText(cliente.expediente)
            binding.textCalle.setText(cliente.calle)
            binding.textNumero.setText(cliente.numero)
            binding.textColonia.setText(cliente.colonia)
            binding.textMunicipio.setText(cliente.municipio)
            binding.textTelefono.setText(cliente.telefono)
            binding.textCorreo.setText(cliente.correo)
            binding.textGoogleMaps.setText(cliente.urlGoogleMaps)
            binding.textKilometrosCasa.setText(cliente.kilometrosCasa)
        }
    }

    fun initListeners(){
        binding.btnCancelar.setOnClickListener {
            it.hideKeyboard()
            UtilFragment.changeFragment(requireContext() , ClienteFragment() ,TAG)
        }
        binding.btnSubirImagen.setOnClickListener {
            it.hideKeyboard()
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, AddPacienteFragment.PICK_IMAGE_REQUEST)
        }
        binding.btnGuardar.setOnClickListener {
            it.hideKeyboard()

            setCliente()

            val validationResult = ClienteModel.validarCliente(requireContext(), CLIENTE_EDIT)
            if (validationResult.isValid) {
                GlobalScope.launch(Dispatchers.IO) {
                    editarCliente(CLIENTE_EDIT)
                }
            } else {
                println(validationResult.message)
                ConfigLoading.hideLoadingAnimation()
                DialogMaterialHelper.mostrarErrorDialog(requireActivity(), validationResult.message)
            }
        }
    }
    fun setCliente(){
        val nombre = binding.textNombre.text.toString()
        val apellidoPaterno = binding.textAppellidoPaterno.text.toString()
        val apellidoMaterno = binding.textAppellidoMaterno.text.toString()
        val expediente = binding.textExpediente.text.toString()
        val correo = binding.textCorreo.text.toString()
        val telefono = binding.textTelefono.text.toString()
        val calle = binding.textCalle.text.toString()
        val numero = binding.textNumero.text.toString()
        val colonia = binding.textColonia.text.toString()
        val municipio = binding.textMunicipio.text.toString()
        val googleMaps = binding.textGoogleMaps.text.toString()

        CLIENTE_EDIT.nombre = nombre
        CLIENTE_EDIT.apellidoPaterno = apellidoPaterno
        CLIENTE_EDIT.apellidoMaterno = apellidoMaterno
        CLIENTE_EDIT.expediente = expediente
        CLIENTE_EDIT.correo = correo
        CLIENTE_EDIT.telefono = telefono
        CLIENTE_EDIT.calle = calle
        CLIENTE_EDIT.numero = numero
        CLIENTE_EDIT.colonia = colonia
        CLIENTE_EDIT.municipio = municipio
        CLIENTE_EDIT.urlGoogleMaps = googleMaps
    }
    fun editarCliente(clienteModel: ClienteModel){
        GlobalScope.launch(Dispatchers.IO) {
            if (FirebaseStorageManager.hasSelectedImage()){
                requireActivity().runOnUiThread {  ConfigLoading.showLoadingAnimation() }
                val imageUrl = FirebaseStorageManager.uploadImage(FirebaseStorageManager.URI_IMG_SELECTED, FOLDER_NAME)
                println("URL de descarga de la imagen: $imageUrl")
                clienteModel.imageUrl = imageUrl

                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val result = FirebaseClienteUtil.editarCliente(clienteModel.id, clienteModel)
                        result.onSuccess { message ->
                            requireActivity().runOnUiThread {
                                ConfigLoading.hideLoadingAnimation()
                                DialogMaterialHelper.mostrarSuccessDialog(requireActivity(), message)
                            }
                        }
                        result.onFailure { exception ->
                            requireActivity().runOnUiThread {
                                ConfigLoading.hideLoadingAnimation()
                                DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error al editar el cliente: ${exception.message}")
                            }
                        }
                    } catch (e: Exception) {
                        requireActivity().runOnUiThread {
                            ConfigLoading.hideLoadingAnimation()
                            DialogMaterialHelper.mostrarErrorDialog(requireActivity(),  "Error: ${e.message}")
                        }
                    }
                }
            }else{
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val result = FirebaseClienteUtil.editarCliente(clienteModel.id, clienteModel)
                        result.onSuccess { message ->
                            requireActivity().runOnUiThread {
                                ConfigLoading.hideLoadingAnimation()
                                DialogMaterialHelper.mostrarSuccessDialog(requireActivity(), message)
                            }
                        }
                        result.onFailure { exception ->
                            requireActivity().runOnUiThread {
                                ConfigLoading.hideLoadingAnimation()
                                DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error al editar el cliente: ${exception.message}")
                            }
                        }
                    } catch (e: Exception) {
                        requireActivity().runOnUiThread {
                            ConfigLoading.hideLoadingAnimation()
                            DialogMaterialHelper.mostrarErrorDialog(requireActivity(),  "Error: ${e.message}")
                        }
                    }
                }
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    fun limpiarCampos(){
        requireActivity().runOnUiThread {
            binding.apply {
                textNombre.text!!.clear()
                textAppellidoPaterno.text!!.clear()
                textAppellidoMaterno.text!!.clear()
                textExpediente.text!!.clear()
                textCorreo.text!!.clear()
                textTelefono.text!!.clear()
                textCalle.text!!.clear()
                textNumero.text!!.clear()
                textColonia.text!!.clear()
                textMunicipio.text!!.clear()
                textGoogleMaps.text!!.clear()
                imgPerfil.setImageResource(R.drawable.ic_imagen)
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AddPacienteFragment.PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            FirebaseStorageManager.URI_IMG_SELECTED = data.data!!
            // You can now upload this image to Firebase Storage and display it in the ImageView

            binding.imgPerfil.setImageURI(FirebaseStorageManager.URI_IMG_SELECTED)
        }
    }
    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    UtilFragment.changeFragment(requireContext(), ClienteDetalleFragment(), TAG)
                }
            })
    }
}
