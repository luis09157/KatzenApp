package com.example.katzen.Fragment.Servicio

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseServicioUtil
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.ServicioModel
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.FragmentAddServicioBinding
import java.text.DecimalFormat

class AddServicioFragment : Fragment() {
    private var _binding: FragmentAddServicioBinding? = null
    private val binding get() = _binding!!
    private val TAG = "AddServicioFragment"

    companion object {
        private const val ARG_SERVICIO = "servicio"

        fun newInstance(servicio: ServicioModel): AddServicioFragment {
            val fragment = AddServicioFragment()
            val args = Bundle()
            args.putParcelable(ARG_SERVICIO, servicio)
            fragment.arguments = args
            return fragment
        }
    }

    private var servicioToEdit: ServicioModel? = null
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().title = getString(R.string.submenu_add_servicios)
        servicioToEdit = arguments?.getParcelable(ARG_SERVICIO)
        isEditMode = servicioToEdit != null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddServicioBinding.inflate(inflater, container, false)
        initLoading()
        setupCalculations()
        setupListeners()
        
        if (isEditMode) {
            cargarDatosServicio()
        }
        
        return binding.root
    }

    private fun initLoading() {
        ConfigLoading.init(
            binding.lottieAnimationView,
            binding.contAddServicio,
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
        binding.etPorcentajeIva.addTextChangedListener(precioWatcher)
    }

    private fun calcularPrecioFinal() {
        try {
            val precioUnitario = binding.etPrecioUnitario.text.toString().toDoubleOrNull() ?: 0.0
            val porcentajeIva = binding.etPorcentajeIva.text.toString().toDoubleOrNull() ?: 0.0
            
            val iva = precioUnitario * (porcentajeIva / 100)
            val precioFinal = precioUnitario + iva
            
            val df = DecimalFormat("#,##0.00")
            binding.etPrecioFinal.setText(df.format(precioFinal))
        } catch (e: Exception) {
            Log.e(TAG, "Error al calcular precio final: ${e.message}")
            binding.etPrecioFinal.setText("0.00")
        }
    }

    private fun cargarDatosServicio() {
        servicioToEdit?.let {
            binding.etNombre.setText(it.nombre)
            binding.etCodigoInterno.setText(it.codigoInterno)
            binding.etPrecioUnitario.setText(it.precioUnitario)
            binding.etPorcentajeIva.setText(it.iva)
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
        val precioUnitario = binding.etPrecioUnitario.text.toString()
        val iva = binding.etPorcentajeIva.text.toString()
        val precioFinal = binding.etPrecioFinal.text.toString()
        val activo = binding.rbActivo.isChecked

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
            val servicio = if (isEditMode && servicioToEdit != null) {
                servicioToEdit!!.apply {
                    this.nombre = nombre
                    this.codigoInterno = codigoInterno
                    this.precioUnitario = precioUnitario
                    this.iva = iva
                    this.precioFinal = precioFinal
                    this.activo = activo
                }
            } else {
                ServicioModel(
                    nombre = nombre,
                    codigoInterno = codigoInterno,
                    precioUnitario = precioUnitario,
                    iva = iva,
                    precioFinal = precioFinal,
                    activo = activo,
                    fechaRegistro = UtilHelper.getDate()
                )
            }

            Log.d(TAG, "Iniciando guardado de servicio: ${servicio.nombre}")

            if (isEditMode) {
                FirebaseServicioUtil.actualizarServicio(servicio) { success, message ->
                    Log.d(TAG, "Callback de actualización recibido: $success, $message")
                    ConfigLoading.hideLoadingAnimation()
                    if (success) {
                        DialogMaterialHelper.mostrarSuccessClickDialog(
                            requireActivity(),
                            "Servicio actualizado correctamente"
                        ) {
                            UtilFragment.changeFragment(requireContext(), ListaServiciosFragment(), TAG)
                        }
                    } else {
                        DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error: $message")
                    }
                }
            } else {
                Log.d(TAG, "Enviando solicitud para agregar servicio")
                FirebaseServicioUtil.agregarServicio(servicio) { success, message ->
                    Log.d(TAG, "Callback de agregar recibido: $success, $message")
                    ConfigLoading.hideLoadingAnimation()
                    if (success) {
                        DialogMaterialHelper.mostrarSuccessClickDialog(
                            requireActivity(),
                            "Servicio guardado correctamente"
                        ) {
                            UtilFragment.changeFragment(requireContext(), ListaServiciosFragment(), TAG)
                        }
                    } else {
                        DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error: $message")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar servicio: ${e.message}", e)
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
                    UtilFragment.changeFragment(requireContext(), ListaServiciosFragment(), TAG)
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 