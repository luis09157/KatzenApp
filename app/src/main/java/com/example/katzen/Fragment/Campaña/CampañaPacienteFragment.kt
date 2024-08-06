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
import com.example.katzen.Adapter.Paciente.PacienteAdapter
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseCampañaUtil
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.MenuFragment
import com.example.katzen.R
import com.example.katzen.databinding.CampaniaEventoFragmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CampañaPacienteFragment : Fragment() {
    private val TAG: String = "CampañaFragment"

    private var _binding: CampaniaEventoFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var pacienteList: MutableList<PacienteModel>
    private lateinit var pacienteListAdapter: PacienteAdapter

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

        CoroutineScope(Dispatchers.Main).launch {
            obtenerPacientes()
        }

        return root
    }

    private fun init() {
        ConfigLoading.hideLoadingAnimation()

        pacienteList = mutableListOf()
        pacienteListAdapter = PacienteAdapter(requireActivity(), pacienteList)
        binding.lisMenuMascota.adapter = pacienteListAdapter
        binding.lisMenuMascota.divider = null
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
        binding.lisMenuMascota.setOnItemClickListener { adapterView, view, i, l ->

        }
        binding.btnAddCampania.setOnClickListener {
            UtilFragment.changeFragment(requireContext(), AddCampañaFragment(), TAG)
        }

        binding.btnAddPacienteCampaA.setOnClickListener {
            UtilFragment.changeFragment(requireContext(), AddPacienteCampañaFragment(), TAG)
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
        val campaña = // obtener campaña actual de algún modo (puede ser a través de argumentos o de una variable global)
            FirebaseCampañaUtil.obtenerPacientesCampaña(CampañaFragment.ADD_CAMPAÑA).let { pacientes ->
                withContext(Dispatchers.Main) {
                   /* pacienteList.clear()
                    pacienteList.addAll(pacientes)
                    pacienteListAdapter.notifyDataSetChanged()*/

                    ConfigLoading.hideLoadingAnimation()
                }
            }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                UtilFragment.changeFragment(requireContext(), MenuFragment(), TAG)
            }
        })
    }
}
