package com.example.katzen.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.katzen.R
import com.example.katzen.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import java.math.RoundingMode
import java.text.DecimalFormat

class PaymetCardFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.btnCalcula.setOnClickListener {
            if(binding.etCosto.text.toString().trim().isEmpty()){
                Snackbar.make(it, "Agrega la cantidad a calcular.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
                return@setOnClickListener
            }
            calcular(binding.etCosto.text.toString().toDouble())
        }

        binding.btnClean.setOnClickListener {
            clean()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun calcular(costo : Double){
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.DOWN

        val porcentaje =  0.045 * costo

        binding.txtCosto.text = "$ " + df.format(costo)
        binding.txtGanancia.text = "$ " + df.format(porcentaje)
        binding.txtVenta.text = "$ " + df.format(costo + porcentaje)
    }

    fun clean(){
        binding.txtCosto.text = getString(R.string.title_dinero)
        binding.txtGanancia.text = getString(R.string.title_dinero)
        binding.txtVenta.text = getString(R.string.title_dinero)
        binding.etCosto.text.clear()
    }
}