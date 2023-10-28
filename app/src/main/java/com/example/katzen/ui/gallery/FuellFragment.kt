package com.example.katzen.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.katzen.Config.Config
import com.example.katzen.Helper.UtilHelper
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
        var position = 0

        if(binding.rbSemana.isChecked){
           position = 0
        }else if(binding.rbCampaA.isChecked){
           position = 2
        }else if(binding.rbRuta.isChecked) {
            position = 3
        }else if(binding.rbMoto.isChecked){
            position = 4
        }

        val (costoT,gananciaT,ventaT) = UtilHelper.calcular(km,Config.CATEGORIAS.get(position))

        binding.txtCosto.text = "$ ${costoT}"
        binding.txtGanancia.text = "$ ${gananciaT}"
        binding.txtVenta.text = "$ ${ventaT}"
    }

    fun clean(){
        binding.txtCosto.text = getString(R.string.title_dinero)
        binding.txtGanancia.text = getString(R.string.title_dinero)
        binding.txtVenta.text = getString(R.string.title_dinero)
        binding.etCosto.text.clear()
    }
}