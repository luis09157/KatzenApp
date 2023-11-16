package com.example.katzen.ui.gasolina

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.katzen.Config.Config
import com.example.katzen.Helper.GasHelper
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Helper.UtilHelper.Companion.hideKeyboard
import com.example.katzen.R
import com.example.katzen.databinding.FragmentGalleryBinding
import com.google.android.material.snackbar.Snackbar
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
            if(binding.spCategorias.text.toString().trim().isEmpty()){
                Snackbar.make(it, "Selecciona una categoria para calcular.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
                return@setOnClickListener
            }
            val (costo,ganancia,venta) =  GasHelper.calcular(binding.etCosto.text.toString().toDouble(),binding.spCategorias.text.toString())

            binding.txtGanancia.text = ganancia
            binding.txtCosto.text = costo
            binding.txtVenta.text = venta

        }
        binding.btnClean.setOnClickListener {
            clean()
        }
        binding.spCategorias.setOnClickListener {
            UtilHelper.hideKeyBoardWorld(requireActivity(),it)
        }

        val adapter = ArrayAdapter(requireActivity(),
            android.R.layout.simple_list_item_1,Config.CATEGORIAS)


        binding.spCategorias.setAdapter(adapter)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun clean(){
        binding.txtCosto.text = getString(R.string.title_dinero)
        binding.txtGanancia.text = getString(R.string.title_dinero)
        binding.txtVenta.text = getString(R.string.title_dinero)
        binding.etCosto.text?.clear()
    }
    override fun onResume() {
        super.onResume()
        if (view == null) {
            return
        }
        requireView().isFocusableInTouchMode = true
        requireView().requestFocus()
        requireView().setOnKeyListener { v, keyCode, event ->
            event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK
        }
    }
}