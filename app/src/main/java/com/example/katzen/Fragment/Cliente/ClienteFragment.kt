package com.example.katzen.Fragment.Cliente

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.ClienteAdapter
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseClienteUtil
import com.example.katzen.Helper.LoadingHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Model.ClienteModel
import com.example.katzen.Model.ProductoModel
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

        initLoading()
        listeners()
        init()

        return root
    }

    fun init(){
        ConfigLoading.showLoadingAnimation()
        clientesList = mutableListOf()
        clientesAdapter = ClienteAdapter(requireContext(), clientesList)
        binding.lisMenuClientes.adapter = clientesAdapter

        obtenerClientes()
    }
    fun listeners(){
        binding.btnAddCliente.setOnClickListener {
            UtilFragment.changeFragment(requireActivity(), AddClienteFragment(),TAG)
        }
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