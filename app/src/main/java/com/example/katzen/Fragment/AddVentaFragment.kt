package com.example.katzen.Fragment

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.example.katzen.Config.Config
import com.example.katzen.Helper.CalendarioUtil
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.databinding.AddVentaFragmentBinding

class AddVentaFragment : Fragment() {

    private var _binding: AddVentaFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddVentaFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        init()

        binding.btnGuardar.setOnClickListener {
            askSave()
        }

        binding.editTextFecha.setOnClickListener {
            //CalendarioUtil.mostrarCalendario(requireContext(), binding.editTextFecha)

        }
        binding.editTextFecha.setOnFocusChangeListener { view, isFocus ->
            if(isFocus){
               // CalendarioUtil.mostrarCalendario(requireContext(), binding.editTextFecha)

            }
        }
        return root
    }

    fun init(){
        binding.editTextFecha.setText(UtilHelper.getDate())
        val adapterMetodosPago = ArrayAdapter(requireContext(), R.layout.simple_list_item_1, Config.METODOS_PAGO)
        binding.editTextMetodoPago.setAdapter(adapterMetodosPago)
    }

    private fun askSave() {
        MaterialDialog(requireContext()).show {
            title(text = "Confirmación")
            message(text = "¿Estás seguro de guardar los datos?")
            positiveButton(text = "Guardar") {
                // Aquí puedes agregar lógica para guardar los datos
                // Ejemplo: guardarDatos()
            }
            negativeButton(text = "Cancelar")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}