package com.example.katzen.Fragment.Viajes

import android.R
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Config.Config
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.Fragment.Paciente.PacienteFragment
import com.example.katzen.Fragment.Paciente.SeleccionarPacienteFragment
import com.example.katzen.Helper.DialogHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper.Companion.hideKeyboard
import com.example.katzen.MainActivity
import com.example.katzen.Model.ClienteModel
import com.example.katzen.Model.VentaMesDetalleModel
import com.example.katzen.databinding.AddViajeFragmentBinding
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class AddViajeFragment : Fragment() {
    val TAG = "AddMascotaFragment"
    val PATH_FIREBASE = "Katzen/Gasolina"


    private var _binding: AddViajeFragmentBinding? = null
    private val binding get() = _binding!!
    private val datePicker =
        MaterialDatePicker.Builder.datePicker()
            .setTitleText("Selecciona Fecha")
            .build()
    companion object{
        val PICK_IMAGE_REQUEST = 1
        var ADD_VIAJE : VentaMesDetalleModel = VentaMesDetalleModel()
        var ADD_CLIENTE_VIAJE : ClienteModel = ClienteModel()
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
        binding.textFechaDetalle.setOnClickListener {
            it.hideKeyboard()
            datePicker.show((activity as MainActivity).supportFragmentManager, DialogHelper.TAG);
        }
        binding.textFechaDetalle.setOnFocusChangeListener { view, b ->
            if(b){
                view.hideKeyboard()
                datePicker.show((activity as MainActivity).supportFragmentManager, DialogHelper.TAG);
            }
        }

        datePicker.addOnPositiveButtonClickListener {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.time = Date(it)
            var fecha = "${calendar.get(Calendar.DAY_OF_MONTH)}-" +
                    "${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.YEAR)}"
            binding.textFechaDetalle.text =  Editable.Factory.getInstance().newEditable(fecha)
        }

    }
    fun init(){
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_list_item_1, Config.CATEGORIAS)
        binding.textCategoria.setAdapter(adapter)

       // UpperCaseTextWatcher.UpperText(binding.textColor)

    }
    fun initLoading(){
        ConfigLoading.LOTTIE_ANIMATION_VIEW = binding.lottieAnimationView
        ConfigLoading.CONT_ADD_PRODUCTO = binding.contAddViaje
        ConfigLoading.FRAGMENT_NO_DATA = binding.fragmentNoData.contNoData
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

    fun setValues() {
        val fullName = "${ADD_CLIENTE_VIAJE.nombre} ${ADD_CLIENTE_VIAJE.apellidoPaterno} ${ADD_CLIENTE_VIAJE.apellidoMaterno}"
        binding.textCliente.text = Editable.Factory.getInstance().newEditable(fullName)
        val domicilio = "${ADD_CLIENTE_VIAJE.calle} #${ADD_CLIENTE_VIAJE.numero}, ${ADD_CLIENTE_VIAJE.colonia} ${ADD_CLIENTE_VIAJE.municipio}"
        if(ADD_CLIENTE_VIAJE.calle != ""){
            binding.textDomicilio.text = Editable.Factory.getInstance().newEditable(domicilio)
        }
        binding.textKilometros.text =  Editable.Factory.getInstance().newEditable(ADD_CLIENTE_VIAJE.kilometrosCasa)
        binding.textLinkMaps.text = Editable.Factory.getInstance().newEditable(ADD_CLIENTE_VIAJE.urlGoogleMaps)
        binding.textFechaDetalle.text =  Editable.Factory.getInstance().newEditable(DialogHelper.getDateNow())
    }
    fun setPacienteModel(){
        ADD_VIAJE.nombreDomicilio = binding.nombreDomicilio.text.toString()
        ADD_VIAJE.categoria = binding.textCategoria.text.toString()
        ADD_VIAJE.domicilio = binding.textDomicilio.text.toString()
        ADD_VIAJE.kilometros = binding.textKilometros.text.toString()
        ADD_VIAJE.linkMaps = binding.textLinkMaps.text.toString()
        ADD_VIAJE.fecha = binding.textFechaDetalle.text.toString()

        if (ADD_CLIENTE_VIAJE.id != ""){
            ADD_VIAJE.nombreCliente = binding.textCliente.text.toString()
            ADD_VIAJE.idCliente = ADD_CLIENTE_VIAJE.id
        }

    }

    override fun onResume() {
        super.onResume()
        setPacienteModel()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    UtilFragment.changeFragment(requireContext(), ViajesDetalleV2Fragment(), TAG)
                }
            })
    }
}
