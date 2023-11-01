package com.example.katzen.ui.paciente

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.katzen.databinding.FragmentAddPacienteBinding


class AgregarPacienteFragment : Fragment() {
    private var _binding: FragmentAddPacienteBinding? = null
    private val binding get() = _binding!!
    var spinnerStates = ArrayList<String>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddPacienteBinding.inflate(inflater, container, false)
        val root: View = binding.root

        spinnerStates.add("Seleccione una opción")
        spinnerStates.add("Canino")
        spinnerStates.add("Felino")


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}