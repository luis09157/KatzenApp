package com.example.katzen.Fragment.Paciente

import PacienteModel
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.katzen.Config.Config
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebasePacienteUtil
import com.example.katzen.DataBaseFirebase.FirebaseStorageManager
import com.example.katzen.Fragment.Seleccionadores.SeleccionarClienteFragment
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.MediaHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper.Companion.hideKeyboard
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.VistaAgregarMascotaBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EditarPacienteFragment : Fragment(), MediaHelper.MediaCallback {

    val TAG: String = "EditarPacienteFragment"

    private var _binding: VistaAgregarMascotaBinding? = null
    private val binding get() = _binding!!
    val FOLDER_NAME = "Mascotas"
    val PATH_FIREBASE = "Katzen/Mascota"
    companion object {
        var PACIENTE_EDIT: PacienteModel = PacienteModel()
    }

    private lateinit var mediaHelper: MediaHelper

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

        // Inicializar MediaHelper
        mediaHelper = MediaHelper(this)
        mediaHelper.setMediaCallback(this)

        return root
    }

    fun initLoading() {
        ConfigLoading.init(
            binding.lottieAnimationView,
            binding.contAddProducto,
            binding.fragmentNoData.contNoData
        )
    }

    fun init() {
        val adapterSEXO = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, Config.SEXO)
        binding.spSexo.setAdapter(adapterSEXO)
        val adapterESPECIE = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, Config.ESPECIE)
        binding.spEspecie.setAdapter(adapterESPECIE)

        FirebaseStorageManager.URI_IMG_SELECTED = Uri.EMPTY
    }
    fun initValues() {
        PACIENTE_EDIT?.let { paciente ->
            if (paciente.imageUrl.isNotEmpty()) {
                Glide.with(binding.fotoMascota.context)
                    .load(paciente.imageUrl)
                    .placeholder(R.drawable.ic_perfil)
                    .error(R.drawable.no_disponible_rosa)
                    .into(binding.fotoMascota)
            } else {
                binding.fotoMascota.setImageResource(R.drawable.no_disponible_rosa)
            }

            binding.textNombre.setText(paciente.nombre)
            binding.textPeso.setText(paciente.peso)
            binding.editTextFecha2.setText(paciente.edad)
            binding.textColor.setText(paciente.color)
            binding.textCliente.setText(paciente.nombreCliente)
            binding.spSexo.setText(paciente.sexo)
            binding.spEspecie.setText(paciente.especie)
            binding.spRaza.setText(paciente.raza)
        }
    }
    fun initListeners() {
        binding.btnCancelar.setOnClickListener {
            it.hideKeyboard()
            UtilFragment.changeFragment(requireContext(), PacienteDetalleFragment(), TAG)
        }
        binding.btnSubirImagen.setOnClickListener {
            it.hideKeyboard()
            mediaHelper.showMediaOptionsDialog()  // Usar MediaHelper para abrir el diálogo de selección de imágenes
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
            UtilFragment.changeFragment(requireContext(), SeleccionarClienteFragment("EDIT_PACIENTE"), TAG)
        }
    }
    fun setPacienteModel() {
        PACIENTE_EDIT.nombre = binding.textNombre.text.toString()
        PACIENTE_EDIT.raza = binding.spRaza.text.toString()
        PACIENTE_EDIT.especie = binding.spEspecie.text.toString()
        PACIENTE_EDIT.sexo = binding.spSexo.text.toString()
        PACIENTE_EDIT.edad = binding.editTextFecha2.text.toString()
        PACIENTE_EDIT.color = binding.textColor.text.toString()
        PACIENTE_EDIT.nombreCliente = binding.textCliente.text.toString()
    }
    fun editarCliente(pacienteModel: PacienteModel) {
        GlobalScope.launch(Dispatchers.IO) {
            if (FirebaseStorageManager.hasSelectedImage()) {
                requireActivity().runOnUiThread { ConfigLoading.showLoadingAnimation() }
                val imageUrl = FirebaseStorageManager.uploadImage(FirebaseStorageManager.URI_IMG_SELECTED, FOLDER_NAME)
                println("URL de descarga de la imagen: $imageUrl")
                pacienteModel.imageUrl = imageUrl

                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val result = FirebasePacienteUtil.editarMascota(pacienteModel.id, pacienteModel)
                        result.onSuccess { message ->
                            requireActivity().runOnUiThread {
                                limpiarCampos()  // Limpiar los campos después de guardar
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
                            DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error: ${e.message}")
                        }
                    }
                }
            } else {
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val result = FirebasePacienteUtil.editarMascota(pacienteModel.id, pacienteModel)
                        result.onSuccess { message ->
                            requireActivity().runOnUiThread {
                                limpiarCampos()  // Limpiar los campos después de guardar
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
                            DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error: ${e.message}")
                        }
                    }
                }
            }
        }
    }
    override fun onMediaSelected(uri: Uri?) {
        uri?.let {
            FirebaseStorageManager.URI_IMG_SELECTED = it
            binding.fotoMascota.setImageURI(it)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    fun limpiarCampos() {
        requireActivity().runOnUiThread {
            binding.apply {
                textNombre.text!!.clear()
                editTextFecha2.text!!.clear()
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
        mediaHelper.handleActivityResult(requestCode, resultCode, data)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mediaHelper.onRequestPermissionsResult(requestCode, grantResults)
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