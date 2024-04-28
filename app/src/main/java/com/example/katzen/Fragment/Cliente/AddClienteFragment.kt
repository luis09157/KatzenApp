package com.example.katzen.Fragment.Cliente

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UpperCaseTextWatcher
import com.example.katzen.Model.ClienteModel
import com.example.katzen.databinding.AddClienteFragmentBinding

class AddClienteFragment : Fragment() {
    val TAG : String  = "AddClienteFragment"

    private var _binding: AddClienteFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddClienteFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initLoading()
        initListeners()

        return root
    }

    fun initLoading(){
        ConfigLoading.LOTTIE_ANIMATION_VIEW = binding.lottieAnimationView
        ConfigLoading.CONT_ADD_PRODUCTO = binding.contAddProducto
        ConfigLoading.FRAGMENT_NO_DATA = binding.fragmentNoData.contNoData
    }

    fun initListeners(){
        UpperCaseTextWatcher.UpperText(binding.textNombre)
        UpperCaseTextWatcher.UpperText(binding.textAppellidoPaterno)
        UpperCaseTextWatcher.UpperText(binding.textAppellidoMaterno)
        UpperCaseTextWatcher.UpperText(binding.textCalle)
        UpperCaseTextWatcher.UpperText(binding.textMunicipio)
        UpperCaseTextWatcher.UpperText(binding.textTelefono)
        UpperCaseTextWatcher.UpperText(binding.textColonia)

        binding.btnGuardar.setOnClickListener {
            val nombre = binding.textNombre.text.toString()
            val apellidoPaterno = binding.textAppellidoPaterno.text.toString()
            val apellidoMaterno = binding.textAppellidoMaterno.text.toString()
            val correo = binding.textCorreo.text.toString()
            val telefono = binding.textTelefono.text.toString()
            val calle = binding.textCalle.text.toString()
            val numero = binding.textNumero.text.toString()
            val colonia = binding.textColonia.text.toString()
            val municipio = binding.textMunicipio.text.toString()

            val cliente = ClienteModel(
                nombre = nombre,
                apellidoPaterno = apellidoPaterno,
                apellidoMaterno = apellidoMaterno,
                correo = correo,
                telefono = telefono,
                calle = calle,
                numero = numero,
                colonia = colonia,
                municipio = municipio
            )

            val validationResult = ClienteModel.validarCliente(requireContext(), cliente)
            if (validationResult.isValid) {
                // Data is valid, proceed with further actions
                // For example, you can save the client data or navigate to another screen
            } else {
                DialogMaterialHelper.mostrarErrorDialog(requireActivity(), validationResult.message)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
