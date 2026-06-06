package com.example.katzen.Fragment.Seleccionadores

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.katzen.Helper.ImageLoaderHelper
import com.example.katzen.Helper.ListScrollKeys
import com.example.katzen.Helper.ListScrollStateHelper
import com.example.katzen.Helper.ListUiHelper
import com.example.katzen.Helper.SearchUiHelper
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
            seleccionClienteAdapter = SeleccionClienteAdapter(requireActivity()) { cliente ->
                handleClienteSelection(cliente)
            }
            ListUiHelper.setupVerticalList(binding.lisMenuClientes)
            binding.lisMenuClientes.adapter = seleccionClienteAdapter

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
            ListUiHelper.restoreScrollIfPending(
                ListScrollKeys.SELECCION_CLIENTES,
                binding.lisMenuClientes,
                filteredList.map { it.id }
            )
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
            SearchUiHelper.bindSearch(binding.searchBar.searchEditText) { query ->
            filterClientes(query)
        }
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
                            producto?.let { cliente ->
                                val resolvedImage = ImageLoaderHelper.resolveProfileImage(
                                    imageUrl = cliente.imageUrl,
                                    imageFileName = cliente.imageFileName,
                                    snapshot = productoSnapshot
                                )
                                cliente.imageUrl = resolvedImage.imageUrl
                                cliente.imageFileName = resolvedImage.imageFileName
                                clientesList.add(cliente)
                            }
                        }

                        // Notificar al adaptador que los datos han cambiado
                        seleccionClienteAdapter.updateList(clientesList)
                        ListUiHelper.restoreScrollIfPending(
                            ListScrollKeys.SELECCION_CLIENTES,
                            binding.lisMenuClientes,
                            clientesList.map { it.id }
                        )

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

    private fun handleClienteSelection(cliente: ClienteModel) {
        ListScrollStateHelper.saveSelection(
            ListScrollKeys.SELECCION_CLIENTES,
            binding.lisMenuClientes,
            cliente.id
        )
        val nombreCompleto = "${cliente.nombre} ${cliente.apellidoPaterno} ${cliente.apellidoMaterno}".trim()
        when (flagVentana) {
            "EDIT_PACIENTE" -> {
                EditarPacienteFragment.PACIENTE_EDIT.idCliente = cliente.id
                EditarPacienteFragment.PACIENTE_EDIT.nombreCliente = nombreCompleto
                UtilFragment.changeFragment(requireActivity(), EditarPacienteFragment(), TAG)
            }
            "ADD_PACIENTE" -> {
                AddPacienteFragment.ADD_PACIENTE.idCliente = cliente.id
                AddPacienteFragment.ADD_PACIENTE.nombreCliente = nombreCompleto
                UtilFragment.changeFragment(requireActivity(), AddPacienteFragment(), TAG)
            }
            "ADD_VIAJE" -> {
                AddViajeFragment.ADD_CLIENTE_VIAJE = cliente
                UtilFragment.changeFragment(requireActivity(), AddViajeFragment(), TAG)
            }
            "ADD_CAMPAÑA" -> {
                CampañaFragment.ADD_CAMPAÑA.idCliente = cliente.id
                CampañaFragment.ADD_CAMPAÑA.nombreCliente = nombreCompleto
                UtilFragment.changeFragment(requireActivity(), AddPacienteCampañaFragment(), TAG)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    UtilFragment.goBackOrHome(requireContext())
                }
            })
    }
}
