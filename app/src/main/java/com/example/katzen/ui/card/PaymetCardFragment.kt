package com.example.katzen.ui.card

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Fragment.Paciente.PacienteFragment
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.MenuFragment
import com.example.katzen.R
import com.example.katzen.databinding.FragmentCardBinding
import com.google.android.material.snackbar.Snackbar
import java.math.RoundingMode
import java.text.DecimalFormat

class PaymetCardFragment : Fragment() {
    val TAG = "PaymetCardFragment"
    private var _binding: FragmentCardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        requireActivity().title = getString(R.string.menu_pago_tarjeta)

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
        binding.etCosto.text?.clear()
    }
    override fun onResume() {
        super.onResume()
        if (view == null) {
            return
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    UtilFragment.changeFragment(requireContext(), MenuFragment(), TAG)
                }
            })
    }
}