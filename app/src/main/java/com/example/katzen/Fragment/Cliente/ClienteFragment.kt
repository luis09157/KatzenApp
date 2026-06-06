package com.example.katzen.Fragment.Cliente

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.Cliente.ClienteAdapter
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseClienteUtil
import com.example.katzen.Helper.RtdbActiveRecords
import com.example.katzen.Helper.FirebaseUiHelper
import com.example.katzen.Helper.ListScrollKeys
import com.example.katzen.Helper.ListUiHelper
import com.example.katzen.Helper.SearchUiHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Model.ClienteModel
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.ClienteFragmentBinding
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
        setupScreenHeader()
        init()
        listeners()

        return root
    }

    private fun setupScreenHeader() {
        binding.screenHeader.tvHeaderTitle.text = getString(R.string.staff_header_clientes_title)
        binding.screenHeader.tvHeaderSubtitle.text = getString(R.string.staff_header_clientes_sub)
        binding.screenHeader.imgHeaderIcon.setImageResource(R.drawable.ic_person)
    }

    fun init(){
        ConfigLoading.showLoadingAnimation()
        clientesList = mutableListOf()
        clientesAdapter = ClienteAdapter(requireActivity()) { cliente ->
            EditClienteFragment.CLIENTE_EDIT = cliente
            UtilFragment.changeFragment(
                requireActivity(),
                ClienteDetalleFragment(),
                TAG,
                listKey = ListScrollKeys.CLIENTES,
                listRecyclerView = binding.lisMenuClientes,
                selectedItemId = cliente.id
            )
        }
        ListUiHelper.setupVerticalList(binding.lisMenuClientes)
        binding.lisMenuClientes.adapter = clientesAdapter

        obtenerClientes()
    }
     fun filterClientes(text: String) {
        val filteredList = clientesList.filter { cliente ->
            val fullName = "${cliente.nombre} ${cliente.apellidoPaterno} ${cliente.apellidoMaterno} ${cliente.expediente}"
            fullName.contains(text, ignoreCase = true)
        }
         clientesAdapter.updateList(filteredList)
         ListUiHelper.restoreScrollIfPending(
             ListScrollKeys.CLIENTES,
             binding.lisMenuClientes,
             filteredList.map { it.id }
         )
    }
    fun listeners(){
        binding.btnAddCliente.setOnClickListener {
            UtilFragment.changeFragment(requireActivity(), AddClienteFragment(),TAG)
        }
        SearchUiHelper.bindSearch(binding.searchBar.searchEditText) { query ->
            filterClientes(query)
        }
    }
    fun initLoading() {
        ConfigLoading.init(
            binding.lottieAnimationView,
            binding.contAddProducto,
            binding.fragmentNoData.contNoData
        )
    }


    fun obtenerClientes(){
        FirebaseClienteUtil.obtenerListaClientesUnaVez(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Limpiar la lista antes de agregar los nuevos datos
                clientesList.clear()

                // Recorrer los datos obtenidos y agregarlos a la lista de clientes
                for (productoSnapshot in snapshot.children) {
                    try {
                        if (!RtdbActiveRecords.isActive(productoSnapshot)) continue
                        val cliente = FirebaseClienteUtil.parseCliente(productoSnapshot)
                        if (cliente != null && cliente.id.isNotBlank()) {
                            clientesList.add(cliente)
                        }
                    } catch (_: Exception) {
                    }
                }

                // Notificar al adaptador que los datos han cambiado
                clientesAdapter.updateList(clientesList)
                ListUiHelper.restoreScrollIfPending(
                    ListScrollKeys.CLIENTES,
                    binding.lisMenuClientes,
                    clientesList.map { it.id }
                )
                if (clientesList.size > 0){
                    requireActivity().title = "${getString(R.string.menu_cliente)} (${clientesList.size})"
                    ConfigLoading.hideLoadingAnimation()
                }else{
                    ConfigLoading.showNodata()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                FirebaseUiHelper.handleLoadError(requireContext(), error) {
                    obtenerClientes()
                }
            }
        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                UtilFragment.goHome(requireContext())
            }
        })
    }

}