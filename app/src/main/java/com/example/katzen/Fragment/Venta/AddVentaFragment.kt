package com.example.katzen.Fragment.Venta

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Config.Config
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseVentaUtil
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.ProductoModel
import com.example.katzen.Model.VentaModel
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.AddVentaFragmentBinding

class AddVentaFragment : Fragment() {

    private var _binding: AddVentaFragmentBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_PRODUCTO_ID = "producto_id"
        private const val ARG_PRODUCTO_NOMBRE = "producto_nombre"
        private const val ARG_PRODUCTO_VENTA = "producto_venta"
        private const val ARG_PRODUCTO_COSTO = "producto_costo"
        private const val ARG_PRODUCTO_CATEGORIA = "producto_categoria"

        fun newInstance(producto: ProductoModel? = null): AddVentaFragment {
            return AddVentaFragment().apply {
                if (producto != null) {
                    arguments = Bundle().apply {
                        putString(ARG_PRODUCTO_ID, producto.id)
                        putString(ARG_PRODUCTO_NOMBRE, producto.nombre)
                        putDouble(ARG_PRODUCTO_VENTA, producto.precioVenta)
                        putDouble(ARG_PRODUCTO_COSTO, producto.costo)
                        putString(ARG_PRODUCTO_CATEGORIA, producto.categoria)
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddVentaFragmentBinding.inflate(inflater, container, false)
        initCampos()
        setupCalculoGanancia()
        binding.btnGuardar.setOnClickListener { guardarVenta() }
        return binding.root
    }

    private fun initCampos() {
        binding.editTextFecha.setText(UtilHelper.getDate())
        val adapterMetodosPago = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            Config.METODOS_PAGO
        )
        binding.editTextMetodoPago.adapter = adapterMetodosPago

        arguments?.let { args ->
            binding.editTextArticulo.setText(args.getString(ARG_PRODUCTO_NOMBRE).orEmpty())
            binding.editTextVenta.setText(args.getDouble(ARG_PRODUCTO_VENTA).toString())
            binding.editTextCosto.setText(args.getDouble(ARG_PRODUCTO_COSTO).toString())
            recalcularGanancia()
        }
    }

    private fun setupCalculoGanancia() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                recalcularGanancia()
            }
        }
        binding.editTextVenta.addTextChangedListener(watcher)
        binding.editTextCosto.addTextChangedListener(watcher)
    }

    private fun recalcularGanancia() {
        val venta = binding.editTextVenta.text.toString().toDoubleOrNull() ?: 0.0
        val costo = binding.editTextCosto.text.toString().toDoubleOrNull() ?: 0.0
        binding.editTextGanancia.setText(
            String.format("%.2f", VentaModel.calcularGanancia(venta, costo))
        )
    }

    private fun guardarVenta() {
        val articulo = binding.editTextArticulo.text.toString().trim()
        val venta = binding.editTextVenta.text.toString().toDoubleOrNull()
        val costo = binding.editTextCosto.text.toString().toDoubleOrNull()

        if (articulo.isBlank() || venta == null || costo == null) {
            DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Completa artículo, venta y costo.")
            return
        }

        DialogMaterialHelper.mostrarConfirmDialog(requireActivity(), "¿Guardar esta venta?") { confirmed ->
            if (!confirmed) return@mostrarConfirmDialog
            ConfigLoading.showLoadingAnimation()

            val ventaModel = VentaModel(
                idProducto = arguments?.getString(ARG_PRODUCTO_ID).orEmpty(),
                articulo = articulo,
                venta = venta,
                costo = costo,
                fecha = binding.editTextFecha.text.toString().ifBlank { UtilHelper.getDate() },
                metodoPago = binding.editTextMetodoPago.selectedItem?.toString().orEmpty(),
                categoria = arguments?.getString(ARG_PRODUCTO_CATEGORIA).orEmpty()
            )

            FirebaseVentaUtil.guardarVenta(ventaModel)
                .addOnSuccessListener {
                    ConfigLoading.hideLoadingAnimation()
                    DialogMaterialHelper.mostrarSuccessDialog(requireActivity(), "Venta guardada correctamente")
                    UtilFragment.goBack(requireContext())
                }
                .addOnFailureListener { error ->
                    ConfigLoading.hideLoadingAnimation()
                    DialogMaterialHelper.mostrarErrorDialog(
                        requireActivity(),
                        "Error al guardar: ${error.message}"
                    )
                }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    UtilFragment.goBackOrHome(requireContext())
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
