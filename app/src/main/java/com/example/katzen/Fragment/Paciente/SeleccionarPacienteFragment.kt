package com.example.katzen.Fragment.Paciente

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.Cliente.SeleccionClienteAdapter
import com.example.katzen.Config.Config
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseClienteUtil
import com.example.katzen.Fragment.Cliente.AddClienteFragment
import com.example.katzen.Fragment.Cliente.EditClienteFragment
import com.example.katzen.Fragment.Viajes.AddViajeFragment
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.MenuFragment
import com.example.katzen.Model.ClienteModel
import com.example.katzen.databinding.ClienteFragmentBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class SeleccionarPacienteFragment(val flagVentana : String) : Fragment( ) {
    val TAG : String  = "ClienteFragment"

    private var _binding: ClienteFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var clientesList: MutableList<ClienteModel>
    private lateinit var seleccionClienteAdapter: SeleccionClienteAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ClienteFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root


        requireActivity().title = "Selecciona el cliente"

        initLoading()
        init()
        listeners()

        return root
    }

    fun init(){
        ConfigLoading.showLoadingAnimation()
        binding.btnAddCliente.visibility = View.GONE
        clientesList = mutableListOf()
        seleccionClienteAdapter = SeleccionClienteAdapter(requireActivity(), clientesList)
        binding.lisMenuClientes.adapter = seleccionClienteAdapter
        binding.lisMenuClientes.divider = null
        binding.lisMenuClientes.setOnItemClickListener { adapterView, view, i, l ->

                when (flagVentana) {
                    "EDIT_PACIENTE" -> {
                        EditarPacienteFragment.PACIENTE_EDIT.idCliente = clientesList[i].id
                        EditarPacienteFragment.PACIENTE_EDIT.nombreCliente = "${clientesList[i].nombre} ${clientesList[i].apellidoPaterno} ${clientesList[i].apellidoMaterno}"
                        UtilFragment.changeFragment(requireActivity(), EditarPacienteFragment(), TAG)
                    }
                    "ADD_PACIENTE" -> {
                        AddPacienteFragment.ADD_PACIENTE.idCliente = clientesList[i].id
                        AddPacienteFragment.ADD_PACIENTE.nombreCliente = "${clientesList[i].nombre} ${clientesList[i].apellidoPaterno} ${clientesList[i].apellidoMaterno}"
                        UtilFragment.changeFragment(requireActivity(), AddPacienteFragment(), TAG)
                    }
                    "ADD_VIAJE" -> {
                        AddViajeFragment.ADD_CLIENTE_VIAJE = clientesList[i]
                        UtilFragment.changeFragment(requireActivity(), AddViajeFragment(), TAG)
                    }
                    else -> {
                        // En caso de que flagVentanaEdit no sea ni true ni false
                    }
                }


        }

        obtenerClientes()
    }
    fun filterClientes(text: String) {
        val filteredList = clientesList.filter { cliente ->
            val fullName = "${cliente.nombre} ${cliente.apellidoPaterno} ${cliente.apellidoMaterno}"
            fullName.contains(text, ignoreCase = true)
        }
        seleccionClienteAdapter.updateList(filteredList)
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
                seleccionClienteAdapter.notifyDataSetChanged()

                if(clientesList.size > 0){
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
                when (flagVentana) {
                    "EDIT_PACIENTE" -> {
                        UtilFragment.changeFragment(requireActivity(), EditarPacienteFragment(), TAG)
                    }
                    "ADD_PACIENTE" -> {
                        UtilFragment.changeFragment(requireActivity(), AddPacienteFragment(), TAG)
                    }
                    "ADD_VIAJE" -> {
                        UtilFragment.changeFragment(requireActivity(), AddViajeFragment(), TAG)
                    }
                    else -> {
                        // En caso de que flagVentanaEdit no sea ni true ni false
                    }
                }

            }
        })
    }

}