package com.example.katzen.Fragment.Cliente

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.MenuFragment
import com.example.katzen.R
import com.example.katzen.databinding.ClienteDetalleFragmentBinding

class ClienteDetalleFragment : Fragment() {
    val TAG : String  = "ClienteDetalleFragment"

    private var _binding: ClienteDetalleFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ClienteDetalleFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        requireActivity().title = getString(R.string.menu_cliente)

        initLoading()
        init()

        return root
    }

    fun init(){
        ConfigLoading.showLoadingAnimation()
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

    override fun onResume() {
        super.onResume()
        init()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                UtilFragment.changeFragment(requireContext() , MenuFragment() ,TAG)
            }
        })
    }

}