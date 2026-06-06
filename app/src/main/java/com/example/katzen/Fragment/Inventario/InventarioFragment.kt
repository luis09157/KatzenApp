package com.example.katzen.Fragment.Inventario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.Producto.ProductoInventarioAdapter
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseCatalogoUtil
import com.example.katzen.DataBaseFirebase.FirebaseInventarioUtil
import com.example.katzen.Helper.ListScrollKeys
import com.example.katzen.Helper.ListUiHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Model.ProductoModel
import com.ninodev.katzen.databinding.InventarioFragmentBinding
import com.ninodev.katzen.R
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class InventarioFragment : Fragment() {
    val TAG: String = "InventarioFragment"

    private var _binding: InventarioFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var productosList: MutableList<ProductoModel>
    private lateinit var productosAdapter: ProductoInventarioAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = InventarioFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        requireActivity().title = getString(R.string.menu_inventario)
        initLoading()
        init()

        return root
    }

    private fun initLoading() {
        ConfigLoading.init(
            binding.lottieAnimationView,
            binding.contAddProducto,
            binding.fragmentNoData.contNoData
        )
    }

    private fun init() {
        ConfigLoading.showLoadingAnimation()
        productosList = mutableListOf()
        productosAdapter = ProductoInventarioAdapter { producto ->
            val addProductoInventarioFragment = AddProductoInventarioFragment()
            addProductoInventarioFragment.setProducto(producto)
            UtilFragment.changeFragment(
                requireContext(),
                addProductoInventarioFragment,
                TAG,
                listKey = ListScrollKeys.INVENTARIO,
                listRecyclerView = binding.menuListProductoInventario,
                selectedItemId = producto.id
            )
        }
        ListUiHelper.setupGridList(binding.menuListProductoInventario, 2)
        binding.menuListProductoInventario.adapter = productosAdapter
        obtenerProductos()
    }

    private fun obtenerInventario(productoModel: ProductoModel, onComplete: (ProductoModel) -> Unit) {
        FirebaseInventarioUtil.obtenerInventarioPorProducto(productoModel.id) { inventarioList ->
            for (inventario in inventarioList) {
                productoModel.cantidadInventario += inventario.cantidad
            }
            onComplete(productoModel)
        }
    }

    private fun obtenerProductos() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val productos = FirebaseCatalogoUtil.obtenerCatalogoUnificado()
                if (!isAdded) return@launch
                if (productos.isEmpty()) {
                    productosList.clear()
                    productosAdapter.updateList(emptyList())
                    ConfigLoading.showNodata()
                    return@launch
                }

                productosList.clear()
                var pendientes = productos.size

                productos.forEach { producto ->
                    obtenerInventario(producto) { productoActualizado ->
                        if (!isAdded) return@obtenerInventario
                        productosList.add(productoActualizado)
                        pendientes--
                        if (pendientes == 0) {
                            productosAdapter.updateList(productosList.toList())
                            ListUiHelper.restoreScrollIfPending(
                                ListScrollKeys.INVENTARIO,
                                binding.menuListProductoInventario,
                                productosList.map { it.id }
                            )
                            ConfigLoading.hideLoadingAnimation()
                        }
                    }
                }
            } catch (_: Exception) {
                if (!isAdded) return@launch
                ConfigLoading.showNodata()
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
