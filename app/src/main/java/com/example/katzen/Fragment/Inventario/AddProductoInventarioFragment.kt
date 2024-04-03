package com.example.katzen.Fragment.Inventario

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
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

        limpiar()
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
            // Obtener la cantidad del EditText y convertirla a Double
            val cantidadProducto = binding.editTextCantidad.editText?.text.toString().toDoubleOrNull() ?: 0.0

            // Verificar qué radio button está seleccionado
            val isSumarSeleccionado = binding.radioSumar.isChecked

            // Ajustar la cantidad según la selección del radio button
            val cantidadAjustada = if (isSumarSeleccionado) {
                cantidadProducto  // Mantener la cantidad tal como está para la suma
            } else {
                -cantidadProducto  // Convertir la cantidad en negativa para la resta
            }

            // Obtener la unidad de medida y la fecha
            val unidadMedidaProducto = binding.spUnidadMedida.text.toString()
            val fechaProducto = binding.editTextFecha2.text.toString()

            // Crear un objeto InventarioModel con los datos del producto
            val productoInventario = InventarioModel(fechaProducto, cantidadAjustada, unidadMedidaProducto)

            // Llamar a la función para agregar el registro de inventario
            FirebaseInventarioUtil.agregarRegistroInventario(requireContext(), this.productoModel.id, productoInventario, object :
                FirebaseInventarioUtil.RegistroInventarioCallback {
                override fun onRegistroAgregadoExitosamente() {
                    limpiar()
                    init()
                    DialogMaterialHelper.mostrarSuccessDialog(requireContext(), "Producto agregado correctamente")
                }

                override fun onRegistroError(mensaje: String) {
                    // Hubo un problema al agregar el registro
                    DialogMaterialHelper.mostrarErrorDialog(requireContext(), mensaje)
                }
            })
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

            obtenerInventarioI(productoModel!!) { inventarioActualizado ->
                Log.e("errorluisiana",inventarioActualizado.cantidad.toString())
                if (inventarioActualizado.cantidad <= 0.0) {
                    binding.spUnidadMedida.isEnabled = false
                    binding.editTextUnidadMedida.isEnabled = false
                    binding.editTextUnidadMedida.requestFocus()
                }

                binding.editTextUnidadMedida.editText!!.setText(inventarioActualizado.unidadMedida)
                binding.editTextCantidad.placeholderText = inventarioActualizado.cantidad.toString()
            }

        } catch (e: Exception) {
            DialogMaterialHelper.mostrarErrorDialog(requireContext(), getString(R.string.error_loading_product_data))
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun obtenerInventarioI(productoModel: ProductoModel, onComplete: (InventarioModel) -> Unit) {
        FirebaseInventarioUtil.obtenerInventarioPorProducto(productoModel.id) { inventarioList ->
            var inventarioG  =  InventarioModel()
            // Actualizar el productoModel con la cantidad de inventario
            for (inventario in inventarioList) {
                inventarioG.cantidad += inventario.cantidad
                inventarioG.fecha = inventario.fecha
                inventarioG.unidadMedida = inventario.unidadMedida
            }
            // Llamar al onComplete con el productoModel actualizado
            onComplete(inventarioG)
        }
    }

    fun limpiar() {
        binding.editTextCantidad.editText?.setText("")
        binding.spUnidadMedida.setText("")
        binding.editTextFecha2.setText("")
    }

}