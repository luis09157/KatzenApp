package com.example.katzen.Fragment.Paciente

import com.example.katzen.Model.PacienteModel
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Helper.ImageLoaderHelper
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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditarPacienteFragment : Fragment(), MediaHelper.MediaCallback {

    val TAG: String = "EditarPacienteFragment"

    private var _binding: VistaAgregarMascotaBinding? = null
    private val binding get() = _binding!!
    val FOLDER_NAME = "Mascotas"
    val PATH_FIREBASE = "Katzen/Mascota"
    companion object {
        var PACIENTE_EDIT: PacienteModel
            get() = com.example.katzen.Helper.StaffEditSessionBridge.pacienteEdit
            set(value) {
                com.example.katzen.Helper.StaffEditSessionBridge.pacienteEdit = value
            }
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
            ImageLoaderHelper.load(
                imageView = binding.fotoMascota,
                imageUrl = paciente.imageUrl,
                placeholderRes = R.drawable.avatar_sin_imagen_mascota,
                errorRes = R.drawable.avatar_sin_imagen_mascota,
                storageFolder = FOLDER_NAME,
                imageFileName = paciente.imageFileName
            )

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
                lifecycleScope.launch {
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
    private suspend fun editarCliente(pacienteModel: PacienteModel) {
        withContext(Dispatchers.Main) { ConfigLoading.showLoadingAnimation() }

        if (FirebaseStorageManager.hasSelectedImage()) {
            val imageUrl = withContext(Dispatchers.IO) {
                FirebaseStorageManager.uploadImage(FirebaseStorageManager.URI_IMG_SELECTED, FOLDER_NAME)
            }
            pacienteModel.imageUrl = imageUrl
        }

        try {
            val result = withContext(Dispatchers.IO) {
                FirebasePacienteUtil.editarMascota(pacienteModel.id, pacienteModel)
            }
            if (!isAdded) return

            result.onSuccess { message ->
                limpiarCampos()
                ConfigLoading.hideLoadingAnimation()
                DialogMaterialHelper.mostrarSuccessDialog(requireActivity(), message)
            }
            result.onFailure { exception ->
                ConfigLoading.hideLoadingAnimation()
                DialogMaterialHelper.mostrarErrorDialog(
                    requireActivity(),
                    "Error al editar el paciente: ${exception.message}"
                )
            }
        } catch (e: Exception) {
            if (!isAdded) return
            ConfigLoading.hideLoadingAnimation()
            DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error: ${e.message}")
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