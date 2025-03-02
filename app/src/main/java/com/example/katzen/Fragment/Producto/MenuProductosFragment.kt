package com.example.katzen.Fragment.Producto

import FirebaseProductoUtil
import com.example.katzen.Adapter.Producto.ProductosAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Model.InventarioModel
import com.example.katzen.Model.ProductoModel
import com.ninodev.katzen.databinding.MenuProductosFragmnetBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class MenuProductosFragment : Fragment() {
    val TAG : String  = "MenuProductosFragment"
    private var _binding: MenuProductosFragmnetBinding? = null
    private val binding get() = _binding!!

    private lateinit var productosAdapter: ProductosAdapter
    private lateinit var productosList: MutableList<ProductoModel>
    private lateinit var inventarioList: MutableList<InventarioModel>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MenuProductosFragmnetBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initLoading()
        ConfigLoading.showLoadingAnimation()
        // Inicializar la lista de productos y el adaptador
        productosList = mutableListOf()
        productosAdapter = ProductosAdapter(requireContext(), productosList)
        binding.lisMenuProductos.adapter = productosAdapter
        binding.lisMenuProductos.divider = null

        obtenerProductos()

        binding.lisMenuProductos.setOnItemClickListener { adapterView, view, position, id ->
            // Obtener el producto seleccionado de la lista
            val productoSeleccionado = productosList[position]

            // Obtener información específica del producto desde Firebase
            FirebaseProductoUtil.obtenerProducto(
                productoSeleccionado.id,
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // Verificar si existen datos
                        if (snapshot.exists()) {
                            // Obtener el producto específico de la base de datos
                            val producto = snapshot.getValue(ProductoModel::class.java)
                            var addProductoFragment = AddProductoFragment()
                            addProductoFragment.setProducto(producto!!)
                            UtilFragment.changeFragment(requireContext(), addProductoFragment, TAG)


                            // Aquí puedes manejar los datos del producto como desees
                        } else {
                            // No se encontraron datos para el producto seleccionado
                            // Manejar el caso según sea necesario
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Manejar errores de la consulta a la base de datos
                        // Por ejemplo, mostrar un mensaje de error
                    }
                })
        }

        return root
    }

    fun obtenerProductos(){
        FirebaseProductoUtil.obtenerListaProductos(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Limpiar la lista antes de agregar los nuevos datos
                productosList.clear()

                // Recorrer los datos obtenidos y agregarlos a la lista de productos
                for (productoSnapshot in snapshot.children) {
                    val producto = productoSnapshot.getValue(ProductoModel::class.java)
                    producto?.let { productosList.add(it) }
                }

                // Notificar al adaptador que los datos han cambiado
                productosAdapter.notifyDataSetChanged()
                ConfigLoading.hideLoadingAnimation()
            }

            override fun onCancelled(error: DatabaseError) {
                ConfigLoading.hideLoadingAnimation()
                // Manejar errores de la consulta a la base de datos
                // Por ejemplo, mostrar un mensaje de error
            }
        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    fun initLoading(){
        ConfigLoading.LOTTIE_ANIMATION_VIEW = binding.lottieAnimationView
        ConfigLoading.CONT_ADD_PRODUCTO = binding.contAddProducto
        ConfigLoading.FRAGMENT_NO_DATA = binding.fragmentNoData.contNoData
    }
}
