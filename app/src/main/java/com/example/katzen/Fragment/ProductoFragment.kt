package com.example.katzen.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.katzen.databinding.VentasFragmentBinding

class ProductoFragment : Fragment() {
    val TAG : String  = "ProductoFragment"

    private var _binding: VentasFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = VentasFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.listProductos.setOnItemClickListener { adapterView, view, i, l ->

        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}