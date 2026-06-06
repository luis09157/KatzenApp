package com.example.katzen.Fragment.Venta

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.katzen.Adapter.Producto.MenuProductosInventarioAdapter
import com.example.katzen.Config.Config
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseCatalogoUtil
import com.example.katzen.Helper.ListScrollKeys
import com.example.katzen.Helper.ListUiHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Model.ProductoModel
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.VentasFragmentBinding
import kotlinx.coroutines.launch

class VentasFragment : Fragment() {
    private val TAG = "VentasFragment"
    private var _binding: VentasFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var productosAdapter: MenuProductosInventarioAdapter
    private val productosList = mutableListOf<ProductoModel>()
    private val originalProductosList = mutableListOf<ProductoModel>()
    private var categoriaSeleccionada: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = VentasFragmentBinding.inflate(inflater, container, false)
        requireActivity().title = getString(R.string.menu_venta)
        initLoading()
        setupAdapter()
        setupFilters()
        cargarProductos()
        return binding.root
    }

    private fun initLoading() {
        ConfigLoading.init(
            binding.lottieAnimationView,
            binding.contAddProducto,
            binding.fragmentNoData.contNoData
        )
    }

    private fun setupAdapter() {
        ConfigLoading.showLoadingAnimation()
        productosAdapter = MenuProductosInventarioAdapter { producto ->
            UtilFragment.changeFragment(
                requireContext(),
                AddVentaFragment.newInstance(producto),
                TAG,
                listKey = ListScrollKeys.VENTAS,
                listRecyclerView = binding.lisMenuProductos,
                selectedItemId = producto.id
            )
        }
        ListUiHelper.setupGridList(binding.lisMenuProductos, 2)
        binding.lisMenuProductos.adapter = productosAdapter
    }

    private fun setupFilters() {
        binding.searchTextInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                aplicarFiltros()
            }
        })

        val adapterCategorias = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_list_item_1,
            listOf(getString(R.string.txtField_categoria)) + Config.CATEGORIAS_PRODUCTO
        )
        binding.spCategoria.setAdapter(adapterCategorias)
        binding.spCategoria.doOnTextChanged { text, _, _, _ ->
            categoriaSeleccionada = text?.toString()?.takeIf {
                it.isNotBlank() && it != getString(R.string.txtField_categoria)
            }
            aplicarFiltros()
        }

        binding.btnBuscar.setOnClickListener { aplicarFiltros() }

        binding.fab.setOnClickListener {
            UtilFragment.changeFragment(requireContext(), AddVentaFragment.newInstance(), TAG)
        }
    }

    private fun aplicarFiltros() {
        val query = binding.searchTextInputLayout.editText?.text?.toString().orEmpty()
        val filtered = originalProductosList.filter { producto ->
            val matchText = query.isBlank() ||
                producto.nombre.contains(query, ignoreCase = true)
            val matchCategoria = categoriaSeleccionada.isNullOrBlank() ||
                producto.categoria.equals(categoriaSeleccionada, ignoreCase = true)
            matchText && matchCategoria
        }
        productosList.clear()
        productosList.addAll(filtered)
        productosAdapter.updateList(productosList.toList())
        ListUiHelper.restoreScrollIfPending(
            ListScrollKeys.VENTAS,
            binding.lisMenuProductos,
            productosList.map { it.id }
        )
    }

    private fun cargarProductos() {
        ConfigLoading.showLoadingAnimation()
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val productos = FirebaseCatalogoUtil.obtenerCatalogoUnificado()
                if (!isAdded) return@launch
                productosList.clear()
                originalProductosList.clear()
                productosList.addAll(productos)
                originalProductosList.addAll(productos)
                aplicarFiltros()
                if (productosList.isEmpty()) {
                    ConfigLoading.showNodata()
                } else {
                    ConfigLoading.hideLoadingAnimation()
                }
            } catch (e: Exception) {
                if (!isAdded) return@launch
                ConfigLoading.showError(getString(R.string.portal_load_error)) {
                    cargarProductos()
                }
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
