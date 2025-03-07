package com.example.katzen.Fragment.Producto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.katzen.Config.Config
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
        val adapter = ArrayAdapter(requireActivity(),
            android.R.layout.simple_list_item_1, Config.UNIDADES_MEDIDA)

        binding.spUnidadMedida.setAdapter(adapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}