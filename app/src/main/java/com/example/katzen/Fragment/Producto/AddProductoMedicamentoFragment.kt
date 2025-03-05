package com.example.katzen.Fragment.Producto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.ninodev.katzen.databinding.AddProductoMedicamentoFragmentBinding

class AddProductoMedicamentoFragment : Fragment() {

    private var _binding: AddProductoMedicamentoFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddProductoMedicamentoFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Configurar el Spinner de Unidad de Medida
        setupUnidadMedidaSpinner()

        return root
    }

    private fun setupUnidadMedidaSpinner() {
        // Obtener referencia al AutoCompleteTextView desde el binding
        val spinnerUnidadMedida = binding.spinnerUnidadMedida

        // Lista de unidades de medida
        val unidades = arrayOf("u.", "l.", "ml.", "kg.", "gr.", "mg.")

        // Configurar el adaptador
        val adapter = ArrayAdapter(
            requireContext(), // Usar requireContext() en un Fragment
            android.R.layout.simple_dropdown_item_1line, // Layout predeterminado para el Spinner
            unidades
        )

        // Asignar el adaptador al AutoCompleteTextView
        spinnerUnidadMedida.setAdapter(adapter)

        // Manejar la selección de un elemento
        spinnerUnidadMedida.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = unidades[position]
            // Hacer algo con el elemento seleccionado
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}