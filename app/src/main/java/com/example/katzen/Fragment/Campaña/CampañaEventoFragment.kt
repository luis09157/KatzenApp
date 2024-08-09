package com.example.katzen.Fragment.Campaña

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.Campaña.CampañaEventoAdapter
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseCampañaUtil
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.MenuFragment
import com.example.katzen.Model.CampañaModel
import com.example.katzen.R
import com.example.katzen.databinding.CampaniaEventoFragmentBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CampañaEventoFragment : Fragment() {
    private val TAG: String = "CampañaFragment"

    private var _binding: CampaniaEventoFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var campañaList: MutableList<CampañaModel>
    private lateinit var campañaListAdapter: CampañaEventoAdapter

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

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Llamamos a la función initCampañas y a obtenerCantidadCampañasPorMes dentro de un coroutine
        CoroutineScope(Dispatchers.Main).launch {
            //initCampañas()
            //obtenerCantidadCampañasPorMes()
        }
    }
    private fun init() {
        ConfigLoading.showLoadingAnimation()
        binding.btnAddPacienteCampaA.visibility = View.GONE
        binding.btnAddCampania.visibility = View.VISIBLE
        campañaList = mutableListOf()
        campañaListAdapter = CampañaEventoAdapter(requireActivity(), campañaList)
        binding.lisMenuMascota.adapter = campañaListAdapter
        binding.lisMenuMascota.divider = null


        obtenerCampañas()
    }
    private fun listeners() {
        binding.buscarMascota.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterMascotas(newText.toString())
                return true
            }
        })
        binding.lisMenuMascota.setOnItemClickListener { adapterView, view, i, l ->
            CampañaFragment.ADD_CAMPAÑA = campañaList.get(i)
            UtilFragment.changeFragment(requireContext(),CampañaPacienteFragment(),TAG)
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
    private fun filterMascotas(text: String) {
        val filteredList = campañaList.filter { campaña ->
            campaña.mes.contains(text, ignoreCase = true)
        }
        campañaListAdapter.updateList(filteredList)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun obtenerCampañas() {
        FirebaseCampañaUtil.obtenerListaCampañas(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val campañas = mutableListOf<CampañaModel>()

                CoroutineScope(Dispatchers.Main).launch {
                    for (campañaSnapshot in snapshot.children) {
                        val campaña = campañaSnapshot.getValue(CampañaModel::class.java)
                        campaña?.let {
                            try {
                                it.año = CampañaFragment.ADD_CAMPAÑA.año
                                it.mes = CampañaFragment.ADD_CAMPAÑA.mes
                                it.cantidadPacientes = FirebaseCampañaUtil.obtenerCantidadPacientes(it).toString()
                                campañas.add(it)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error al obtener cantidad de pacientes: ${e.message}")
                            }
                        }
                    }

                    requireActivity().runOnUiThread {
                        campañaList.clear()
                        campañaList.addAll(campañas)
                        campañaListAdapter.notifyDataSetChanged()

                        if (campañaList.size > 0) {
                            ConfigLoading.hideLoadingAnimation()
                        }else{
                            ConfigLoading.showNodata()
                        }

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error al obtener campañas: ${error.message}")
                ConfigLoading.showNodata()
            }
        })
    }
    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                UtilFragment.changeFragment(requireContext(), CampañaFragment(), TAG)
            }
        })
    }
}
