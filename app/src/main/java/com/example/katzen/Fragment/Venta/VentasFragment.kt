package com.example.katzen.Fragment.Venta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.katzen.MenuFragment
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.databinding.VentasFragmentBinding

class VentasFragment : Fragment() {
    val TAG : String  = "VentasFragment"

    private var _binding: VentasFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = VentasFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}