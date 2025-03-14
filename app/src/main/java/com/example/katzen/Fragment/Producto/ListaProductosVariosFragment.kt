package com.example.katzen.Fragment.Producto

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Config.ConfigLoading
import com.ninodev.katzen.DataBaseFirebase.FirebaseProductoVariosUtil
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilFragment
import com.ninodev.katzen.Model.ProductoVariosModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.FragmentListaProductosVariosBinding

class ListaProductosVariosFragment : Fragment() {
    private var _binding: FragmentListaProductosVariosBinding? = null
    private val binding get() = _binding!!
    private lateinit var productosAdapter: ProductosVariosAdapter
    private val productosList = mutableListOf<ProductoVariosModel>()
    private val productosListOriginal = mutableListOf<ProductoVariosModel>()
    private val TAG = "ListaProductosVariosFragment"
    private var valueEventListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListaProductosVariosBinding.inflate(inflater, container, false)
        requireActivity().title = "Productos Varios"
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
        productosAdapter = ProductosVariosAdapter(productosList, { producto ->
            editarProducto(producto)
        }, { producto ->
            eliminarProducto(producto)
        })
        binding.lisMenuProductos.adapter = productosAdapter
        binding.lisMenuProductos.divider = null
    }

    private fun setupSearchBar() {
        binding.searchBar.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filtrarProductos(s?.toString())
            }
        })
    }

    private fun filtrarProductos(query: String?) {
        if (query.isNullOrEmpty()) {
            productosList.clear()
            productosList.addAll(productosListOriginal)
        } else {
            val filteredList = productosListOriginal.filter { producto ->
                producto.nombre.lowercase().contains(query.lowercase()) ||
                producto.codigoInterno.lowercase().contains(query.lowercase())
            }
            productosList.clear()
            productosList.addAll(filteredList)
        }
        productosAdapter.notifyDataSetChanged()
    }

    private fun setupListeners() {
        binding.btnAddProducto.setOnClickListener {
            UtilFragment.changeFragment(requireContext(), AddProductoVariosFragment(), TAG)
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
                    val producto = productoSnapshot.getValue(ProductoVariosModel::class.java)
                    producto?.let { 
                        productosList.add(it)
                        productosListOriginal.add(it)
                    }
                }
                productosAdapter.notifyDataSetChanged()

                if (productosList.size > 0) {
                    requireActivity().title = "Productos Varios (${productosList.size})"
                    ConfigLoading.hideLoadingAnimation()
                } else {
                    ConfigLoading.showNodata()
                    binding.fragmentNoData.tvNoDataMessage.text = "No hay productos registrados. Agrega uno nuevo con el botón inferior."
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded) return
                ConfigLoading.showNodata()
                binding.fragmentNoData.tvNoDataMessage.text = "Error al cargar productos: ${error.message}"
                DialogMaterialHelper.mostrarErrorDialog(
                    requireActivity(),
                    "Error al cargar los productos: ${error.message}"
                )
            }
        }
        
        valueEventListener?.let {
            FirebaseProductoVariosUtil.obtenerListaProductosVarios(it)
        }
    }

    private fun editarProducto(producto: ProductoVariosModel) {
        val fragment = AddProductoVariosFragment.newInstance(producto)
        UtilFragment.changeFragment(requireContext(), fragment, TAG)
    }

    private fun eliminarProducto(producto: ProductoVariosModel) {
        DialogMaterialHelper.mostrarConfirmDeleteDialog(requireActivity(), "¿Eliminar producto?") { confirmed ->
            if (confirmed) {
                FirebaseProductoVariosUtil.eliminarProductoVarios(producto.id)
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
            FirebaseProductoVariosUtil.removerListener(it)
        }
        _binding = null
    }
    
    // Adapter interno para la lista de productos
    inner class ProductosVariosAdapter(
        private var productosList: List<ProductoVariosModel>,
        private val onItemClick: (ProductoVariosModel) -> Unit,
        private val onDeleteClick: (ProductoVariosModel) -> Unit
    ) : BaseAdapter() {

        override fun getCount(): Int = productosList.size

        override fun getItem(position: Int): Any = productosList[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(parent.context)
                .inflate(R.layout.item_producto_varios, parent, false)
            
            val producto = productosList[position]
            
            val tvNombre: TextView = view.findViewById(R.id.tvNombre)
            val tvPrecio: TextView = view.findViewById(R.id.tvPrecio)
            val tvCodigo: TextView = view.findViewById(R.id.tvCodigo)
            val btnEstado: Button = view.findViewById(R.id.btnEstado)
            val btnEliminar: LinearLayout = view.findViewById(R.id.btnEliminar)
            
            tvNombre.text = producto.nombre
            tvPrecio.text = "Precio: $${producto.precioFinal}"
            tvCodigo.text = "Código: ${producto.codigoInterno}"
            
            btnEstado.text = if (producto.activo) "Activo" else "Inactivo"
            btnEstado.backgroundTintList = view.context.getColorStateList(
                if (producto.activo) R.color.green_300 else R.color.grey_300
            )
            
            btnEliminar.setOnClickListener {
                onDeleteClick(producto)
            }
            
            view.setOnClickListener {
                onItemClick(producto)
            }
            
            return view
        }
    }
} 