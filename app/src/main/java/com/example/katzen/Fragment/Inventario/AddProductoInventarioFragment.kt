package com.example.katzen.Fragment.Inventario

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.katzen.Config.Config
import com.example.katzen.DataBaseFirebase.FirebaseInventarioUtil
import com.example.katzen.Helper.CalendarioUtil
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Helper.UtilHelper.Companion.hideKeyboard
import com.example.katzen.Model.InventarioModel
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
        setupListeners()

        return root
    }

    fun setupListeners(){
        binding.spUnidadMedida.setOnClickListener { it.hideKeyboard() }
        binding.editTextFecha2.setOnFocusChangeListener { view, isFocus ->
            UtilHelper.hideKeyBoardWorld(requireActivity(), view)
            if (isFocus) {
                CalendarioUtil.mostrarCalendario(requireContext(), binding.editTextFecha)
                view.hideKeyboard()
            }
        }
        binding.editTextFecha2.setOnClickListener { view ->
            CalendarioUtil.mostrarCalendario(requireContext(), binding.editTextFecha)
            view.hideKeyboard()
        }
        binding.btnAgregar.setOnClickListener {
            // Obtener los datos del producto desde los campos de entrada
            val nombreProducto = binding.editTextNombre.editText?.text.toString()
            val cantidadProducto = binding.editTextCantidad.editText?.text.toString().toDoubleOrNull() ?: 0.0
            val unidadMedidaProducto = binding.spUnidadMedida.text.toString()
            val fechaProducto = binding.editTextFecha2.text.toString()

            // Crear un objeto InventarioModel con los datos del producto
            val productoInventario = InventarioModel(fechaProducto, cantidadProducto, unidadMedidaProducto)

            // Llamar al método para agregar el producto al inventario en Firebase
            FirebaseInventarioUtil.agregarRegistroInventario(requireContext(),this.productoModel.id ,productoInventario)
        }
    }
    fun init(){
        try {
            Picasso.get().load(productoModel.rutaImagen).into(binding.imageViewProducto)
            binding.editTextNombre.editText!!.setText(productoModel.nombre)

            val adapter = ArrayAdapter(requireActivity(),
                android.R.layout.simple_list_item_1, Config.UNIDAD_MEDIDA)
            binding.spUnidadMedida.setAdapter(adapter)

            binding.editTextFecha.editText!!.setText(CalendarioUtil.obtenerFechaHoraActual())

        } catch (e: Exception) {
            DialogMaterialHelper.mostrarErrorDialog(requireContext(), getString(R.string.error_loading_product_data))
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}