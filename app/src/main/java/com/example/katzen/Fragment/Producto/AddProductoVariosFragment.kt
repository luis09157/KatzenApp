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
import com.ninodev.katzen.DataBaseFirebase.FirebaseProductoVariosUtil
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper
import com.ninodev.katzen.Model.ProductoVariosModel
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.FragmentAddProductoVariosBinding
import java.text.DecimalFormat

class AddProductoVariosFragment : Fragment() {
    private var _binding: FragmentAddProductoVariosBinding? = null
    private val binding get() = _binding!!
    private val TAG = "AddProductoVariosFragment"
    private val df = DecimalFormat("#,##0.00")
    
    // Flags para evitar cálculos recursivos
    private var isCalculatingPrecioVenta = false
    private var isCalculatingMargen = false

    companion object {
        private const val ARG_PRODUCTO = "producto"

        fun newInstance(producto: ProductoVariosModel): AddProductoVariosFragment {
            val fragment = AddProductoVariosFragment()
            val args = Bundle()
            args.putParcelable(ARG_PRODUCTO, producto)
            fragment.arguments = args
            return fragment
        }
    }

    private var productoToEdit: ProductoVariosModel? = null
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().title = "Nuevo Producto"
        productoToEdit = arguments?.getParcelable(ARG_PRODUCTO)
        isEditMode = productoToEdit != null
        
        if (isEditMode) {
            requireActivity().title = "Editar Producto"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductoVariosBinding.inflate(inflater, container, false)
        initLoading()
        setupUnidadMedida()
        setupCalculations()
        setupListeners()
        
        if (isEditMode) {
            cargarDatosProducto()
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
    
    private fun setupUnidadMedida() {
        val unidades = arrayOf("Unidad", "Litro (L)", "Mililitro (ml)", "Kilogramo (kg)", "Gramo (g)", "Miligramo (mg)")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, unidades)
        (binding.spUnidadMedida as? AutoCompleteTextView)?.setAdapter(adapter)
        (binding.spUnidadMedida as? AutoCompleteTextView)?.setText("Unidad", false)
    }

    private fun setupCalculations() {
        // Watcher para costo de compra
        binding.etCostoCompra.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!isCalculatingMargen && !isCalculatingPrecioVenta) {
                    calcularPrecioVentaDesdeMargen()
                }
            }
        })

        // Watcher para margen de ganancia
        binding.etMargenGanancia.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!isCalculatingMargen) {
                    calcularPrecioVentaDesdeMargen()
                }
            }
        })

        // Watcher para precio de venta
        binding.etPrecioVenta.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!isCalculatingPrecioVenta) {
                    calcularMargenDesdePrecioVenta()
                    calcularPrecioFinal()
                }
            }
        })

        // Watcher para IVA
        binding.etIva.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calcularPrecioFinal()
            }
        })
    }

    private fun calcularPrecioVentaDesdeMargen() {
        try {
            val costoCompra = binding.etCostoCompra.text.toString().toDoubleOrNull() ?: 0.0
            val margenGanancia = binding.etMargenGanancia.text.toString().toDoubleOrNull() ?: 0.0
            
            if (costoCompra > 0) {
                isCalculatingPrecioVenta = true
                
                // Calcular precio de venta basado en costo y margen
                val precioVenta = costoCompra * (1 + (margenGanancia / 100))
                binding.etPrecioVenta.setText(df.format(precioVenta))
                
                // Actualizar precio final
                calcularPrecioFinal()
                
                isCalculatingPrecioVenta = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al calcular precio venta desde margen: ${e.message}")
            isCalculatingPrecioVenta = false
        }
    }
    
    private fun calcularMargenDesdePrecioVenta() {
        try {
            val costoCompra = binding.etCostoCompra.text.toString().toDoubleOrNull() ?: 0.0
            val precioVenta = binding.etPrecioVenta.text.toString().toDoubleOrNull() ?: 0.0
            
            if (costoCompra > 0 && precioVenta > 0) {
                isCalculatingMargen = true
                
                // Calcular margen basado en costo y precio de venta
                val margen = ((precioVenta / costoCompra) - 1) * 100
                binding.etMargenGanancia.setText(df.format(margen))
                
                isCalculatingMargen = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al calcular margen desde precio venta: ${e.message}")
            isCalculatingMargen = false
        }
    }

    private fun calcularPrecioFinal() {
        try {
            val precioVenta = binding.etPrecioVenta.text.toString().toDoubleOrNull() ?: 0.0
            val porcentajeIva = binding.etIva.text.toString().toDoubleOrNull() ?: 0.0
            
            // Aplicar IVA
            val iva = precioVenta * (porcentajeIva / 100)
            val precioFinal = precioVenta + iva
            
            binding.etPrecioFinal.setText(df.format(precioFinal))
        } catch (e: Exception) {
            Log.e(TAG, "Error al calcular precio final: ${e.message}")
            binding.etPrecioFinal.setText("0.00")
        }
    }

    private fun getUnidadMedidaSeleccionada(): String {
        return (binding.spUnidadMedida as? AutoCompleteTextView)?.text.toString()
    }

    private fun setUnidadMedidaSeleccionada(unidadMedida: String) {
        (binding.spUnidadMedida as? AutoCompleteTextView)?.setText(unidadMedida, false)
    }

    private fun cargarDatosProducto() {
        productoToEdit?.let {
            binding.etNombre.setText(it.nombre)
            binding.etCodigoInterno.setText(it.codigoInterno)
            binding.etCodigoBarras.setText(it.codigoBarras)
            binding.etCostoCompra.setText(it.costoCompra)
            binding.etPrecioVenta.setText(it.precioVenta)
            binding.etMargenGanancia.setText(it.margenGanancia)
            binding.etIva.setText(it.iva)
            binding.etPrecioFinal.setText(it.precioFinal)
            
            // Establecer unidad de medida
            setUnidadMedidaSeleccionada(it.unidadMedida)
            
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
        val descripcion = binding.etDescripcion.text.toString().trim()
        val costoCompra = binding.etCostoCompra.text.toString()
        val precioVenta = binding.etPrecioVenta.text.toString()
        val margenGanancia = binding.etMargenGanancia.text.toString()
        val iva = binding.etIva.text.toString()
        val precioFinal = binding.etPrecioFinal.text.toString()
        val unidadMedida = getUnidadMedidaSeleccionada()
        val activo = binding.rbActivo.isChecked

        if (nombre.isEmpty() || precioVenta.isEmpty()) {
            ConfigLoading.hideLoadingAnimation()
            DialogMaterialHelper.mostrarErrorDialog(
                requireActivity(),
                "El nombre y precio por unidad son obligatorios"
            )
            return
        }

        try {
            // En modo edición, actualizar el objeto existente para conservar el ID
            val producto = if (isEditMode && productoToEdit != null) {
                productoToEdit!!.apply {
                    this.nombre = nombre
                    this.codigoInterno = codigoInterno
                    this.codigoBarras = codigoBarras
                    this.costoCompra = costoCompra
                    this.precioVenta = precioVenta
                    this.margenGanancia = margenGanancia
                    this.iva = iva
                    this.precioFinal = precioFinal
                    this.unidadMedida = unidadMedida
                    this.activo = activo
                }
            } else {
                ProductoVariosModel(
                    nombre = nombre,
                    codigoInterno = codigoInterno,
                    codigoBarras = codigoBarras,
                    costoCompra = costoCompra,
                    precioVenta = precioVenta,
                    margenGanancia = margenGanancia,
                    iva = iva,
                    precioFinal = precioFinal,
                    unidadMedida = unidadMedida,
                    activo = activo,
                    fechaRegistro = UtilHelper.getDate()
                )
            }

            Log.d(TAG, "Iniciando guardado de producto: ${producto.nombre}")

            if (isEditMode) {
                FirebaseProductoVariosUtil.actualizarProductoVarios(producto) { success, message ->
                    Log.d(TAG, "Callback de actualización recibido: $success, $message")
                    ConfigLoading.hideLoadingAnimation()
                    if (success) {
                        DialogMaterialHelper.mostrarSuccessClickDialog(
                            requireActivity(),
                            "Producto actualizado correctamente"
                        ) {
                            UtilFragment.changeFragment(requireContext(), ListaProductosVariosFragment(), TAG)
                        }
                    } else {
                        DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error: $message")
                    }
                }
            } else {
                Log.d(TAG, "Enviando solicitud para agregar producto")
                FirebaseProductoVariosUtil.agregarProductoVarios(producto) { success, message ->
                    Log.d(TAG, "Callback de agregar recibido: $success, $message")
                    ConfigLoading.hideLoadingAnimation()
                    if (success) {
                        DialogMaterialHelper.mostrarSuccessClickDialog(
                            requireActivity(),
                            "Producto guardado correctamente"
                        ) {
                            UtilFragment.changeFragment(requireContext(), ListaProductosVariosFragment(), TAG)
                        }
                    } else {
                        DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error: $message")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar producto: ${e.message}", e)
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
                    UtilFragment.changeFragment(requireContext(), ListaProductosVariosFragment(), TAG)
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 