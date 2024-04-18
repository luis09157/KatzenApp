package com.example.katzen.Fragment.Mascota

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.databinding.PacienteFragmentBinding

class PacienteFragment : Fragment() {
    val TAG : String  = "PacienteFragment"

    private var _binding: PacienteFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PacienteFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initLoading()
        listeners()

        return root
    }

    fun listeners(){
        binding.btnAddPaciente.setOnClickListener {
            UtilFragment.changeFragment(requireActivity(), AddMascotaFragment(),TAG)
        }
    }
    fun initLoading(){
        ConfigLoading.LOTTIE_ANIMATION_VIEW = binding.lottieAnimationView
        ConfigLoading.CONT_ADD_PRODUCTO = binding.contAddProducto
        ConfigLoading.FRAGMENT_NO_DATA = binding.fragmentNoData.contNoData
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}