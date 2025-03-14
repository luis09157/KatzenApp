package com.example.katzen.Fragment.Producto

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseProcedimientoUtil
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.ProcedimientoModel
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.FragmentAddProcedimientoBinding
import java.text.DecimalFormat

class AddProcedimientoFragment : Fragment() {
    private var _binding: FragmentAddProcedimientoBinding? = null
    private val binding get() = _binding!!
    private val TAG = "AddProcedimientoFragment"
    private val df = DecimalFormat("#,##0.00")
    
    // Estado de los botones
    private var estadoActivo = true
    private var tipoProcedimiento = "Otro" // Valores: "Otro", "Castración", "Eutanasia"

    companion object {
        private const val ARG_PROCEDIMIENTO = "procedimiento"

        fun newInstance(procedimiento: ProcedimientoModel): AddProcedimientoFragment {
            val fragment = AddProcedimientoFragment()
            val args = Bundle()
            args.putParcelable(ARG_PROCEDIMIENTO, procedimiento)
            fragment.arguments = args
            return fragment
        }
    }

    private var procedimientoToEdit: ProcedimientoModel? = null
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().title = "Nuevo Procedimiento"
        procedimientoToEdit = arguments?.getParcelable(ARG_PROCEDIMIENTO)
        isEditMode = procedimientoToEdit != null
        
        if (isEditMode) {
            requireActivity().title = "Editar Procedimiento"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProcedimientoBinding.inflate(inflater, container, false)
        initLoading()
        setupCalculations()
        setupSpinner()
        setupListeners()
        
        if (isEditMode) {
            cargarDatosProcedimiento()
        } else {
            // Valores por defecto
            binding.etIva.setText("16")
            actualizarEstadoBotones(true)
        }
        
        return binding.root
    }

    private fun initLoading() {
        ConfigLoading.init(
            binding.lottieAnimationView,
            binding.contAddProducto,
            binding.fragmentNoData.contNoData
        )
    }

    private fun setupCalculations() {
        // Calcular precio final cuando cambie el precio unitario o el IVA
        val precioWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calcularPrecioFinal()
            }
        }

        binding.etPrecioUnitario.addTextChangedListener(precioWatcher)
        binding.etIva.addTextChangedListener(precioWatcher)
    }

    private fun setupSpinner() {
        val tiposProcedimiento = arrayOf("Otro", "Castración", "Eutanasia")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, tiposProcedimiento)
        binding.spinnerTipo.setAdapter(adapter)
        
        // Establecer valor por defecto
        binding.spinnerTipo.setText("Otro", false)
        
        binding.spinnerTipo.setOnItemClickListener { _, _, position, _ ->
            tipoProcedimiento = tiposProcedimiento[position]
        }
    }

    private fun calcularPrecioFinal() {
        try {
            val precioUnitario = binding.etPrecioUnitario.text.toString().toDoubleOrNull() ?: 0.0
            val porcentajeIva = binding.etIva.text.toString().toDoubleOrNull() ?: 0.0
            
            val iva = precioUnitario * (porcentajeIva / 100)
            val precioFinal = precioUnitario + iva
            
            binding.etPrecioFinal.setText("$" + df.format(precioFinal))
        } catch (e: Exception) {
            Log.e(TAG, "Error al calcular precio final: ${e.message}")
            binding.etPrecioFinal.setText("$0.00")
        }
    }

    private fun actualizarEstadoBotones(activo: Boolean) {
        estadoActivo = activo
        
        if (activo) {
            binding.btnActivo.setBackgroundTintList(resources.getColorStateList(R.color.purple_500, null))
            binding.btnActivo.setTextColor(resources.getColor(android.R.color.white, null))
            binding.btnInactivo.setBackgroundTintList(resources.getColorStateList(R.color.grey_400, null))
            binding.btnInactivo.setTextColor(resources.getColor(android.R.color.white, null))
        } else {
            binding.btnActivo.setBackgroundTintList(resources.getColorStateList(R.color.grey_400, null))
            binding.btnActivo.setTextColor(resources.getColor(android.R.color.white, null))
            binding.btnInactivo.setBackgroundTintList(resources.getColorStateList(R.color.purple_500, null))
            binding.btnInactivo.setTextColor(resources.getColor(android.R.color.white, null))
        }
    }

    private fun cargarDatosProcedimiento() {
        procedimientoToEdit?.let {
            binding.etNombre.setText(it.nombre)
            binding.etCodigoInterno.setText(it.codigoInterno)
            binding.etInstrucciones.setText(it.instrucciones)
            binding.etPrecioUnitario.setText(it.precioUnitario)
            binding.etIva.setText(it.iva)
            
            // Establecer estado y tipo
            actualizarEstadoBotones(it.activo)
            binding.spinnerTipo.setText(it.tipo, false)
            tipoProcedimiento = it.tipo
            
            // Calcular precio final
            calcularPrecioFinal()
        }
    }

    private fun setupListeners() {
        binding.btnGuardar.setOnClickListener {
            onClickGuardar()
        }
        
        // Listeners para botones de estado
        binding.btnActivo.setOnClickListener {
            actualizarEstadoBotones(true)
        }
        
        binding.btnInactivo.setOnClickListener {
            actualizarEstadoBotones(false)
        }
    }

    private fun onClickGuardar() {
        ConfigLoading.showLoadingAnimation()

        // Obtener todos los valores
        val nombre = binding.etNombre.text.toString().trim()
        val codigoInterno = binding.etCodigoInterno.text.toString().trim()
        val instrucciones = binding.etInstrucciones.text.toString().trim()
        val precioUnitario = binding.etPrecioUnitario.text.toString()
        val iva = binding.etIva.text.toString()
        val precioFinal = binding.etPrecioFinal.text.toString().replace("$", "")
        val activo = estadoActivo
        val tipo = tipoProcedimiento

        if (nombre.isEmpty() || precioUnitario.isEmpty()) {
            ConfigLoading.hideLoadingAnimation()
            DialogMaterialHelper.mostrarErrorDialog(
                requireActivity(),
                "El nombre y precio unitario son obligatorios"
            )
            return
        }

        try {
            // En modo edición, actualizar el objeto existente para conservar el ID
            val procedimiento = if (isEditMode && procedimientoToEdit != null) {
                procedimientoToEdit!!.apply {
                    this.nombre = nombre
                    this.codigoInterno = codigoInterno
                    this.instrucciones = instrucciones
                    this.precioUnitario = precioUnitario
                    this.iva = iva
                    this.precioFinal = precioFinal
                    this.activo = activo
                    this.tipo = tipo
                }
            } else {
                ProcedimientoModel(
                    nombre = nombre,
                    codigoInterno = codigoInterno,
                    instrucciones = instrucciones,
                    precioUnitario = precioUnitario,
                    iva = iva,
                    precioFinal = precioFinal,
                    activo = activo,
                    tipo = tipo,
                    fechaRegistro = UtilHelper.getDate()
                )
            }

            Log.d(TAG, "Iniciando guardado de procedimiento: ${procedimiento.nombre}")

            if (isEditMode) {
                FirebaseProcedimientoUtil.actualizarProcedimiento(procedimiento) { success, message ->
                    Log.d(TAG, "Callback de actualización recibido: $success, $message")
                    ConfigLoading.hideLoadingAnimation()
                    if (success) {
                        DialogMaterialHelper.mostrarSuccessClickDialog(
                            requireActivity(),
                            "Procedimiento actualizado correctamente"
                        ) {
                            UtilFragment.changeFragment(requireContext(), ListaProcedimientosFragment(), TAG)
                        }
                    } else {
                        DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error: $message")
                    }
                }
            } else {
                Log.d(TAG, "Enviando solicitud para agregar procedimiento")
                FirebaseProcedimientoUtil.agregarProcedimiento(procedimiento) { success, message ->
                    Log.d(TAG, "Callback de agregar recibido: $success, $message")
                    ConfigLoading.hideLoadingAnimation()
                    if (success) {
                        DialogMaterialHelper.mostrarSuccessClickDialog(
                            requireActivity(),
                            "Procedimiento guardado correctamente"
                        ) {
                            UtilFragment.changeFragment(requireContext(), ListaProcedimientosFragment(), TAG)
                        }
                    } else {
                        DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error: $message")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar procedimiento: ${e.message}", e)
            ConfigLoading.hideLoadingAnimation()
            DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    UtilFragment.changeFragment(requireContext(), ListaProcedimientosFragment(), TAG)
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 