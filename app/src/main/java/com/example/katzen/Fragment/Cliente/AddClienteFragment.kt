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
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseDatabaseManager
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

class AddClienteFragment : Fragment() {
    val TAG : String  = "AddClienteFragment"

    private var _binding: AddClienteFragmentBinding? = null
    private val binding get() = _binding!!
    val FOLDER_NAME = "Clientes"
    val PATH_FIREBASE = "Katzen/Cliente"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddClienteFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        requireActivity().title = "Añdadir Cliente"

        initLoading()
        initListeners()
        init()

        return root
    }

    fun initLoading(){
        ConfigLoading.LOTTIE_ANIMATION_VIEW = binding.lottieAnimationView
        ConfigLoading.CONT_ADD_PRODUCTO = binding.contAddProducto
        ConfigLoading.FRAGMENT_NO_DATA = binding.fragmentNoData.contNoData
    }
    fun init(){
        FirebaseStorageManager.URI_IMG_SELECTED = Uri.EMPTY

        UpperCaseTextWatcher.UpperText(binding.textNombre)
        UpperCaseTextWatcher.UpperText(binding.textAppellidoPaterno)
        UpperCaseTextWatcher.UpperText(binding.textAppellidoMaterno)
        UpperCaseTextWatcher.UpperText(binding.textCalle)
        UpperCaseTextWatcher.UpperText(binding.textMunicipio)
        UpperCaseTextWatcher.UpperText(binding.textTelefono)
        UpperCaseTextWatcher.UpperText(binding.textColonia)
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
            val nombre = binding.textNombre.text.toString()
            val apellidoPaterno = binding.textAppellidoPaterno.text.toString()
            val apellidoMaterno = binding.textAppellidoMaterno.text.toString()
            val correo = binding.textCorreo.text.toString()
            val telefono = binding.textTelefono.text.toString()
            val calle = binding.textCalle.text.toString()
            val numero = binding.textNumero.text.toString()
            val colonia = binding.textColonia.text.toString()
            val municipio = binding.textMunicipio.text.toString()
            val googleMaps = binding.textGoogleMaps.text.toString()

            val cliente = ClienteModel(
                nombre = nombre,
                apellidoPaterno = apellidoPaterno,
                apellidoMaterno = apellidoMaterno,
                correo = correo,
                telefono = telefono,
                calle = calle,
                numero = numero,
                colonia = colonia,
                municipio = municipio,
                urlGoogleMaps = googleMaps
            )

            val validationResult = ClienteModel.validarCliente(requireContext(), cliente)
            if (validationResult.isValid) {
                GlobalScope.launch(Dispatchers.IO) {
                    guardarCliente(cliente)
                }
            } else {
                println(validationResult.message)
                ConfigLoading.hideLoadingAnimation()
                DialogMaterialHelper.mostrarErrorDialog(requireActivity(), validationResult.message)
            }
        }
    }
    fun guardarCliente(clienteModel: ClienteModel){
        GlobalScope.launch(Dispatchers.IO) {
            if (FirebaseStorageManager.hasSelectedImage()){
                requireActivity().runOnUiThread {  ConfigLoading.showLoadingAnimation() }
                val imageUrl = FirebaseStorageManager.uploadImage(FirebaseStorageManager.URI_IMG_SELECTED, FOLDER_NAME)
                println("URL de descarga de la imagen: $imageUrl")
                clienteModel.imageUrl = imageUrl

                GlobalScope.launch(Dispatchers.IO) {
                    val ( flag,message ) = FirebaseDatabaseManager.insertModel(clienteModel, clienteModel.id ,PATH_FIREBASE)

                    if (flag) {
                        limpiarCampos()
                        requireActivity().runOnUiThread {
                            ConfigLoading.hideLoadingAnimation()
                            DialogMaterialHelper.mostrarSuccessDialog(requireActivity(), "El cliente se guardó exitosamente.")
                        }
                    } else {
                        requireActivity().runOnUiThread {
                            ConfigLoading.hideLoadingAnimation()
                            DialogMaterialHelper.mostrarErrorDialog(requireActivity(), message)
                        }
                    }
                }
            }else{
                GlobalScope.launch(Dispatchers.IO) {
                    val ( flag,message ) =  FirebaseDatabaseManager.insertModel(clienteModel, clienteModel.id, PATH_FIREBASE)

                    if (flag) {
                        limpiarCampos()
                        requireActivity().runOnUiThread {  ConfigLoading.hideLoadingAnimation() }
                        DialogMaterialHelper.mostrarSuccessDialog(requireActivity(), "El cliente se guardó exitosamente.")
                    } else {
                        requireActivity().runOnUiThread {
                            ConfigLoading.hideLoadingAnimation()
                            DialogMaterialHelper.mostrarErrorDialog(requireActivity(), message)
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
                    UtilFragment.changeFragment(requireContext(), ClienteFragment(), TAG)
                }
            })
    }
}
