package com.example.katzen.ui.mascota

import PacienteModel
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.MainActivity
import com.example.katzen.databinding.VistaMascotaDetalleBinding

class MascotaDetalleFragment(pacienteModel: PacienteModel) : Fragment() {
    val TAG : String = "MascotaDetalleFragment"
    private var _binding: VistaMascotaDetalleBinding? = null
    private var pacienteModel : PacienteModel = pacienteModel
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = VistaMascotaDetalleBinding.inflate(inflater, container, false)
        val root: View = binding.root

        (requireActivity() as MainActivity?)?.getSupportActionBar()?.setTitle("Mascota Detalle")

        initUI()

        return root
    }

    fun initUI() {
        binding.textNombre.isEnabled = false
        binding.textSexo.isEnabled = false
        binding.textRaza.isEnabled = false
        binding.textColor.isEnabled = false
        binding.textEspecie.isEnabled = false
        binding.textFechaNacimiento.isEnabled = false

        binding.textNombre.setText(pacienteModel.nombre)
        binding.textEspecie.setText(pacienteModel.especie)
        binding.textRaza.setText(pacienteModel.raza)
        binding.textColor.setText(pacienteModel.color)
        binding.textSexo.setText(pacienteModel.sexo)
    }

    override fun onResume() {
        super.onResume()
        if (view == null) {
            return
        }
        requireView().isFocusableInTouchMode = true
        requireView().requestFocus()
        requireView().setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                UtilFragment.changeFragment(requireContext(), MascotaFragment(),TAG)
                true
            } else false
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}