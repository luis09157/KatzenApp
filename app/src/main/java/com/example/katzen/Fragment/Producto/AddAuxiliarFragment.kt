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
import com.example.katzen.DataBaseFirebase.FirebaseAuxiliarUtil
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.AuxiliarModel
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.FragmentAddAuxiliarBinding
import java.text.DecimalFormat

class AddAuxiliarFragment : Fragment() {
    private var _binding: FragmentAddAuxiliarBinding? = null
    private val binding get() = _binding!!
    private val TAG = "AddAuxiliarFragment"

    companion object {
        private const val ARG_AUXILIAR = "auxiliar"

        fun newInstance(auxiliar: AuxiliarModel): AddAuxiliarFragment {
            val fragment = AddAuxiliarFragment()
            val args = Bundle()
            args.putParcelable(ARG_AUXILIAR, auxiliar)
            fragment.arguments = args
            return fragment
        }
    }

    private var auxiliarToEdit: AuxiliarModel? = null
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().title = getString(R.string.submenu_productos_m_complementarios)
        auxiliarToEdit = arguments?.getParcelable(ARG_AUXILIAR)
        isEditMode = auxiliarToEdit != null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddAuxiliarBinding.inflate(inflater, container, false)
        initLoading()
        setupSpinners()
        setupCalculations()
        setupListeners()
        
        if (isEditMode) {
            cargarDatosAuxiliar()
        }
        
        return binding.root
    }

    private fun initLoading() {
        ConfigLoading.init(
            binding.lottieAnimationView,
            binding.contAddAuxiliar,
            binding.fragmentNoData.contNoData
        )
    }

    private fun setupSpinners() {
        // Configurar spinner de Tipo
        val tiposArray = arrayOf("Laboratorio", "Radiografía", "Otro")
        val tipoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, tiposArray)
        binding.spinnerTipo.setAdapter(tipoAdapter)

        // Configurar spinner de Tipo de muestra
        val tiposMuestraArray = arrayOf("Sangre", "Orina", "Heces", "Tejido", "Otro")
        val tipoMuestraAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, tiposMuestraArray)
        binding.spinnerTipoMuestra.setAdapter(tipoMuestraAdapter)
    }

    private fun setupCalculations() {
        // Calcular precio final cuando cambie el precio unitario, margen de ganancia o el IVA
        val precioWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calcularPrecioFinal()
            }
        }

        binding.etPrecioUnitario.addTextChangedListener(precioWatcher)
        binding.etMargenGanancia.addTextChangedListener(precioWatcher)
        binding.etPorcentajeIva.addTextChangedListener(precioWatcher)
    }

    private fun calcularPrecioFinal() {
        try {
            val precioUnitario = binding.etPrecioUnitario.text.toString().toDoubleOrNull() ?: 0.0
            val margenGanancia = binding.etMargenGanancia.text.toString().toDoubleOrNull() ?: 0.0
            val porcentajeIva = binding.etPorcentajeIva.text.toString().toDoubleOrNull() ?: 0.0
            
            val montoMargen = precioUnitario * (margenGanancia / 100)
            val precioConMargen = precioUnitario + montoMargen
            val iva = precioConMargen * (porcentajeIva / 100)
            val precioFinal = precioConMargen + iva
            
            val df = DecimalFormat("#,##0.00")
            binding.etPrecioFinal.setText(df.format(precioFinal))
        } catch (e: Exception) {
            Log.e(TAG, "Error al calcular precio final: ${e.message}")
            binding.etPrecioFinal.setText("0.00")
        }
    }

    private fun cargarDatosAuxiliar() {
        auxiliarToEdit?.let {
            binding.etNombre.setText(it.nombre)
            binding.etCodigoInterno.setText(it.codigoInterno)
            binding.spinnerTipo.setText(it.tipo)
            binding.etLaboratorio.setText(it.laboratorio)
            binding.spinnerTipoMuestra.setText(it.tipoMuestra)
            binding.etMetodoAnalisis.setText(it.metodoAnalisis)
            binding.etInstrucciones.setText(it.instrucciones)
            binding.etCostoCompra.setText(it.costoCompra)
            binding.etPrecioUnitario.setText(it.precioUnitario)
            binding.etMargenGanancia.setText(it.margenGanancia)
            binding.etPorcentajeIva.setText(it.porcentajeIva)
            binding.etPrecioFinal.setText(it.precioFinal)
            
            if (it.activo) {
                binding.rbActivo.isChecked = true
            } else {
                binding.rbInactivo.isChecked = true
            }
        }
    }

    private fun setupListeners() {
        binding.btnGuardar.setOnClickListener {
            onClickGuardar()
        }
    }

    private fun onClickGuardar() {
        ConfigLoading.showLoadingAnimation()

        // Obtener todos los valores
        val nombre = binding.etNombre.text.toString().trim()
        val codigoInterno = binding.etCodigoInterno.text.toString().trim()
        val tipo = binding.spinnerTipo.text.toString().trim()
        val laboratorio = binding.etLaboratorio.text.toString().trim()
        val tipoMuestra = binding.spinnerTipoMuestra.text.toString().trim()
        val metodoAnalisis = binding.etMetodoAnalisis.text.toString().trim()
        val instrucciones = binding.etInstrucciones.text.toString().trim()
        val costoCompra = binding.etCostoCompra.text.toString()
        val precioUnitario = binding.etPrecioUnitario.text.toString()
        val margenGanancia = binding.etMargenGanancia.text.toString()
        val porcentajeIva = binding.etPorcentajeIva.text.toString()
        val precioFinal = binding.etPrecioFinal.text.toString()
        val activo = binding.rbActivo.isChecked

        if (nombre.isEmpty() || tipo.isEmpty()) {
            ConfigLoading.hideLoadingAnimation()
            DialogMaterialHelper.mostrarErrorDialog(
                requireActivity(),
                "Los campos Nombre y Tipo son obligatorios"
            )
            return
        }

        try {
            // En modo edición, actualizar el objeto existente para conservar el ID
            val auxiliar = if (isEditMode && auxiliarToEdit != null) {
                auxiliarToEdit!!.apply {
                    this.nombre = nombre
                    this.codigoInterno = codigoInterno
                    this.tipo = tipo
                    this.laboratorio = laboratorio
                    this.tipoMuestra = tipoMuestra
                    this.metodoAnalisis = metodoAnalisis
                    this.instrucciones = instrucciones
                    this.costoCompra = costoCompra
                    this.precioUnitario = precioUnitario
                    this.margenGanancia = margenGanancia
                    this.porcentajeIva = porcentajeIva
                    this.precioFinal = precioFinal
                    this.activo = activo
                }
            } else {
                AuxiliarModel(
                    nombre = nombre,
                    codigoInterno = codigoInterno,
                    tipo = tipo,
                    laboratorio = laboratorio,
                    tipoMuestra = tipoMuestra,
                    metodoAnalisis = metodoAnalisis,
                    instrucciones = instrucciones,
                    costoCompra = costoCompra,
                    precioUnitario = precioUnitario,
                    margenGanancia = margenGanancia,
                    porcentajeIva = porcentajeIva,
                    precioFinal = precioFinal,
                    activo = activo,
                    fechaRegistro = UtilHelper.getDate()
                )
            }

            Log.d(TAG, "Iniciando guardado de auxiliar: ${auxiliar.nombre}")

            if (isEditMode) {
                FirebaseAuxiliarUtil.actualizarAuxiliar(auxiliar) { success, message ->
                    Log.d(TAG, "Callback de actualización recibido: $success, $message")
                    ConfigLoading.hideLoadingAnimation()
                    if (success) {
                        DialogMaterialHelper.mostrarSuccessClickDialog(
                            requireActivity(),
                            "Auxiliar actualizado correctamente"
                        ) {
                            UtilFragment.changeFragment(requireContext(), ListaAuxiliaresFragment(), TAG)
                        }
                    } else {
                        DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error: $message")
                    }
                }
            } else {
                Log.d(TAG, "Enviando solicitud para agregar auxiliar")
                FirebaseAuxiliarUtil.agregarAuxiliar(auxiliar) { success, message ->
                    Log.d(TAG, "Callback de agregar recibido: $success, $message")
                    ConfigLoading.hideLoadingAnimation()
                    if (success) {
                        DialogMaterialHelper.mostrarSuccessClickDialog(
                            requireActivity(),
                            "Auxiliar guardado correctamente"
                        ) {
                            UtilFragment.changeFragment(requireContext(), ListaAuxiliaresFragment(), TAG)
                        }
                    } else {
                        DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error: $message")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar auxiliar: ${e.message}", e)
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
                    UtilFragment.changeFragment(requireContext(), ListaAuxiliaresFragment(), TAG)
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 