package com.example.katzen.Fragment.Paciente

import PacienteModel
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Config.Config
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebasePacienteUtil
import com.example.katzen.DataBaseFirebase.FirebaseStorageManager
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UpperCaseTextWatcher
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper.Companion.hideKeyboard
import com.example.katzen.R
import com.example.katzen.databinding.VistaAgregarMascotaBinding
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EditarPacienteFragment : Fragment() {
    val TAG : String  = "EditarPacienteFragment"

    private var _binding: VistaAgregarMascotaBinding? = null
    private val binding get() = _binding!!
    val FOLDER_NAME = "Mascotas"
    val PATH_FIREBASE = "Katzen/Mascota"
    companion object{
        var PACIENTE_EDIT : PacienteModel = PacienteModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = VistaAgregarMascotaBinding.inflate(inflater, container, false)
        val root: View = binding.root

        requireActivity().title = "Editar Paciente"
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
        val adapterSEXO = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, Config.SEXO)
        binding.spSexo.setAdapter(adapterSEXO)
        val adapterESPECIE = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, Config.ESPECIE)
        binding.spEspecie.setAdapter(adapterESPECIE)

        FirebaseStorageManager.URI_IMG_SELECTED = Uri.EMPTY

        UpperCaseTextWatcher.UpperText(binding.textNombre)
        UpperCaseTextWatcher.UpperText(binding.textCliente)
        UpperCaseTextWatcher.UpperText(binding.textEdad)
        UpperCaseTextWatcher.UpperText(binding.textPeso)
        UpperCaseTextWatcher.UpperText(binding.textColor)
        UpperCaseTextWatcher.UpperText(binding.spRaza)
        UpperCaseTextWatcher.UpperText(binding.spEspecie)
        UpperCaseTextWatcher.UpperText(binding.spSexo)
    }
    fun initValues() {
        // Verifica si CLIENTE_EDIT no es nulo antes de acceder a sus propiedades
        PACIENTE_EDIT?.let { paciente ->
           if (paciente.imageUrl.isNotEmpty()) {
                Picasso.get()
                    .load(paciente.imageUrl)
                    .placeholder(R.drawable.ic_perfil) // Establecer la imagen predeterminada
                    .error(R.drawable.no_disponible_rosa) // Opcional: establecer una imagen en caso de error al cargar
                    .into(binding.fotoMascota)
            } else {
                binding.fotoMascota?.setImageResource(R.drawable.no_disponible_rosa)
            }
            binding.textNombre.setText(paciente.nombre)
            binding.textPeso.setText(paciente.peso)
            binding.textEdad.setText(paciente.edad)
            binding.textColor.setText(paciente.color)
            binding.textCliente.setText(paciente.nombreCliente)
            binding.spSexo.setText(paciente.sexo)
            binding.spEspecie.setText(paciente.especie)
            binding.spRaza.setText(paciente.raza)
        }
    }

    fun initListeners(){
        binding.btnCancelar.setOnClickListener {
            it.hideKeyboard()
            UtilFragment.changeFragment(requireContext(), PacienteFragment(), TAG)
        }
        binding.btnSubirImagen.setOnClickListener {
            it.hideKeyboard()
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, AddPacienteFragment.PICK_IMAGE_REQUEST)
        }
        binding.btnGuardar.setOnClickListener {
            it.hideKeyboard()

            setPacienteModel()

            val validationResult = PacienteModel.validarMascota(requireContext(), PACIENTE_EDIT)
            if (validationResult.isValid) {
                GlobalScope.launch(Dispatchers.IO) {
                    editarCliente(PACIENTE_EDIT)
                }
            } else {
                println(validationResult.message)
                ConfigLoading.hideLoadingAnimation()
                DialogMaterialHelper.mostrarErrorDialog(requireActivity(), validationResult.message)
            }
        }
        binding.textCliente.setOnClickListener {
            it.hideKeyboard()
            setPacienteModel()
            UtilFragment.changeFragment(requireContext() ,SeleccionarPacienteFragment(true) ,TAG)
        }
        binding.textCliente.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.hideKeyboard()
                setPacienteModel()
                UtilFragment.changeFragment(requireContext(), SeleccionarPacienteFragment(true), TAG)
            }
        }
    }
    fun setPacienteModel(){

        PACIENTE_EDIT.nombre = binding.textNombre.text.toString()
        PACIENTE_EDIT.raza =  binding.spRaza.text.toString()
        PACIENTE_EDIT.especie = binding.spEspecie.text.toString()
        PACIENTE_EDIT.sexo = binding.spSexo.text.toString()
        PACIENTE_EDIT.edad = binding.textEdad.text.toString()
        PACIENTE_EDIT.color = binding.textColor.text.toString()
        PACIENTE_EDIT.nombreCliente = binding.textCliente.text.toString()
    }
    fun editarCliente(pacienteModel: PacienteModel){
        GlobalScope.launch(Dispatchers.IO) {
            if (FirebaseStorageManager.hasSelectedImage()){
                requireActivity().runOnUiThread {  ConfigLoading.showLoadingAnimation() }
                val imageUrl = FirebaseStorageManager.uploadImage(FirebaseStorageManager.URI_IMG_SELECTED, FOLDER_NAME)
                println("URL de descarga de la imagen: $imageUrl")
                pacienteModel.imageUrl = imageUrl

                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val result = FirebasePacienteUtil.editarMascota(pacienteModel.id, pacienteModel)
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
                        val result = FirebasePacienteUtil.editarMascota(pacienteModel.id, pacienteModel)
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
                textEdad.text!!.clear()
                textColor.text!!.clear()
                textCliente.text!!.clear()
                textPeso.text!!.clear()
                spRaza.text!!.clear()
                spEspecie.text!!.clear()
                spSexo.text!!.clear()
                fotoMascota.setImageResource(R.drawable.ic_imagen)
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AddPacienteFragment.PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            FirebaseStorageManager.URI_IMG_SELECTED = data.data!!
            // You can now upload this image to Firebase Storage and display it in the ImageView

            binding.fotoMascota.setImageURI(FirebaseStorageManager.URI_IMG_SELECTED)
        }
    }
    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    UtilFragment.changeFragment(requireContext(), PacienteFragment(), TAG)
                }
            })
    }
}
