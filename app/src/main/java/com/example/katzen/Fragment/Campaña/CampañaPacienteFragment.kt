package com.example.katzen.Fragment.Campaña

import PacienteModel
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.katzen.Adapter.Paciente.PacienteListAdapter
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseCampañaUtil
import com.example.katzen.DataBaseFirebase.FirebasePacienteUtil
import com.example.katzen.Fragment.Paciente.EditarPacienteFragment
import com.example.katzen.Fragment.Paciente.PacienteDetalleFragment
import com.example.katzen.Helper.UtilFragment
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.CampaniaEventoFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CampañaPacienteFragment : Fragment() {
    private val TAG: String = "CampañaFragment"

    private var _binding: CampaniaEventoFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var pacienteList: MutableList<PacienteModel>
    private lateinit var pacienteListAdapter: PacienteListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CampaniaEventoFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        requireActivity().title = getString(R.string.menu_campaña)

        initLoading()
        init()
        listeners()

        lifecycleScope.launch {
            obtenerPacientes()
        }

        return root
    }

    private fun init() {
        ConfigLoading.showLoadingAnimation()

        binding.btnAddPacienteCampaA.visibility = View.VISIBLE
        binding.btnAddCampania.visibility = View.GONE
        pacienteList = mutableListOf()
        pacienteListAdapter = PacienteListAdapter(requireActivity(), pacienteList)
        binding.lisMenuMascota.adapter = pacienteListAdapter
        binding.lisMenuMascota.divider = null
        PacienteListAdapter.FLAG_IN_PACIENTE = false
    }

    private fun listeners() {
        binding.buscarMascota.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterPacientes(newText.toString())
                return true
            }
        })

        binding.lisMenuMascota.setOnItemClickListener { _, _, i, _ ->
            PacienteListAdapter.FLAG_IN_PACIENTE = false
            EditarPacienteFragment.PACIENTE_EDIT = pacienteListAdapter.getItem(i)!!
            UtilFragment.changeFragment(requireActivity(), PacienteDetalleFragment(), TAG)
        }

        binding.btnAddCampania.setOnClickListener {
            UtilFragment.changeFragment(requireContext(), AddCampañaFragment(), TAG)
        }

        binding.btnAddPacienteCampaA.setOnClickListener {
            CampañaFragment.ADD_CAMPAÑA.nombreCliente = ""
            CampañaFragment.ADD_CAMPAÑA.nombrePaciente = ""
            UtilFragment.changeFragment(requireContext(), AddPacienteCampañaFragment(), TAG)
        }

        binding.btnPDF.setOnClickListener {

        }
    }

    private fun initLoading() {
        ConfigLoading.LOTTIE_ANIMATION_VIEW = binding.lottieAnimationView
        ConfigLoading.CONT_ADD_PRODUCTO = binding.contAddProducto
        ConfigLoading.FRAGMENT_NO_DATA = binding.fragmentNoData.contNoData
    }

    private fun filterPacientes(text: String) {
        val filteredList = pacienteList.filter { paciente ->
            paciente.nombre.contains(text, ignoreCase = true)
        }
        pacienteListAdapter.updateList(filteredList)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private suspend fun obtenerPacientes() {
        try {
            val pacientes = FirebaseCampañaUtil.obtenerListaPacientes(CampañaFragment.ADD_CAMPAÑA)

            val pacienteDeferredList = withContext(Dispatchers.IO) {
                pacientes.map { paciente ->
                    async {
                        val pacienteCompleto = FirebasePacienteUtil.obtenerPacientePorId(paciente.idPaciente)
                        pacienteCompleto
                    }
                }
            }

            val pacientesCompletos = pacienteDeferredList.awaitAll()
            pacienteList.clear()
            pacienteList.addAll(pacientesCompletos.filterNotNull())

            withContext(Dispatchers.Main) {
                pacienteListAdapter.notifyDataSetChanged()
                if (pacienteList.isNotEmpty()) {
                    ConfigLoading.hideLoadingAnimation()
                } else {
                    ConfigLoading.showNodata()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener pacientes: ${e.message}")
            withContext(Dispatchers.Main) {
                ConfigLoading.showNodata()
                // Muestra un mensaje de error si es necesario.
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                UtilFragment.changeFragment(requireContext(), CampañaEventoFragment(), TAG)
            }
        })
    }
}
