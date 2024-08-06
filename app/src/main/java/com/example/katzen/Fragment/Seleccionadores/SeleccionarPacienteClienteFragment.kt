package com.example.katzen.Fragment.Seleccionadores

import PacienteModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.Cliente.SeleccionClienteAdapter
import com.example.katzen.Adapter.Paciente.SeleccionPacienteAdapter
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebasePacienteUtil
import com.example.katzen.Fragment.Campaña.AddPacienteCampañaFragment
import com.example.katzen.Fragment.Campaña.CampañaFragment
import com.example.katzen.Fragment.Cliente.AddClienteFragment
import com.example.katzen.Fragment.Paciente.AddPacienteFragment
import com.example.katzen.Fragment.Paciente.EditarPacienteFragment
import com.example.katzen.Fragment.Viajes.AddViajeFragment
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Model.ClienteModel
import com.example.katzen.databinding.ClienteFragmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SeleccionarPacienteClienteFragment(val flagVentana: String) : Fragment() {
    val TAG: String = "ClienteFragment"

    private var _binding: ClienteFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var pacientesList: MutableList<PacienteModel>
    private lateinit var seleccionPacienteAdapter : SeleccionPacienteAdapter

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
        ConfigLoading.showLoadingAnimation()
        binding.btnAddCliente.visibility = View.GONE
        pacientesList = mutableListOf()
        seleccionPacienteAdapter = SeleccionPacienteAdapter(requireActivity(), pacientesList)
        binding.lisMenuClientes.adapter = seleccionPacienteAdapter
        binding.lisMenuClientes.divider = null
        binding.lisMenuClientes.setOnItemClickListener { _, _, i, _ ->

            val selectedCliente = seleccionPacienteAdapter.getItem(i)
            when (flagVentana) {
                "EDIT_PACIENTE" -> {
                    EditarPacienteFragment.PACIENTE_EDIT.idCliente = selectedCliente?.id ?: ""
                    EditarPacienteFragment.PACIENTE_EDIT.nombreCliente = "${selectedCliente?.nombre ?: ""}"
                    UtilFragment.changeFragment(requireActivity(), EditarPacienteFragment(), TAG)
                }
                "ADD_PACIENTE" -> {
                    AddPacienteFragment.ADD_PACIENTE.idCliente = selectedCliente?.id ?: ""
                    AddPacienteFragment.ADD_PACIENTE.nombreCliente = "${selectedCliente?.nombre ?: ""}"
                    UtilFragment.changeFragment(requireActivity(), AddPacienteFragment(), TAG)
                }
                "ADD_VIAJE" -> {
                    // AddViajeFragment.ADD_CLIENTE_VIAJE = selectedCliente!!
                    UtilFragment.changeFragment(requireActivity(), AddViajeFragment(), TAG)
                }
                "ADD_CAMPAÑA" -> {
                    CampañaFragment.ADD_CAMPAÑA.idPaciente = selectedCliente?.id ?: ""
                    CampañaFragment.ADD_CAMPAÑA.nombrePaciente = "${selectedCliente?.nombre ?: ""}"
                    UtilFragment.changeFragment(requireActivity(), AddPacienteCampañaFragment(), TAG)
                }
                else -> {
                    // En caso de que flagVentana no sea ninguno de los casos anteriores
                }
            }
        }

        obtenerPacientesCliente()
    }

    fun filterClientes(text: String) {
        val filteredList = pacientesList.filter { paciente ->
            val fullName = "${paciente.nombre}"
            fullName.contains(text, ignoreCase = true)
        }
        seleccionPacienteAdapter.updateList(filteredList)
    }

    fun listeners() {
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
    }

    fun initLoading() {
        ConfigLoading.LOTTIE_ANIMATION_VIEW = binding.lottieAnimationView
        ConfigLoading.CONT_ADD_PRODUCTO = binding.contAddProducto
        ConfigLoading.FRAGMENT_NO_DATA = binding.fragmentNoData.contNoData
    }

    fun obtenerPacientesCliente() {
        // Using Coroutines to call suspend function
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val pacientes = FirebasePacienteUtil.obtenerPacientesDeCliente(CampañaFragment.ADD_CAMPAÑA.idCliente) // Replace "client_id" with the actual client ID
                pacientesList.clear()
                pacientesList.addAll(pacientes)
                seleccionPacienteAdapter.notifyDataSetChanged()

                if (pacientesList.isNotEmpty()) {
                    ConfigLoading.hideLoadingAnimation()
                } else {
                    ConfigLoading.showNodata()
                }
            } catch (e: Exception) {
                ConfigLoading.showNodata()
                println("Error al obtener la lista de pacientes: ${e.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
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
                    "ADD_CAMPAÑA" -> {
                        UtilFragment.changeFragment(requireActivity(), AddPacienteCampañaFragment(), TAG)
                    }
                    else -> {
                        // En caso de que flagVentana no sea ninguno de los casos anteriores
                    }
                }
            }
        })
    }
}
