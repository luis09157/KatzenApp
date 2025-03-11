package com.example.katzen.Fragment.Producto

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Config.Config
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseMedicamentoUtil
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.ProductoMedicamentoModel
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.AddProductoMedicamentoFragmentBinding
import java.text.DecimalFormat

class AddProductoMedicamentoFragment : Fragment() {
    private var _binding: AddProductoMedicamentoFragmentBinding? = null
    private val binding get() = _binding!!
    private val TAG = "AddProductoMedicamentoFragment"

    companion object {
        private const val ARG_MEDICAMENTO = "medicamento"

        fun newInstance(medicamento: ProductoMedicamentoModel): AddProductoMedicamentoFragment {
            val fragment = AddProductoMedicamentoFragment()
            val args = Bundle()
            args.putParcelable(ARG_MEDICAMENTO, medicamento)
            fragment.arguments = args
            return fragment
        }
    }

    private var medicamentoToEdit: ProductoMedicamentoModel? = null
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().title = getString(R.string.submenu_add_productos_medicamentos)
        medicamentoToEdit = arguments?.getParcelable(ARG_MEDICAMENTO)
        isEditMode = medicamentoToEdit != null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddProductoMedicamentoFragmentBinding.inflate(inflater, container, false)
        initLoading()
        setupUnidadesMedida()
        setupCalculations()
        setupListeners()
        
        if (isEditMode) {
            cargarDatosMedicamento()
        }
        
        return binding.root
    }

    fun initLoading() {
        ConfigLoading.init(
            binding.lottieAnimationView,
            binding.contAddProducto,
            binding.fragmentNoData.contNoData
        )
    }


    private fun setupUnidadesMedida() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            Config.UNIDADES_MEDIDA
        )
        
        (binding.spUnidadMedida as? AutoCompleteTextView)?.setAdapter(adapter)
    }

    private fun setupCalculations() {
        // Calcular precio por unidad cuando cambie el margen de ganancia
        binding.etMargenGanancia.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calcularPrecios()
            }
        })

        // Calcular precio final cuando cambie el IVA
        binding.etPorcentajeIva.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calcularPrecios()
            }
        })

        // También agregar listener para el costo de compra
        binding.etCostoCompra.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calcularPrecios()
            }
        })
    }

    private fun calcularPrecios() {
        val costoStr = binding.etCostoCompra.text.toString()
        val margenStr = binding.etMargenGanancia.text.toString()
        val ivaStr = binding.etPorcentajeIva.text.toString()

        if (costoStr.isNotEmpty() && margenStr.isNotEmpty()) {
            val costo = costoStr.toDoubleOrNull() ?: 0.0
            val margen = margenStr.toDoubleOrNull() ?: 0.0
            val iva = ivaStr.toDoubleOrNull() ?: 0.0

            val precioSinIva = costo * (1 + margen/100)
            val precioFinal = precioSinIva * (1 + iva/100)

            binding.etPrecioPorUnidad.setText(String.format("%.2f", precioSinIva))
            binding.etPrecioFinal.setText(String.format("%.2f", precioFinal))
        }
    }

    private fun cargarDatosMedicamento() {
        medicamentoToEdit?.let {
            binding.etNombre.setText(it.nombre)
            binding.etCodigoInterno.setText(it.codigoInterno)
            binding.etCodigoBarras.setText(it.codigoBarras)
            binding.etInstrucciones.setText(it.descripcion)
            binding.spUnidadMedida.setText(it.unidadMedida, false)
            binding.etCostoCompra.setText(it.costoCompra)
            binding.etMargenGanancia.setText(it.margenGanancia)
            binding.etPorcentajeIva.setText(it.iva)
            binding.etPrecioPorUnidad.setText(it.precioSinIva)
            binding.etPrecioFinal.setText(it.precio)
            
            when (it.tipo) {
                "Vacuna" -> binding.rbVacuna.isChecked = true
                "Antiparasitario" -> binding.rbAntiparasitario.isChecked = true
                else -> binding.rbOtro.isChecked = true
            }
            
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
        val codigoBarras = binding.etCodigoBarras.text.toString().trim()
        val instrucciones = binding.etInstrucciones.text.toString().trim()
        val unidadMedida = binding.spUnidadMedida.text.toString()
        val costoCompra = binding.etCostoCompra.text.toString()
        val margenGanancia = binding.etMargenGanancia.text.toString()
        val precioSinIva = binding.etPrecioPorUnidad.text.toString()
        val iva = binding.etPorcentajeIva.text.toString()
        val precioFinal = binding.etPrecioFinal.text.toString()
        val tipo = when {
            binding.rbVacuna.isChecked -> "Vacuna"
            binding.rbAntiparasitario.isChecked -> "Antiparasitario"
            else -> "Otro"
        }
        val activo = binding.rbActivo.isChecked

        if (nombre.isEmpty() || costoCompra.isEmpty() || unidadMedida.isEmpty()) {
            ConfigLoading.hideLoadingAnimation()
            DialogMaterialHelper.mostrarErrorDialog(
                requireActivity(),
                "El nombre, costo y unidad de medida son obligatorios"
            )
            return
        }

        try {
            // En modo edición, actualizar el objeto existente para conservar el ID
            val medicamento = if (isEditMode && medicamentoToEdit != null) {
                medicamentoToEdit!!.apply {
                    this.nombre = nombre
                    this.descripcion = instrucciones
                    this.codigoInterno = codigoInterno
                    this.codigoBarras = codigoBarras
                    this.tipo = tipo
                    this.unidadMedida = unidadMedida
                    this.costoCompra = costoCompra
                    this.margenGanancia = margenGanancia
                    this.precioSinIva = precioSinIva
                    this.iva = iva
                    this.precio = precioFinal
                    this.categoria = tipo
                    this.activo = activo
                }
            } else {
                ProductoMedicamentoModel(
                    nombre = nombre,
                    descripcion = instrucciones,
                    codigoInterno = codigoInterno,
                    codigoBarras = codigoBarras,
                    tipo = tipo,
                    unidadMedida = unidadMedida,
                    costoCompra = costoCompra,
                    margenGanancia = margenGanancia,
                    precioSinIva = precioSinIva,
                    iva = iva,
                    precio = precioFinal,
                    categoria = tipo,
                    activo = activo,
                    fechaRegistro = UtilHelper.getDate()
                )
            }

            Log.d(TAG, "Iniciando guardado de medicamento: ${medicamento.nombre}")

            if (isEditMode) {
                FirebaseMedicamentoUtil.actualizarMedicamento(medicamento) { success, message ->
                    Log.d(TAG, "Callback de actualización recibido: $success, $message")
                    ConfigLoading.hideLoadingAnimation()
                    if (success) {
                        DialogMaterialHelper.mostrarSuccessClickDialog(
                            requireActivity(),
                            "Medicamento actualizado correctamente"
                        ) {
                            UtilFragment.changeFragment(requireContext(), ListaMedicamentosFragment(), TAG)
                        }
                    } else {
                        DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error: $message")
                    }
                }
            } else {
                Log.d(TAG, "Enviando solicitud para agregar medicamento")
                FirebaseMedicamentoUtil.agregarMedicamento(medicamento) { success, message ->
                    Log.d(TAG, "Callback de agregar recibido: $success, $message")
                    ConfigLoading.hideLoadingAnimation()
                    if (success) {
                        DialogMaterialHelper.mostrarSuccessClickDialog(
                            requireActivity(),
                            "Medicamento guardado correctamente"
                        ) {
                            UtilFragment.changeFragment(requireContext(), ListaMedicamentosFragment(), TAG)
                        }
                    } else {
                        DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error: $message")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar medicamento: ${e.message}", e)
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
                    UtilFragment.changeFragment(requireContext(), MenuProductosFragment(), TAG)
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}