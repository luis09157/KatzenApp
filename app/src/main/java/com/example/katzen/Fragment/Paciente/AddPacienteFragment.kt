package com.example.katzen.Fragment.Paciente

import PacienteModel
import android.app.Activity
import android.content.Intent
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
import com.example.katzen.DataBaseFirebase.FirebaseDatabaseManager
import com.example.katzen.DataBaseFirebase.FirebaseStorageManager
import com.example.katzen.Fragment.Seleccionadores.SeleccionarClienteFragment
import com.example.katzen.Helper.CalendarioUtil
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UpperCaseTextWatcher
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Helper.UtilHelper.Companion.hideKeyboard
import com.example.katzen.R
import com.example.katzen.databinding.VistaAgregarMascotaBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddPacienteFragment : Fragment() {
    val TAG = "AddMascotaFragment"
    val FOLDER_NAME = "Mascotas"
    val PATH_FIREBASE = "Katzen/Mascota"


    private var _binding: VistaAgregarMascotaBinding? = null
    private val binding get() = _binding!!
    companion object{
        val PICK_IMAGE_REQUEST = 1
        var ADD_PACIENTE : PacienteModel = PacienteModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = VistaAgregarMascotaBinding.inflate(inflater, container, false)
        val view = binding.root

        requireActivity().title = "Añadir Paciente"

        initLoading()
        init()
        listeners()
        setValues()
        view.setOnClickListener { it.hideKeyboard() }

        return view
    }

    fun listeners(){
        binding.spSexo.setOnClickListener { it.hideKeyboard() }
        binding.spEspecie.setOnClickListener { it.hideKeyboard() }

        binding.btnCancelar.setOnClickListener {
            it.hideKeyboard()
            UtilFragment.changeFragment(requireContext() ,PacienteFragment() ,TAG)
        }
        binding.btnGuardar.setOnClickListener {
            it.hideKeyboard()
            guardarMascota()
        }
        binding.btnSubirImagen.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
        binding.textCliente.setOnClickListener {
            it.hideKeyboard()
            setPacienteModel()
            UtilFragment.changeFragment(requireContext() ,
                SeleccionarClienteFragment("ADD_PACIENTE") ,TAG)
        }
        binding.textCliente.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.hideKeyboard()
                setPacienteModel()
                UtilFragment.changeFragment(requireContext(), SeleccionarClienteFragment("ADD_PACIENTE"), TAG)
            }
        }
        binding.editTextFecha2.setOnClickListener {
            it.hideKeyboard()
            CalendarioUtil.mostrarCalendarioFecha(requireContext(), binding.editTextFecha)
        }
        binding.editTextFecha2.setOnFocusChangeListener { view, isFocus ->
            UtilHelper.hideKeyBoardWorld(requireActivity(), view)
            if (isFocus) {
                view.hideKeyboard()
                CalendarioUtil.mostrarCalendarioFecha(requireContext(), binding.editTextFecha)

            }
        }
    }
    fun init(){
        val adapterSEXO = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, Config.SEXO)
        binding.spSexo.setAdapter(adapterSEXO)
        val adapterESPECIE = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, Config.ESPECIE)
        binding.spEspecie.setAdapter(adapterESPECIE)


        UpperCaseTextWatcher.UpperText(binding.spEspecie)
        //UpperCaseTextWatcher.UpperText(binding.spRaza)
        //UpperCaseTextWatcher.UpperText(binding.spSexo)

        /*UpperCaseTextWatcher.UpperText(binding.textColor)
        UpperCaseTextWatcher.UpperText(binding.textPeso)
        UpperCaseTextWatcher.UpperText(binding.textNombre)
        UpperCaseTextWatcher.UpperText(binding.textEdad)
        UpperCaseTextWatcher.UpperText(binding.textCliente)*/


    }
    fun initLoading(){
        ConfigLoading.LOTTIE_ANIMATION_VIEW = binding.lottieAnimationView
        ConfigLoading.CONT_ADD_PRODUCTO = binding.contAddProducto
        ConfigLoading.FRAGMENT_NO_DATA = binding.fragmentNoData.contNoData
    }
    fun guardarMascota(){

       setPacienteModel()

        // Validar la mascota
        val validationResult = PacienteModel.validarMascota(requireContext(), ADD_PACIENTE)

        if (validationResult.isValid) {

            GlobalScope.launch(Dispatchers.IO) {
                if (FirebaseStorageManager.hasSelectedImage()){
                    requireActivity().runOnUiThread {  ConfigLoading.showLoadingAnimation() }
                    val imageUrl = FirebaseStorageManager.uploadImage(FirebaseStorageManager.URI_IMG_SELECTED, FOLDER_NAME)
                    println("URL de descarga de la imagen: $imageUrl")
                    ADD_PACIENTE.imageUrl = imageUrl

                    GlobalScope.launch(Dispatchers.IO) {
                        val ( flag,message ) =  FirebaseDatabaseManager.insertModel(ADD_PACIENTE, ADD_PACIENTE.id,PATH_FIREBASE)

                        if (flag) {
                            limpiarCampos()
                            requireActivity().runOnUiThread {
                                ConfigLoading.hideLoadingAnimation()
                                DialogMaterialHelper.mostrarSuccessDialog(requireActivity(), "El paciente se guardó exitosamente.")
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
                        val ( flag,message ) =  FirebaseDatabaseManager.insertModel(ADD_PACIENTE, ADD_PACIENTE.id,PATH_FIREBASE)

                        if (flag) {
                            limpiarCampos()
                            requireActivity().runOnUiThread {
                                ConfigLoading.hideLoadingAnimation()
                                DialogMaterialHelper.mostrarSuccessDialog(requireActivity(), "El paciente se guardó exitosamente.")
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
        }else {
            // La mascota no es válida, mostrar mensaje de error
            println(validationResult.message)
            ConfigLoading.hideLoadingAnimation()
            DialogMaterialHelper.mostrarErrorDialog(requireActivity(), validationResult.message)
        }
    }
    fun limpiarCampos() {
        requireActivity().runOnUiThread {
            binding.apply {
                textNombre.text?.clear()
                spSexo.text?.clear()
                spEspecie.text?.clear()
                spRaza.text?.clear()
                textPeso.text?.clear()
                editTextFecha2.text?.clear()
                textColor.text?.clear()
                textCliente.text?.clear()
                fotoMascota.setImageResource(R.drawable.ic_imagen)
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            FirebaseStorageManager.URI_IMG_SELECTED = data.data!!
            // You can now upload this image to Firebase Storage and display it in the ImageView

            binding.fotoMascota.setImageURI(FirebaseStorageManager.URI_IMG_SELECTED)
        }
    }

    fun setValues(){
        binding.fotoMascota.setImageURI(FirebaseStorageManager.URI_IMG_SELECTED)
        binding.textNombre.setText(ADD_PACIENTE.nombre)
        binding.textPeso.setText(ADD_PACIENTE.peso)
        binding.spRaza.setText(ADD_PACIENTE.raza)
        binding.spEspecie.setText(ADD_PACIENTE.especie)
        binding.editTextFecha2.setText(ADD_PACIENTE.edad)
        binding.textColor.setText(ADD_PACIENTE.color)
        binding.spSexo.setText(ADD_PACIENTE.sexo)
        binding.textCliente.setText(ADD_PACIENTE.nombreCliente)
    }
    fun setPacienteModel(){
        ADD_PACIENTE.nombre = binding.textNombre.text.toString()
        ADD_PACIENTE.peso = binding.textPeso.text.toString()
        ADD_PACIENTE.raza = binding.spRaza.text.toString()
        ADD_PACIENTE.especie = binding.spEspecie.text.toString()
        ADD_PACIENTE.edad = binding.editTextFecha2.text.toString()
        ADD_PACIENTE.color = binding.textColor.text.toString()
        ADD_PACIENTE.sexo = binding.spSexo.text.toString()
        ADD_PACIENTE.nombreCliente = binding.textCliente.text.toString()
        ADD_PACIENTE.fecha = UtilHelper.getDate()
    }

    override fun onResume() {
        super.onResume()
        setPacienteModel()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    UtilFragment.changeFragment(requireContext(), PacienteFragment(), TAG)
                }
            })
    }
}
