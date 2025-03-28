package com.example.katzen.Fragment.Seleccionadores

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.Cliente.SeleccionClienteAdapter
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseClienteUtil
import com.example.katzen.Fragment.Campaña.AddCampañaFragment
import com.example.katzen.Fragment.Campaña.AddPacienteCampañaFragment
import com.example.katzen.Fragment.Campaña.CampañaFragment
import com.example.katzen.Fragment.Cliente.AddClienteFragment
import com.example.katzen.Fragment.Paciente.AddPacienteFragment
import com.example.katzen.Fragment.Paciente.EditarPacienteFragment
import com.example.katzen.Fragment.Viajes.AddViajeFragment
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Model.ClienteModel
import com.ninodev.katzen.databinding.ClienteFragmentBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class SeleccionarClienteFragment(val flagVentana : String) : Fragment() {
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

    fun init() {
        try {
            ConfigLoading.showLoadingAnimation()
            binding.btnAddCliente.visibility = View.GONE
            clientesList = mutableListOf()
            seleccionClienteAdapter = SeleccionClienteAdapter(requireActivity(), clientesList)
            binding.lisMenuClientes.adapter = seleccionClienteAdapter
            binding.lisMenuClientes.divider = null
            binding.lisMenuClientes.setOnItemClickListener { adapterView, view, i, l ->
                try {
                    when (flagVentana) {
                        "EDIT_PACIENTE" -> {
                            EditarPacienteFragment.PACIENTE_EDIT.idCliente = seleccionClienteAdapter.getItem(i).toString()
                            EditarPacienteFragment.PACIENTE_EDIT.nombreCliente = "${seleccionClienteAdapter.getItem(i)!!.nombre} ${seleccionClienteAdapter.getItem(i)!!.apellidoPaterno} ${seleccionClienteAdapter.getItem(i)!!.apellidoMaterno}"
                            UtilFragment.changeFragment(requireActivity(), EditarPacienteFragment(), TAG)
                        }
                        "ADD_PACIENTE" -> {
                            AddPacienteFragment.ADD_PACIENTE.idCliente = seleccionClienteAdapter.getItem(i)!!.id
                            AddPacienteFragment.ADD_PACIENTE.nombreCliente = "${seleccionClienteAdapter.getItem(i)!!.nombre} ${seleccionClienteAdapter.getItem(i)!!.apellidoPaterno} ${seleccionClienteAdapter.getItem(i)!!.apellidoMaterno}"
                            UtilFragment.changeFragment(requireActivity(), AddPacienteFragment(), TAG)
                        }
                        "ADD_VIAJE" -> {
                            AddViajeFragment.ADD_CLIENTE_VIAJE = seleccionClienteAdapter.getItem(i)!!
                            UtilFragment.changeFragment(requireActivity(), AddViajeFragment(), TAG)
                        }
                        "ADD_CAMPAÑA" -> {
                            CampañaFragment.ADD_CAMPAÑA.idCliente = seleccionClienteAdapter.getItem(i)!!.id
                            CampañaFragment.ADD_CAMPAÑA.nombreCliente = "${seleccionClienteAdapter.getItem(i)!!.nombre} ${seleccionClienteAdapter.getItem(i)!!.apellidoPaterno} ${seleccionClienteAdapter.getItem(i)!!.apellidoMaterno}"
                            UtilFragment.changeFragment(requireActivity(), AddPacienteCampañaFragment(), TAG)
                        }
                        else -> {
                            // En caso de que flagVentanaEdit no sea ni true ni false
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Manejar excepción específica si es necesario
                }
            }

            obtenerClientes()
        } catch (e: Exception) {
            e.printStackTrace()
            // Manejar la excepción durante la inicialización
        }
    }

    fun filterClientes(text: String) {
        try {
            val filteredList = clientesList.filter { cliente ->
                val fullName = "${cliente.nombre} ${cliente.apellidoPaterno} ${cliente.apellidoMaterno} ${cliente.expediente}"
                fullName.contains(text, ignoreCase = true)
            }
            seleccionClienteAdapter.updateList(filteredList)
        } catch (e: Exception) {
            e.printStackTrace()
            // Manejar la excepción durante el filtrado
        }
    }

    fun listeners() {
        try {
            binding.btnAddCliente.setOnClickListener {
                UtilFragment.changeFragment(requireActivity(), AddClienteFragment(), TAG)
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
        } catch (e: Exception) {
            e.printStackTrace()
            // Manejar la excepción durante la configuración de los listeners
        }
    }

    fun initLoading() {
        ConfigLoading.init(
            binding.lottieAnimationView,
            binding.contAddProducto,
            binding.fragmentNoData.contNoData
        )
    }


    fun obtenerClientes() {
        try {
            FirebaseClienteUtil.obtenerListaClientes(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        // Limpiar la lista antes de agregar los nuevos datos
                        clientesList.clear()

                        // Recorrer los datos obtenidos y agregarlos a la lista de productos
                        for (productoSnapshot in snapshot.children) {
                            val producto = productoSnapshot.getValue(ClienteModel::class.java)
                            producto?.let { clientesList.add(it) }
                        }

                        // Notificar al adaptador que los datos han cambiado
                        seleccionClienteAdapter.notifyDataSetChanged()

                        if (clientesList.size > 0) {
                            ConfigLoading.hideLoadingAnimation()
                        } else {
                            ConfigLoading.showNodata()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Manejar la excepción durante el procesamiento de datos
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    try {
                        ConfigLoading.showNodata()
                        // Manejar errores de la consulta a la base de datos
                        // Por ejemplo, mostrar un mensaje de error
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Manejar la excepción durante la cancelación de la consulta
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            // Manejar la excepción durante la obtención de clientes
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        try {
            init()
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    try {
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
                            "ADD_CAMPAÑA" -> {
                                UtilFragment.changeFragment(requireActivity(), AddPacienteCampañaFragment(), TAG)
                            }
                            else -> {
                                // En caso de que flagVentanaEdit no sea ni true ni false
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Manejar la excepción durante la navegación
                    }
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
            // Manejar la excepción durante la configuración de onBackPressedDispatcher
        }
    }
}
