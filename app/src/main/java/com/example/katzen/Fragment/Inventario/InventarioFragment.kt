package com.example.katzen.Fragment.Inventario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.ProductoInventarioAdapter
import com.example.katzen.DataBaseFirebase.FirebaseInventarioUtil
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Model.InventarioModel
import com.example.katzen.Model.ProductoModel
import com.example.katzen.databinding.InventarioFragmentBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class InventarioFragment : Fragment() {
    val TAG : String  = "InventarioFragment"

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

        init()

        binding.menuListProductoInventario.setOnItemClickListener { adapterView, view, i, l ->
            var addProductoInventarioFragment = AddProductoInventarioFragment()
            addProductoInventarioFragment.setProducto(productosList.get(i))
            UtilFragment.changeFragment(requireContext(),addProductoInventarioFragment,TAG)
        }

        return root
    }

    fun init(){
        productosList = mutableListOf()
        productosAdapter = ProductoInventarioAdapter(requireContext(), productosList)
        binding.menuListProductoInventario.adapter = productosAdapter
        obtenerProductos()

    }

    fun obtenerInventario(productoModel: ProductoModel, onComplete: (ProductoModel) -> Unit) {
        FirebaseInventarioUtil.obtenerInventarioPorProducto(productoModel.id) { inventarioList ->
            // Actualizar el productoModel con la cantidad de inventario
            for (inventario in inventarioList) {
                println("Cantidad: ${inventario.cantidad}, Fecha: ${inventario.fecha}, Unidad de Medida: ${inventario.unidadMedida}")
                productoModel.cantidadInventario += inventario.cantidad
            }
            // Llamar al onComplete con el productoModel actualizado
            onComplete(productoModel)
        }
    }
    fun obtenerProductos(){
        FirebaseProductoUtil.obtenerListaProductos(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Limpiar la lista antes de agregar los nuevos datos
                productosList.clear()

                // Recorrer los datos obtenidos y agregarlos a la lista de productos
                for (productoSnapshot in snapshot.children) {
                    var producto = productoSnapshot.getValue(ProductoModel::class.java)

                    obtenerInventario(producto!!) { productoActualizado ->
                        // Aquí puedes trabajar con el producto actualizado, como agregarlo a la lista de productos
                        productosList.add(productoActualizado)
                        // Asegúrate de notificar al adaptador después de agregar el producto a la lista
                        productosAdapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
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