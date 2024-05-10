package com.example.katzen.Fragment.Viajes

import PacienteModel
import android.R
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
import com.example.katzen.Fragment.Paciente.PacienteFragment
import com.example.katzen.Fragment.Paciente.SeleccionarPacienteFragment
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UpperCaseTextWatcher
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Helper.UtilHelper.Companion.hideKeyboard
import com.example.katzen.Model.VentaMesDetalleModel
import com.example.katzen.databinding.AddViajeFragmentBinding
import com.example.katzen.databinding.VistaAgregarMascotaBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddViajeFragment : Fragment() {
    val TAG = "AddMascotaFragment"
    val FOLDER_NAME = "Mascotas"
    val PATH_FIREBASE = "Katzen/Mascota"


    private var _binding: AddViajeFragmentBinding? = null
    private val binding get() = _binding!!
    companion object{
        val PICK_IMAGE_REQUEST = 1
        var ADD_VIAJE : VentaMesDetalleModel = VentaMesDetalleModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = AddViajeFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        requireActivity().title = "Añadir Viaje"

        initLoading()
        init()
        listeners()
        setValues()
        view.setOnClickListener { it.hideKeyboard() }

        return view
    }

    fun listeners(){

        binding.btnCancelar.setOnClickListener {
            it.hideKeyboard()
            UtilFragment.changeFragment(requireContext() , PacienteFragment() ,TAG)
        }
        binding.btnGuardar.setOnClickListener {
            it.hideKeyboard()
            //guardarMascota()
        }
        binding.textCliente.setOnClickListener {
            it.hideKeyboard()
            setPacienteModel()
            UtilFragment.changeFragment(requireContext() ,SeleccionarPacienteFragment("ADD_VIAJE") ,TAG)
        }
        binding.textCliente.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.hideKeyboard()
                setPacienteModel()
                UtilFragment.changeFragment(requireContext(), SeleccionarPacienteFragment("ADD_VIAJE"), TAG)
            }
        }
    }
    fun init(){

       // UpperCaseTextWatcher.UpperText(binding.textColor)

    }
    fun initLoading(){
        //ConfigLoading.LOTTIE_ANIMATION_VIEW = binding.lottieAnimationView
        //ConfigLoading.CONT_ADD_PRODUCTO = binding.contAddProducto
        //ConfigLoading.FRAGMENT_NO_DATA = binding.fragmentNoData.contNoData
    }
    fun limpiarCampos() {
        /*requireActivity().runOnUiThread {
            binding.apply {
                textNombre.text?.clear()
                spSexo.text?.clear()
            }
        }*/
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    fun setValues(){
       // binding.textNombre.setText(ADD_PACIENTE.nombre)
    }
    fun setPacienteModel(){
        //ADD_PACIENTE.nombre = binding.textNombre.text.toString()
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
