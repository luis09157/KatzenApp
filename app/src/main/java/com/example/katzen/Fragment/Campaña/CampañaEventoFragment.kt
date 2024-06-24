package com.example.katzen.Fragment.Campaña

import android.os.Bundle
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CampañaEventoFragment : Fragment() {
    val TAG: String = "CampañaFragment"

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

        requireActivity().title = getString(R.string.menu_paciente)

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

    fun init() {
        ConfigLoading.hideLoadingAnimation()

        // Inicializar lista y adaptador
        campañaList = mutableListOf()
        campañaListAdapter = CampañaEventoAdapter(requireActivity(), campañaList)
        binding.lisMenuMascota.adapter = campañaListAdapter
        binding.lisMenuMascota.divider = null

        GlobalScope.launch(Dispatchers.Main) {
            obtenerCampañas()
        }
    }

    fun listeners() {
        binding.buscarMascota.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterMascotas(newText.toString())
                return true
            }
        })
        binding.btnAddCampania.setOnClickListener {
            UtilFragment.changeFragment(requireContext(), AddCampañaFragment(), TAG)
        }
        binding.btnAddPacienteCampaA.setOnClickListener {
            UtilFragment.changeFragment(requireContext(), AddPacienteCampañaFragment(), TAG)
        }
    }

    fun initLoading() {
        ConfigLoading.LOTTIE_ANIMATION_VIEW = binding.lottieAnimationView
        ConfigLoading.CONT_ADD_PRODUCTO = binding.contAddProducto
        ConfigLoading.FRAGMENT_NO_DATA = binding.fragmentNoData.contNoData
    }

    fun filterMascotas(text: String) {
        val filteredList = campañaList.filter { campaña ->
            campaña.mes.contains(text, ignoreCase = true)
        }
        campañaListAdapter.updateList(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private suspend fun obtenerCampañas() {
        try {
            FirebaseCampañaUtil.obtenerListaCampañas(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val campañas = mutableListOf<CampañaModel>()

                    for (campañaSnapshot in snapshot.children) {
                        val campaña = campañaSnapshot.getValue(CampañaModel::class.java)

                        campaña?.let { campañas.add(it) }
                    }

                    // Actualizar lista de campañas y notificar cambios al adaptador
                    campañaList.clear()
                    campañaList.addAll(campañas)
                    campañaListAdapter.notifyDataSetChanged()

                    // Actualizar título de la actividad si hay campañas
                   /* if (campañas.isNotEmpty()) {
                        requireActivity().title = "${getString(R.string.menu_campañas)} (${campañas.size})"
                    } else {
                        requireActivity().title = getString(R.string.menu_campañas)
                    }*/

                    // Ocultar la animación de carga si hay datos
                    ConfigLoading.hideLoadingAnimation()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejar errores de la consulta a la base de datos
                    println("Error al obtener campañas: ${error.message}")
                    ConfigLoading.showNodata()
                }
            })
        } catch (e: Exception) {
            // Manejar cualquier excepción que pueda ocurrir
            println("Error en obtenerCampañas(): ${e.message}")
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

