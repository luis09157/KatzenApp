package com.example.katzen.Fragment.Producto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilFragment
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.FragmentListaEquiposBinding

class ListaEquiposFragment : Fragment() {
    private var _binding: FragmentListaEquiposBinding? = null
    private val binding get() = _binding!!
    private val TAG = "ListaEquiposFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListaEquiposBinding.inflate(inflater, container, false)
        requireActivity().title = "Equipos"
        initLoading()
        setupListeners()
        return binding.root
    }

    private fun initLoading() {
        ConfigLoading.init(
            binding.lottieAnimationView,
            binding.contAddProducto,
            binding.fragmentNoData.contNoData
        )
        
        // Mostrar mensaje de "En desarrollo"
        ConfigLoading.showNodata()
        binding.fragmentNoData.tvNoDataMessage.text = "Funcionalidad en desarrollo. Próximamente disponible."
    }

    private fun setupListeners() {
        // Aquí se configurarán los listeners cuando la funcionalidad esté implementada
    }

    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    UtilFragment.changeFragment(requireContext(), MenuProductosFragment(), TAG)
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 