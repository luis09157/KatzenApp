package com.example.katzen.Fragment.Cliente

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.Cliente.ClienteAdapter
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseClienteUtil
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.MenuFragment
import com.example.katzen.Model.ClienteModel
import com.example.katzen.R
import com.example.katzen.databinding.ClienteFragmentBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ClienteFragment : Fragment() {
    val TAG : String  = "ClienteFragment"

    private var _binding: ClienteFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var clientesList: MutableList<ClienteModel>
    private lateinit var clientesAdapter: ClienteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ClienteFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        requireActivity().title = getString(R.string.menu_cliente)

        initLoading()
        init()
        listeners()

        return root
    }

    fun init(){
        ConfigLoading.showLoadingAnimation()
        clientesList = mutableListOf()
        clientesAdapter = ClienteAdapter(requireActivity(), clientesList)
        binding.lisMenuClientes.adapter = clientesAdapter
        binding.lisMenuClientes.divider = null
        binding.lisMenuClientes.setOnItemClickListener { adapterView, view, i, l ->
            EditClienteFragment.CLIENTE_EDIT = ClienteModel()
            EditClienteFragment.CLIENTE_EDIT = clientesAdapter.getItem(i)!!

            UtilFragment.changeFragment(requireActivity() , ClienteDetalleFragment() ,TAG)
        }

        obtenerClientes()
    }
     fun filterClientes(text: String) {
        val filteredList = clientesList.filter { cliente ->
            val fullName = "${cliente.nombre} ${cliente.apellidoPaterno} ${cliente.apellidoMaterno}"
            fullName.contains(text, ignoreCase = true)
        }
         clientesAdapter.updateList(filteredList)
         clientesAdapter.notifyDataSetChanged()
    }
    fun listeners(){
        binding.btnAddCliente.setOnClickListener {
            UtilFragment.changeFragment(requireActivity(), AddClienteFragment(),TAG)
        }
        binding.buscarCliente.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // No se necesita implementación aquí, ya que filtramos a medida que el usuario escribe
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Aplicar el filtro del adaptador al escribir en el SearchView
                filterClientes(newText.toString())
                return true
            }
        })
    }
    fun initLoading(){
        ConfigLoading.LOTTIE_ANIMATION_VIEW = binding.lottieAnimationView
        ConfigLoading.CONT_ADD_PRODUCTO = binding.contAddProducto
        ConfigLoading.FRAGMENT_NO_DATA = binding.fragmentNoData.contNoData
    }

    fun obtenerClientes(){
        FirebaseClienteUtil.obtenerListaClientes(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Limpiar la lista antes de agregar los nuevos datos
                clientesList.clear()

                // Recorrer los datos obtenidos y agregarlos a la lista de productos
                for (productoSnapshot in snapshot.children) {
                    val producto = productoSnapshot.getValue(ClienteModel::class.java)
                    producto?.let { clientesList.add(it) }
                }

                // Notificar al adaptador que los datos han cambiado
                clientesAdapter.notifyDataSetChanged()
                if (clientesList.size > 0){
                    ConfigLoading.hideLoadingAnimation()
                }else{
                    ConfigLoading.showNodata()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                ConfigLoading.showNodata()
                // Manejar errores de la consulta a la base de datos
                // Por ejemplo, mostrar un mensaje de error
            }
        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        init()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                UtilFragment.changeFragment(requireContext() , MenuFragment() ,TAG)
            }
        })
    }

}