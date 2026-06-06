package com.example.katzen.Fragment.Seleccionadores

import com.example.katzen.Model.PacienteModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.Paciente.SeleccionPacienteAdapter
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebasePacienteUtil
import com.example.katzen.Fragment.Campaña.AddPacienteCampañaFragment
import com.example.katzen.Fragment.Campaña.CampañaFragment
import com.example.katzen.Fragment.Cliente.AddClienteFragment
import com.example.katzen.Fragment.Paciente.AddPacienteFragment
import com.example.katzen.Fragment.Paciente.EditarPacienteFragment
import com.example.katzen.Fragment.Viajes.AddViajeFragment
import com.example.katzen.Helper.ListScrollKeys
import com.example.katzen.Helper.ListScrollStateHelper
import com.example.katzen.Helper.ListUiHelper
import com.example.katzen.Helper.SearchUiHelper
import com.example.katzen.Helper.UtilFragment
import com.ninodev.katzen.databinding.ClienteFragmentBinding
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
        seleccionPacienteAdapter = SeleccionPacienteAdapter(requireActivity()) { paciente ->
            handlePacienteSelection(paciente)
        }
        ListUiHelper.setupVerticalList(binding.lisMenuClientes)
        binding.lisMenuClientes.adapter = seleccionPacienteAdapter

        obtenerPacientesCliente()
    }

    private fun handlePacienteSelection(paciente: PacienteModel) {
        ListScrollStateHelper.saveSelection(
            ListScrollKeys.SELECCION_PACIENTES,
            binding.lisMenuClientes,
            paciente.id
        )
        when (flagVentana) {
            "EDIT_PACIENTE" -> {
                EditarPacienteFragment.PACIENTE_EDIT.idCliente = paciente.id
                EditarPacienteFragment.PACIENTE_EDIT.nombreCliente = paciente.nombre
                UtilFragment.changeFragment(requireActivity(), EditarPacienteFragment(), TAG)
            }
            "ADD_PACIENTE" -> {
                AddPacienteFragment.ADD_PACIENTE.idCliente = paciente.id
                AddPacienteFragment.ADD_PACIENTE.nombreCliente = paciente.nombre
                UtilFragment.changeFragment(requireActivity(), AddPacienteFragment(), TAG)
            }
            "ADD_VIAJE" -> {
                UtilFragment.changeFragment(requireActivity(), AddViajeFragment(), TAG)
            }
            "ADD_CAMPAÑA" -> {
                CampañaFragment.ADD_CAMPAÑA.idPaciente = paciente.id
                CampañaFragment.ADD_CAMPAÑA.nombrePaciente = paciente.nombre
                UtilFragment.changeFragment(requireActivity(), AddPacienteCampañaFragment(), TAG)
            }
        }
    }

    fun filterClientes(text: String) {
        val filteredList = pacientesList.filter { paciente ->
            val fullName = "${paciente.nombre}"
            fullName.contains(text, ignoreCase = true)
        }
        seleccionPacienteAdapter.updateList(filteredList)
        ListUiHelper.restoreScrollIfPending(
            ListScrollKeys.SELECCION_PACIENTES,
            binding.lisMenuClientes,
            filteredList.map { it.id }
        )
    }

    fun listeners() {
        binding.btnAddCliente.setOnClickListener {
            UtilFragment.changeFragment(requireActivity(), AddClienteFragment(), TAG)
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


    fun obtenerPacientesCliente() {
        // Using Coroutines to call suspend function
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val pacientes = FirebasePacienteUtil.obtenerPacientesDeCliente(CampañaFragment.ADD_CAMPAÑA.idCliente) // Replace "client_id" with the actual client ID
                pacientesList.clear()
                pacientesList.addAll(pacientes)
                seleccionPacienteAdapter.updateList(pacientesList)
                ListUiHelper.restoreScrollIfPending(
                    ListScrollKeys.SELECCION_PACIENTES,
                    binding.lisMenuClientes,
                    pacientesList.map { it.id }
                )

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
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    UtilFragment.goBackOrHome(requireContext())
                }
            })
    }
}
