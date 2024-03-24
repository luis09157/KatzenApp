import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.katzen.Model.Producto
import com.example.katzen.databinding.MenuProductosFragmnetBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class MenuProductosFragment : Fragment() {
    private var _binding: MenuProductosFragmnetBinding? = null
    private val binding get() = _binding!!

    private lateinit var productosAdapter: ProductosAdapter
    private lateinit var productosList: MutableList<Producto>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MenuProductosFragmnetBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Inicializar la lista de productos y el adaptador
        productosList = mutableListOf()
        productosAdapter = ProductosAdapter(requireContext(), productosList)
        binding.lisMenuProductos.adapter = productosAdapter
        binding.lisMenuProductos.divider = null

        // Obtener la lista de productos desde Firebase
        FirebaseProductoUtil.obtenerListaProductos(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Limpiar la lista antes de agregar los nuevos datos
                productosList.clear()

                // Recorrer los datos obtenidos y agregarlos a la lista de productos
                for (productoSnapshot in snapshot.children) {
                    val producto = productoSnapshot.getValue(Producto::class.java)
                    producto?.let { productosList.add(it) }
                }

                // Notificar al adaptador que los datos han cambiado
                productosAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar errores de la consulta a la base de datos
                // Por ejemplo, mostrar un mensaje de error
            }
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
