package com.example.katzen.Fragment.Campaña

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseCampañaUtil
import com.example.katzen.Fragment.Campaña.CampañaFragment.Companion.ADD_CAMPAÑA
import com.example.katzen.Fragment.Seleccionadores.SeleccionarClienteFragment
import com.example.katzen.Fragment.Seleccionadores.SeleccionarPacienteClienteFragment
import com.example.katzen.Helper.CalendarioUtil
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Helper.UtilHelper.Companion.hideKeyboard
import com.ninodev.katzen.databinding.VistaAgregarPacienteCampaniaBinding
import kotlinx.coroutines.launch

class AddPacienteCampañaFragment : Fragment() {
    val TAG = "AddPacienteCampañaFragment"
    val FOLDER_NAME = "Mascotas"
    val PATH_FIREBASE = "Katzen/Mascota"



    private var _binding: VistaAgregarPacienteCampaniaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = VistaAgregarPacienteCampaniaBinding.inflate(inflater, container, false)
        val view = binding.root

        requireActivity().title = "Añadir Paciente a Campaña"

        init()
        initLoading()
        initListeners()
        setValues()
        view.setOnClickListener { it.hideKeyboard() }

        return view
    }

    fun init(){
        binding.editTextFecha2.setText(CalendarioUtil.obtenerFechaHoraActual())
    }

    fun initLoading() {
        ConfigLoading.init(
            binding.lottieAnimationView,
            binding.contAddProducto,
            binding.fragmentNoData.contNoData
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    fun guardarPacienteCampaña(){
        ADD_CAMPAÑA.fecha = binding.editTextFecha2.text.toString()

        // Call agregarPacienteCampaña
        lifecycleScope.launch {
            val result = FirebaseCampañaUtil.agregarPacienteCampaña(ADD_CAMPAÑA)
            result.fold(
                onSuccess = {
                    // Handle success, e.g., navigate back to the campaign list
                    UtilFragment.changeFragment(requireContext(), CampañaPacienteFragment(), TAG)
                },
                onFailure = {
                    // Handle failure, e.g., show an error message
                    DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error al agregar el paciente a la campaña: ${it.message}")
                    println("Error al agregar el paciente a la campaña: ${it.message}")
                }
            )
        }
    }
    fun initListeners(){
        binding.btnGuardar.setOnClickListener {
            if (validarCampos()) {
                guardarPacienteCampaña()
            }
        }
        binding.btnCancelar.setOnClickListener {
            UtilFragment.changeFragment(requireContext(), CampañaPacienteFragment(), TAG)
        }
        binding.textCliente.setOnClickListener {
            it.hideKeyboard()
            setCampañaModel()
            UtilFragment.changeFragment(requireContext() ,
                SeleccionarClienteFragment("ADD_CAMPAÑA") ,TAG)
        }
        binding.textCliente.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.hideKeyboard()
                setCampañaModel()
                UtilFragment.changeFragment(requireContext(), SeleccionarClienteFragment("ADD_CAMPAÑA"), TAG)
            }
        }

        binding.textPaciente.setOnClickListener {
            it.hideKeyboard()
            setCampañaModel()
            UtilFragment.changeFragment(requireContext() ,
                SeleccionarPacienteClienteFragment("ADD_CAMPAÑA") ,TAG)
        }
        binding.textPaciente.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.hideKeyboard()
                setCampañaModel()
                UtilFragment.changeFragment(requireContext(), SeleccionarPacienteClienteFragment("ADD_CAMPAÑA"), TAG)
            }
        }
        binding.editTextFecha2.setOnClickListener {
            it.hideKeyboard()
            CalendarioUtil.mostrarCalendarioFechaCampaña(requireContext(), binding.editTextFecha)
        }
        binding.editTextFecha2.setOnFocusChangeListener { view, isFocus ->
            UtilHelper.hideKeyBoardWorld(requireActivity(), view)
            if (isFocus) {
                view.hideKeyboard()
                CalendarioUtil.mostrarCalendarioFechaCampaña(requireContext(), binding.editTextFecha)

            }
        }
    }

    fun setCampañaModel(){
        ADD_CAMPAÑA.nombreCliente = binding.textCliente.text.toString()
        ADD_CAMPAÑA.nombrePaciente = binding.textPaciente.text.toString()
        binding.editTextFecha2.setText(ADD_CAMPAÑA.fecha)
    }
    fun setValues(){
        binding.textCliente.setText(ADD_CAMPAÑA.nombreCliente)
        binding.textPaciente.setText(ADD_CAMPAÑA.nombrePaciente)
        binding.editTextFecha2.setText(ADD_CAMPAÑA.fecha)
    }
    fun validarCampos(): Boolean {
        val nombreCliente = binding.textCliente.text.toString().trim()
        val nombrePaciente = binding.textPaciente.text.toString().trim()
        val fecha = binding.editTextFecha2.text.toString().trim()

        if (nombreCliente.isEmpty()) {
            DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Por favor, selecciona un cliente.")
            return false
        }

        if (nombrePaciente.isEmpty()) {
            DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Por favor, selecciona un paciente.")
            return false
        }

        if (fecha.isEmpty()) {
            DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Por favor, selecciona una fecha.")
            return false
        }

        return true
    }
    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    UtilFragment.changeFragment(requireContext(), CampañaPacienteFragment(), TAG)
                }
            })
    }
}
