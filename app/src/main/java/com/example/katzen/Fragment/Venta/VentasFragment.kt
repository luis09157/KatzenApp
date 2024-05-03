package com.example.katzen.Fragment.Venta

import com.example.katzen.Adapter.Producto.MenuProductosInventarioAdapter
import android.R
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.katzen.Config.Config
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.Model.ProductoModel
import com.example.katzen.databinding.VentasFragmentBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
class VentasFragment : Fragment() {
    private var _binding: VentasFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var productosAdapter: MenuProductosInventarioAdapter
    private lateinit var productosList: MutableList<ProductoModel>
    private lateinit var originalProductosList: MutableList<ProductoModel>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = VentasFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        init()
        obtenerProductos()

        return root
    }

    private fun init() {
        productosList = mutableListOf()
        originalProductosList = mutableListOf()
        productosAdapter = MenuProductosInventarioAdapter(requireContext(), productosList)
        binding.lisMenuProductos.adapter = productosAdapter

        binding.searchTextInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filterProductos(s.toString())
            }
        })

        val adapterCategorias = ArrayAdapter(requireActivity(),
            R.layout.simple_list_item_1, Config.CATEGORIAS_PRODUCTO)
        binding.spCategoria.setAdapter(adapterCategorias)
    }

    private fun filterProductos(text: String) {
        val filteredList = originalProductosList.filter { producto ->
            producto.nombre.contains(text, ignoreCase = true)
        }
        productosAdapter.updateList(filteredList)
    }

    fun obtenerProductos(){
        FirebaseProductoUtil.obtenerListaProductos(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Limpiar la lista antes de agregar los nuevos datos
                productosList.clear()
                originalProductosList.clear()

                // Recorrer los datos obtenidos y agregarlos a la lista de productos
                for (productoSnapshot in snapshot.children) {
                    val producto = productoSnapshot.getValue(ProductoModel::class.java)
                    producto?.let {
                        productosList.add(it)
                        originalProductosList.add(it)
                    }
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
}
