package com.example.katzen.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.katzen.Config.Config
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilHelper.Companion.hideKeyboard
import com.example.katzen.Model.ProductoModel
import com.example.katzen.R
import com.example.katzen.databinding.AddPiezaProductoFragmentBinding
import com.squareup.picasso.Picasso

class AddProductoInventarioFragment : Fragment() {
    val TAG : String  = "AddProductoInventarioFragment"

    private var _binding: AddPiezaProductoFragmentBinding? = null
    private val binding get() = _binding!!
    var productoModel : ProductoModel = ProductoModel()
    fun setProducto(productoModel: ProductoModel){
        this.productoModel = productoModel
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddPiezaProductoFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        init()
        listeners()

        return root
    }

    fun listeners(){
        binding.spUnidadMedida.setOnClickListener { it.hideKeyboard() }
    }
    fun init(){
        try {
            Picasso.get().load(productoModel.rutaImagen).into(binding.imageViewProducto)
            binding.editTextNombre.editText!!.setText(productoModel.nombre)

            val adapter = ArrayAdapter(requireActivity(),
                android.R.layout.simple_list_item_1, Config.UNIDAD_MEDIDA)
            binding.spUnidadMedida.setAdapter(adapter)

        } catch (e: Exception) {
            DialogMaterialHelper.mostrarErrorDialog(requireContext(), getString(R.string.error_loading_product_data))
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}