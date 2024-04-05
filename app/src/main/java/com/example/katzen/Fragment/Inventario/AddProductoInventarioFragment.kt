package com.example.katzen.Fragment.Inventario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.katzen.Config.Config
import com.example.katzen.Config.ConfigLoading
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

        initLoading()
        limpiar()
        init()
        setupListeners()


        return root
    }

    fun initLoading(){
        ConfigLoading.LOTTIE_ANIMATION_VIEW = binding.lottieAnimationView
        ConfigLoading.CONT_ADD_PRODUCTO = binding.contAddProducto
        ConfigLoading.FRAGMENT_NO_DATA = binding.fragmentNoData.contNoData
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
            it.hideKeyboard()
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

            // Validar el inventario antes de agregar el registro
            val validationResult = InventarioModel.validarInventario(productoInventario)
            if (validationResult.isValid) {
                // Mostrar animación de carga mientras se realiza la petición
                ConfigLoading.showLoadingAnimation()

                // Llamar a la función para agregar el registro de inventario
                FirebaseInventarioUtil.agregarRegistroInventario(requireContext(), this.productoModel.id, productoInventario, object :
                    FirebaseInventarioUtil.RegistroInventarioCallback {
                    override fun onRegistroAgregadoExitosamente() {
                        // Ocultar animación de carga cuando la petición se completa con éxito


                        limpiar()
                        init()
                        ConfigLoading.hideLoadingAnimation()
                        DialogMaterialHelper.mostrarSuccessDialog(requireContext(), "Producto agregado correctamente")
                    }

                    override fun onRegistroError(mensaje: String) {
                        // Ocultar animación de carga cuando hay un error en la petición
                        ConfigLoading.hideLoadingAnimation()

                        // Hubo un problema al agregar el registro
                        DialogMaterialHelper.mostrarErrorDialog(requireContext(), mensaje)
                    }
                })
            } else {
                // Mostrar un mensaje de error indicando la razón por la que el inventario no es válido
                limpiar()
                init()
                ConfigLoading.hideLoadingAnimation()
                DialogMaterialHelper.mostrarErrorDialog(requireContext(), validationResult.message)
            }
        }
    }

    fun init(){
        try {
            ConfigLoading.showLoadingAnimation()
            Picasso.get().load(productoModel.rutaImagen).into(binding.imageViewProducto)
            binding.editTextNombre.editText!!.setText(productoModel.nombre)

            val adapter = ArrayAdapter(requireActivity(),
                android.R.layout.simple_list_item_1, Config.UNIDAD_MEDIDA)
            binding.spUnidadMedida.setAdapter(adapter)

            binding.editTextFecha.editText!!.setText(CalendarioUtil.obtenerFechaHoraActual())

            obtenerInventarioI(productoModel!!) { inventarioActualizado ->
                if (inventarioActualizado.cantidad > 0.0) {
                    binding.spUnidadMedida.isEnabled = false
                    binding.editTextUnidadMedida.isEnabled = false
                    binding.editTextUnidadMedida.requestFocus()
                    binding.spUnidadMedida.requestFocus()
                }

                binding.editTextUnidadMedida.editText!!.setText(inventarioActualizado.unidadMedida)
                binding.editTextCantidad.placeholderText = inventarioActualizado.cantidad.toString()
                ConfigLoading.hideLoadingAnimation()
            }

        } catch (e: Exception) {
            DialogMaterialHelper.mostrarErrorDialog(requireContext(), getString(R.string.error_loading_product_data))
            ConfigLoading.showNodata()
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