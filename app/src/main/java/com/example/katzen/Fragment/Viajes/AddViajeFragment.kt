package com.example.katzen.Fragment.Viajes

import android.R
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.katzen.Config.Config
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseViajesUtil
import com.example.katzen.Fragment.Paciente.SeleccionarPacienteFragment
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.GasHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Helper.UtilHelper.Companion.hideKeyboard
import com.example.katzen.MainActivity
import com.example.katzen.Model.ClienteModel
import com.example.katzen.Model.VentaMesDetalleModel
import com.example.katzen.databinding.AddViajeFragmentBinding
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        var EDIT_VIAJE : VentaMesDetalleModel = VentaMesDetalleModel()
        var ADD_CLIENTE_VIAJE : ClienteModel = ClienteModel()
        var FLAG_FECHA = false
        var FECHA_ANTIGUA = ""
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
        setValuesEdit()
        view.setOnClickListener { it.hideKeyboard() }

        return view
    }

    fun setValuesEdit(){
        if(EDIT_VIAJE.nombreDomicilio != ""){
            binding.btnEditar.visibility = View.VISIBLE
            binding.btnGuardar.visibility = View.GONE


            binding.textCliente.text = Editable.Factory.getInstance().newEditable(EDIT_VIAJE.nombreCliente)
            binding.textDomicilio.text = Editable.Factory.getInstance().newEditable(EDIT_VIAJE.domicilio)
            binding.nombreDomicilio.text =  Editable.Factory.getInstance().newEditable(EDIT_VIAJE.nombreDomicilio)
            binding.textKilometros.text =  Editable.Factory.getInstance().newEditable(EDIT_VIAJE.kilometros)
            binding.textLinkMaps.text = Editable.Factory.getInstance().newEditable(EDIT_VIAJE.linkMaps)
            binding.textFechaDetalle.text =  Editable.Factory.getInstance().newEditable(EDIT_VIAJE.fecha)
            binding.textCategoria.text =  Editable.Factory.getInstance().newEditable(EDIT_VIAJE.categoria)
        }else{
            binding.btnEditar.visibility = View.GONE
            binding.btnGuardar.visibility = View.VISIBLE
        }
    }
    fun listeners(){
        binding.textCategoria.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // Acción a ejecutar cuando se abre el spinner
                binding.textCategoria.text.clear()
            }
            false
        }

        binding.btnCancelar.setOnClickListener {
            it.hideKeyboard()
            UtilFragment.changeFragment(requireContext() , ViajesDetalleFragment() ,TAG)
        }
        binding.btnGuardar.setOnClickListener {
            it.hideKeyboard()
            setViajeModel()
            guardarViaje(ADD_VIAJE)
        }
        binding.textCliente.setOnClickListener {
            it.hideKeyboard()
            setViajeModel()
            UtilFragment.changeFragment(requireContext() ,SeleccionarPacienteFragment("ADD_VIAJE") ,TAG)
        }
        binding.textCliente.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.hideKeyboard()
                setViajeModel()
                UtilFragment.changeFragment(requireContext(), SeleccionarPacienteFragment("ADD_VIAJE"), TAG)
            }
        }
        binding.textFechaDetalle.setOnClickListener {
            it.hideKeyboard()
            datePicker.show((activity as MainActivity).supportFragmentManager, TAG);
        }
        binding.textFechaDetalle.setOnFocusChangeListener { view, b ->
            if(b){
                view.hideKeyboard()
                datePicker.show((activity as MainActivity).supportFragmentManager, TAG);
            }
        }

        binding.btnEditar.setOnClickListener {
            it.hideKeyboard()
            setViajeEditModel()
            guardarViaje(EDIT_VIAJE)
        }

        datePicker.addOnPositiveButtonClickListener {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.time = Date(it)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val month = String.format("%02d", calendar.get(Calendar.MONTH) + 1)
            val year = calendar.get(Calendar.YEAR)
            val fecha = "$dayOfMonth-$month-$year"
            binding.textFechaDetalle.text = Editable.Factory.getInstance().newEditable(fecha)
        }


    }
    fun init(){
        FLAG_FECHA = false
        FECHA_ANTIGUA = ""

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
        requireActivity().runOnUiThread {
            binding.apply {
                nombreDomicilio.text?.clear()
                textCliente.text?.clear()
                textCategoria.text?.clear()
                textDomicilio.text?.clear()
                textKilometros.text?.clear()
                textLinkMaps.text?.clear()
                textFechaDetalle.text = Editable.Factory.getInstance().newEditable(UtilHelper.getDateNow())
            }
        }
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
        binding.nombreDomicilio.text =  Editable.Factory.getInstance().newEditable(ADD_VIAJE.nombreDomicilio)
        binding.textKilometros.text =  Editable.Factory.getInstance().newEditable(ADD_CLIENTE_VIAJE.kilometrosCasa)
        binding.textLinkMaps.text = Editable.Factory.getInstance().newEditable(ADD_CLIENTE_VIAJE.urlGoogleMaps)
        binding.textFechaDetalle.text =  Editable.Factory.getInstance().newEditable(UtilHelper.getDateNow())
    }
    fun setViajeEditModel(){
        Log.e("asknfkjasnfk", EDIT_VIAJE.fecha)
        Log.e("asknfkjasnfk", binding.textFechaDetalle.text.toString())
        if(EDIT_VIAJE.fecha != binding.textFechaDetalle.text.toString()){
            FLAG_FECHA = true
            FECHA_ANTIGUA = EDIT_VIAJE.fecha
        }


        EDIT_VIAJE.nombreDomicilio = binding.nombreDomicilio.text.toString()
        EDIT_VIAJE.categoria = binding.textCategoria.text.toString()
        EDIT_VIAJE.domicilio = binding.textDomicilio.text.toString()
        EDIT_VIAJE.kilometros = binding.textKilometros.text.toString()
        EDIT_VIAJE.linkMaps = binding.textLinkMaps.text.toString()
        EDIT_VIAJE.fecha = binding.textFechaDetalle.text.toString()


        if (EDIT_VIAJE.id != ""){
            EDIT_VIAJE.nombreCliente = binding.textCliente.text.toString()
            EDIT_VIAJE.idCliente = ADD_CLIENTE_VIAJE.id
        }

        if(EDIT_VIAJE.kilometros != "" && EDIT_VIAJE.categoria != ""){
            val (costo,ganancia,venta) =  GasHelper.calcular(EDIT_VIAJE.kilometros.toDouble(), EDIT_VIAJE.categoria)

            EDIT_VIAJE.costo = costo
            EDIT_VIAJE.ganancia = ganancia
            EDIT_VIAJE.venta = venta
        }

    }
    fun setViajeModel(){
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

        if(ADD_VIAJE.kilometros != "" && ADD_VIAJE.categoria != ""){
            val (costo,ganancia,venta) =  GasHelper.calcular(ADD_VIAJE.kilometros.toDouble(), ADD_VIAJE.categoria)

            ADD_VIAJE.costo = costo
            ADD_VIAJE.ganancia = ganancia
            ADD_VIAJE.venta = venta
        }

    }
    fun guardarViaje(ventaMesDetalleModel: VentaMesDetalleModel) {
        viewLifecycleOwner.lifecycleScope.launch {
            val validationResult = VentaMesDetalleModel.validarViaje(requireContext(), ventaMesDetalleModel)
            if (validationResult.isValid) {

                val resultado = withContext(Dispatchers.IO) {
                    if (ventaMesDetalleModel.id.isNotEmpty()) {
                        // Si el ID del viaje no está vacío, significa que ya existe y queremos editar el cargo del viaje
                        FirebaseViajesUtil.editarCargoViaje(ventaMesDetalleModel)
                    } else {
                        limpiarCampos()
                        // Si el ID del viaje está vacío, significa que queremos guardar un nuevo cargo de viaje
                        FirebaseViajesUtil.guardarCargosViajes(ventaMesDetalleModel)
                    }
                }
                if (resultado.first) {
                    // Se insertó o editó el viaje correctamente
                    // Aquí puedes manejar el flujo de tu aplicación después de que se inserte o edite el viaje

                    ADD_VIAJE = VentaMesDetalleModel()
                    ADD_CLIENTE_VIAJE = ClienteModel()

                    DialogMaterialHelper.mostrarConfirmEditDialog(requireActivity(), resultado.second) {
                        backFragment()
                    }

                } else {
                    // Hubo un error al insertar o editar el viaje

                    if(FLAG_FECHA){
                        eliminarViaje(FECHA_ANTIGUA, EDIT_VIAJE.id)
                    }else{
                        DialogMaterialHelper.mostrarConfirmEditDialog(requireActivity(), resultado.second) {
                            backFragment()
                        }
                    }

                    // Aquí puedes manejar el error, por ejemplo, mostrar un mensaje de error al usuario
                }
            } else {
                // Mostrar mensaje de error de validación
                DialogMaterialHelper.mostrarErrorDialog(requireActivity(), validationResult.message)
            }
        }
    }
    fun eliminarViaje(fecha: String, idViaje: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val resultado = withContext(Dispatchers.IO) {
                FirebaseViajesUtil.eliminarCargoViaje(fecha, idViaje)
            }
            DialogMaterialHelper.mostrarConfirmEditDialog(requireActivity(), resultado.second) {
                backFragment()
            }
        }
    }


    fun backFragment(){
        UtilFragment.changeFragment(requireContext(), ViajesDetalleFragment(), TAG)
    }
    override fun onResume() {
        super.onResume()
        setViajeModel()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backFragment()
                }
            })
    }
}
