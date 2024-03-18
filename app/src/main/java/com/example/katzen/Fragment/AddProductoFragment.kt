package com.example.katzen.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.katzen.DataBaseFirebase.FirebaseProductoUtil
import com.example.katzen.Model.Producto
import com.example.katzen.Validator.ValidadorProducto
import com.example.katzen.databinding.AddProductoFragmentBinding
import java.util.Date

class AddProductoFragment : Fragment() {

    private var _binding: AddProductoFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddProductoFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root


        binding.btnGuardar.setOnClickListener {
            val producto = ValidadorProducto.validarYCrearProducto(
                binding.editTextNombre,
                binding.editTextPrecioVenta,
                binding.editTextCosto,
                binding.editTextFecha,
                binding.editTextMetodoPago
            )

            if (producto != null) {
                FirebaseProductoUtil.guardarProducto(producto)
            } else {
                // Alguno de los campos es inválido, no guardes el producto o muestra un mensaje de error adicional si lo deseas.
            }

        }

        return root
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}