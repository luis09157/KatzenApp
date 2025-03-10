package com.example.katzen.Fragment.Producto

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseMedicamentoUtil
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.ProductoMedicamentoModel
import com.ninodev.katzen.databinding.AddProductoMedicamentoFragmentBinding

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        medicamentoToEdit = arguments?.getParcelable(ARG_MEDICAMENTO)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddProductoMedicamentoFragmentBinding.inflate(inflater, container, false)
        initLoading()
        setupUnidadMedida()
        return binding.root
    }

    private fun initLoading() {
        ConfigLoading.LOTTIE_ANIMATION_VIEW = binding.lottieAnimationView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupCalculations()
        medicamentoToEdit?.let { loadMedicamentoData(it) }
    }

    private fun setupUnidadMedida() {
        val unidadesMedida = arrayOf("Unidad", "Mililitros", "Gramos", "Tabletas", "Cápsulas")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, unidadesMedida)
        binding.spUnidadMedida.setAdapter(adapter)
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

    private fun setupListeners() {
        binding.btnGuardar.setOnClickListener {
            guardarMedicamento()
        }
    }

    private fun guardarMedicamento() {
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
            DialogMaterialHelper.mostrarErrorDialog(
                requireActivity(),
                "El nombre, costo y unidad de medida son obligatorios"
            )
            return
        }

        val medicamento = ProductoMedicamentoModel(
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

        val (success, message) = FirebaseMedicamentoUtil.guardarMedicamento(medicamento)
        
        ConfigLoading.hideLoadingAnimation()
        
        if (success) {
            DialogMaterialHelper.mostrarSuccessClickDialog(
                requireActivity(), 
                message
            ) {
                UtilFragment.changeFragment(requireContext(), ListaMedicamentosFragment(), TAG)
            }
        } else {
            DialogMaterialHelper.mostrarErrorDialog(requireActivity(), message)
        }
    }

    private fun loadMedicamentoData(medicamento: ProductoMedicamentoModel) {
        with(binding) {
            etNombre.setText(medicamento.nombre)
            etCodigoInterno.setText(medicamento.codigoInterno)
            etCodigoBarras.setText(medicamento.codigoBarras)
            etInstrucciones.setText(medicamento.descripcion)
            spUnidadMedida.setText(medicamento.unidadMedida)
            etCostoCompra.setText(medicamento.costoCompra)
            etMargenGanancia.setText(medicamento.margenGanancia)
            etPorcentajeIva.setText(medicamento.iva)
            
            when (medicamento.tipo) {
                "Vacuna" -> rbVacuna.isChecked = true
                "Antiparasitario" -> rbAntiparasitario.isChecked = true
                else -> rbOtro.isChecked = true
            }
            
            rbActivo.isChecked = medicamento.activo
            rbInactivo.isChecked = !medicamento.activo
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