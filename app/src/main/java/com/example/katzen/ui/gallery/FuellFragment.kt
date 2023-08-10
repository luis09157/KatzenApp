package com.example.katzen.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.katzen.R
import com.example.katzen.databinding.FragmentGalleryBinding
import com.google.android.material.snackbar.Snackbar
import java.math.RoundingMode
import java.text.DecimalFormat

class FuellFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
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

    fun calcular(km : Double){
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.DOWN
        var kmV = 0.00
        var porcentaje = 0.0

        if(binding.rbSemana.isChecked){
            kmV = km * 4
            porcentaje = 1.66
        }else if(binding.rbCampaA.isChecked){
            kmV = km * 4
            porcentaje = 1.32
        }else if(binding.rbRuta.isChecked) {
            kmV = km * 2
            porcentaje = 1.31
        }else if(binding.rbMoto.isChecked){
            kmV = km * 2
            porcentaje = 1.66
        }



        var costo = (kmV / 10) * 23
        var venta = costo * porcentaje

        binding.txtCosto.text = "$ " + df.format(costo)
        binding.txtGanancia.text = "$ " + df.format((venta - costo))
        binding.txtVenta.text = "$ " + df.format(venta)
    }

    fun clean(){
        binding.txtCosto.text = getString(R.string.title_dinero)
        binding.txtGanancia.text = getString(R.string.title_dinero)
        binding.txtVenta.text = getString(R.string.title_dinero)
        binding.etCosto.text.clear()
    }
}