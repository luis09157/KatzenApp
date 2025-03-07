package com.example.katzen.Fragment.Cliente

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseDatabaseManager
import com.example.katzen.DataBaseFirebase.FirebaseStorageManager
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.MediaHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Helper.UtilHelper.Companion.hideKeyboard
import com.example.katzen.Model.ClienteModel
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.AddClienteFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.UUID

class AddClienteFragment : Fragment(), MediaHelper.MediaCallback {

    val TAG: String = "AddClienteFragment"

    private var _binding: AddClienteFragmentBinding? = null
    private val binding get() = _binding!!
    val FOLDER_NAME = "Clientes"
    val PATH_FIREBASE = "Katzen/Cliente"

    private lateinit var mediaHelper: MediaHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddClienteFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        requireActivity().title = "Añadir Cliente"

        initLoading()
        initListeners()
        init()

        // Inicializar MediaHelper
        mediaHelper = MediaHelper(this)
        mediaHelper.setMediaCallback(this)

        return root
    }

    fun initLoading() {
        ConfigLoading.LOTTIE_ANIMATION_VIEW = binding.lottieAnimationView
        ConfigLoading.CONT_ADD_PRODUCTO = binding.contAddProducto
        ConfigLoading.FRAGMENT_NO_DATA = binding.fragmentNoData.contNoData
    }
    fun init() {
        FirebaseStorageManager.URI_IMG_SELECTED = Uri.EMPTY

        /*UpperCaseTextWatcher.UpperText(binding.textNombre)
        UpperCaseTextWatcher.UpperText(binding.textAppellidoPaterno)
        UpperCaseTextWatcher.UpperText(binding.textAppellidoMaterno)
        UpperCaseTextWatcher.UpperText(binding.textCalle)
        UpperCaseTextWatcher.UpperText(binding.textMunicipio)
        UpperCaseTextWatcher.UpperText(binding.textTelefono)
        UpperCaseTextWatcher.UpperText(binding.textColonia)*/
    }
    fun initListeners() {
        binding.btnCancelar.setOnClickListener {
            it.hideKeyboard()
            UtilFragment.changeFragment(requireContext(), ClienteFragment(), TAG)
        }

        binding.btnSubirImagen.setOnClickListener {
            it.hideKeyboard()
            mediaHelper.showMediaOptionsDialog()  // Usar MediaHelper para abrir el diálogo de selección de imágenes
        }

        binding.btnGuardar.setOnClickListener {
            it.hideKeyboard()

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
            val kilometros = binding.textKilometrosCasa.text.toString()

            val cliente = ClienteModel(
                id = UUID.randomUUID().toString(),
                nombre = nombre,
                apellidoPaterno = apellidoPaterno,
                apellidoMaterno = apellidoMaterno,
                expediente = expediente,
                correo = correo,
                telefono = telefono,
                calle = calle,
                numero = numero,
                colonia = colonia,
                municipio = municipio,
                urlGoogleMaps = googleMaps,
                kilometrosCasa = kilometros,
                fecha = UtilHelper.getDate(), // Agregar fecha actual
                imageUrl = "",  // Valores predeterminados para evitar errores
                imageFileName = ""
            )

            val validationResult = ClienteModel.validarCliente(requireContext(), cliente)
            if (validationResult.isValid) {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {  // Reemplazo de GlobalScope
                    guardarCliente(cliente)
                }
            } else {
                println(validationResult.message)
                ConfigLoading.hideLoadingAnimation()

                activity?.let {
                    DialogMaterialHelper.mostrarErrorDialog(it, validationResult.message)
                }
            }
        }
    }

    fun guardarCliente(clienteModel: ClienteModel) {
        GlobalScope.launch(Dispatchers.IO) {
            if (FirebaseStorageManager.hasSelectedImage()) {
                requireActivity().runOnUiThread { ConfigLoading.showLoadingAnimation() }
                val imageUrl = FirebaseStorageManager.uploadImage(FirebaseStorageManager.URI_IMG_SELECTED, FOLDER_NAME)
                println("URL de descarga de la imagen: $imageUrl")
                clienteModel.imageUrl = imageUrl

                GlobalScope.launch(Dispatchers.IO) {
                    val (flag, message) = FirebaseDatabaseManager.insertModel(clienteModel, clienteModel.id, PATH_FIREBASE)

                    if (flag) {
                        limpiarCampos()  // Limpiar los campos después de guardar
                        requireActivity().runOnUiThread {
                            DialogMaterialHelper.mostrarConfirmDialog(requireActivity(), "El cliente se guardó exitosamente.") { confirmed ->
                                if (confirmed) {
                                    ConfigLoading.hideLoadingAnimation()
                                    UtilFragment.changeFragment(requireContext(), ClienteFragment(), TAG)
                                }
                            }
                        }
                    } else {
                        requireActivity().runOnUiThread {
                            ConfigLoading.hideLoadingAnimation()
                            DialogMaterialHelper.mostrarErrorDialog(requireActivity(), message)
                        }
                    }
                }
            } else {
                GlobalScope.launch(Dispatchers.IO) {
                    val (flag, message) = FirebaseDatabaseManager.insertModel(clienteModel, clienteModel.id, PATH_FIREBASE)

                    if (flag) {
                        limpiarCampos()  // Limpiar los campos después de guardar
                        requireActivity().runOnUiThread {
                            DialogMaterialHelper.mostrarConfirmDialog(requireActivity(), "El cliente se guardó exitosamente.") { confirmed ->
                                if (confirmed) {
                                    ConfigLoading.hideLoadingAnimation()
                                    UtilFragment.changeFragment(requireContext(), ClienteFragment(), TAG)
                                }
                            }
                        }
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
    override fun onMediaSelected(uri: Uri?) {
        uri?.let {
            FirebaseStorageManager.URI_IMG_SELECTED = it
            binding.imgPerfil.setImageURI(it)
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
                textAppellidoPaterno.text!!.clear()
                textAppellidoMaterno.text!!.clear()
                textCorreo.text!!.clear()
                textTelefono.text!!.clear()
                textCalle.text!!.clear()
                textNumero.text!!.clear()
                textColonia.text!!.clear()
                textMunicipio.text!!.clear()
                textGoogleMaps.text!!.clear()
                textKilometrosCasa.text!!.clear()
                imgPerfil.setImageResource(R.drawable.ic_imagen)  // Restablecer la imagen predeterminada
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
                    UtilFragment.changeFragment(requireContext(), ClienteFragment(), TAG)
                }
            })
    }
}