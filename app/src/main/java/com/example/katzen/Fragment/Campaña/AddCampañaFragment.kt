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
import com.example.katzen.Helper.CalendarioUtil
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Helper.UtilHelper.Companion.hideKeyboard
import com.ninodev.katzen.databinding.VistaAgregarCampaniaBinding
import kotlinx.coroutines.launch

class AddCampañaFragment: Fragment() {
    val TAG = "AddPacienteCampañaFragment"
    val FOLDER_NAME = "Mascotas"
    val PATH_FIREBASE = "Katzen/Mascota"

    private var _binding: VistaAgregarCampaniaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = VistaAgregarCampaniaBinding.inflate(inflater, container, false)
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
        ADD_CAMPAÑA.fecha = CalendarioUtil.obtenerFechaHoraActualCampaña()
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

    fun initListeners(){
        binding.btnGuardar.setOnClickListener {
            setCampañaModel()
            guardarCampaña()
        }
        binding.btnCancelar.setOnClickListener {
            UtilFragment.changeFragment(requireContext(), CampañaEventoFragment(), TAG)
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
        ADD_CAMPAÑA.nombreCampaña = binding.textNombre.text.toString()
        ADD_CAMPAÑA.nombrePaciente = binding.editTextFecha2.text.toString()
        binding.editTextFecha2.setText(ADD_CAMPAÑA.fecha)
    }

    fun setValues(){
        binding.editTextFecha2.setText(ADD_CAMPAÑA.fecha)
    }

    fun limpiar(){
        binding.textNombre.setText("")
        binding.editTextFecha2.setText("")
    }
    fun guardarCampaña() {
        limpiar()
        lifecycleScope.launch {
            val result = FirebaseCampañaUtil.agregarCampaña(ADD_CAMPAÑA)
            if (result.isSuccess) {
                // Show success message and navigate back
                UtilFragment.changeFragment(requireContext(), CampañaEventoFragment(), TAG)
            } else {
                // Handle error (e.g., show a toast message)
                println("Error al agregar la campaña: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    UtilFragment.changeFragment(requireContext(), CampañaEventoFragment(), TAG)
                }
            })
    }
}
