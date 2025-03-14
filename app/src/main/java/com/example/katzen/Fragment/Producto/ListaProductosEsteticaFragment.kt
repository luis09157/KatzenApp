package com.example.katzen.Fragment.Producto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.ProductosEsteticaAdapter
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseProductoEsteticaUtil
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Model.ProductoEsteticaModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.FragmentListaProductosEsteticaBinding

class ListaProductosEsteticaFragment : Fragment() {
    private var _binding: FragmentListaProductosEsteticaBinding? = null
    private val binding get() = _binding!!
    private lateinit var productosAdapter: ProductosEsteticaAdapter
    private val productosList = mutableListOf<ProductoEsteticaModel>()
    private val productosListOriginal = mutableListOf<ProductoEsteticaModel>()
    private val TAG = "ListaProductosEsteticaFragment"
    private var valueEventListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListaProductosEsteticaBinding.inflate(inflater, container, false)
        requireActivity().title = getString(R.string.submenu_productos_estetica)
        initLoading()
        setupAdapter()
        setupListeners()
        setupSearchBar()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargarProductos()
    }

    private fun initLoading() {
        ConfigLoading.init(
            binding.lottieAnimationView,
            binding.contAddProducto,
            binding.fragmentNoData.contNoData
        )
    }

    private fun setupAdapter() {
        productosAdapter = ProductosEsteticaAdapter(productosList, { producto ->
            editarProducto(producto)
        }, { producto ->
            eliminarProducto(producto)
        })
        binding.lisMenuProductos.adapter = productosAdapter
        binding.lisMenuProductos.divider = null
    }

    private fun setupSearchBar() {
        binding.searchBar.editText?.addTextChangedListener { editable ->
            filtrarProductos(editable?.toString())
        }
    }

    private fun filtrarProductos(query: String?) {
        if (query.isNullOrEmpty()) {
            productosList.clear()
            productosList.addAll(productosListOriginal)
        } else {
            val filteredList = productosListOriginal.filter { producto ->
                producto.nombre?.lowercase()?.contains(query.lowercase()) == true ||
                producto.codigoInterno?.lowercase()?.contains(query.lowercase()) == true
            }
            productosList.clear()
            productosList.addAll(filteredList)
        }
        productosAdapter.updateList(productosList)
    }

    private fun setupListeners() {
        binding.btnAddProducto.setOnClickListener {
            UtilFragment.changeFragment(requireContext(), AddProductoEsteticaFragment(), TAG)
        }
        
        binding.lisMenuProductos.setOnItemClickListener { _, _, position, _ ->
            editarProducto(productosList[position])
        }
    }

    private fun cargarProductos() {
        ConfigLoading.showLoadingAnimation()
        
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                
                productosList.clear()
                productosListOriginal.clear()
                for (productoSnapshot in snapshot.children) {
                    val producto = productoSnapshot.getValue(ProductoEsteticaModel::class.java)
                    producto?.let { 
                        productosList.add(it)
                        productosListOriginal.add(it)
                    }
                }
                productosAdapter.updateList(productosList)

                if (productosList.size > 0) {
                    requireActivity().title = "${getString(R.string.submenu_productos_estetica)} (${productosList.size})"
                    ConfigLoading.hideLoadingAnimation()
                } else {
                    ConfigLoading.showNodata()
                    binding.fragmentNoData.tvNoDataMessage.text = "No hay productos de estética registrados. Agrega uno nuevo con el botón inferior."
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded) return
                ConfigLoading.showNodata()
                binding.fragmentNoData.tvNoDataMessage.text = "Error al cargar productos de estética: ${error.message}"
                DialogMaterialHelper.mostrarErrorDialog(
                    requireActivity(),
                    "Error al cargar los productos: ${error.message}"
                )
            }
        }
        
        valueEventListener?.let {
            FirebaseProductoEsteticaUtil.obtenerListaProductosEstetica(it)
        }
    }

    private fun editarProducto(producto: ProductoEsteticaModel) {
        val fragment = AddProductoEsteticaFragment.newInstance(producto)
        UtilFragment.changeFragment(requireContext(), fragment, TAG)
    }

    private fun eliminarProducto(producto: ProductoEsteticaModel) {
        DialogMaterialHelper.mostrarConfirmDeleteDialog(requireActivity(), "¿Eliminar producto?") { confirmed ->
            if (confirmed) {
                FirebaseProductoEsteticaUtil.eliminarProductoEstetica(producto.id)
                    .addOnSuccessListener {
                        cargarProductos()
                        DialogMaterialHelper.mostrarSuccessDialog(requireActivity(), "Producto eliminado correctamente")
                    }
                    .addOnFailureListener { e ->
                        DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error: ${e.message}")
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
                    UtilFragment.changeFragment(requireContext(), MenuProductosFragment(), TAG)
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        valueEventListener?.let {
            FirebaseProductoEsteticaUtil.removerListener(it)
        }
        _binding = null
    }
} 