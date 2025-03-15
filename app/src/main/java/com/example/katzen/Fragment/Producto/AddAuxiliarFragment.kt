package com.example.katzen.Fragment.Producto

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
import com.example.katzen.DataBaseFirebase.FirebaseAuxiliarUtil
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.AuxiliarModel
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.FragmentAddAuxiliarBinding

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

    private fun cargarDatosAuxiliar() {
        auxiliarToEdit?.let {
            binding.etNombre.setText(it.nombre)
            binding.etCodigoInterno.setText(it.codigoInterno)
            
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
        val activo = binding.rbActivo.isChecked

        if (nombre.isEmpty()) {
            ConfigLoading.hideLoadingAnimation()
            DialogMaterialHelper.mostrarErrorDialog(
                requireActivity(),
                "El nombre es obligatorio"
            )
            return
        }

        try {
            // En modo edición, actualizar el objeto existente para conservar el ID
            val auxiliar = if (isEditMode && auxiliarToEdit != null) {
                auxiliarToEdit!!.apply {
                    this.nombre = nombre
                    this.codigoInterno = codigoInterno
                    this.activo = activo
                }
            } else {
                AuxiliarModel(
                    nombre = nombre,
                    codigoInterno = codigoInterno,
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