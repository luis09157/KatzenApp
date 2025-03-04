package com.example.katzen.Fragment.Campaña

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.Campaña.CampañaAdapter
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseCampañaUtil
import com.example.katzen.Helper.CalendarioUtil
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.MenuFragment
import com.example.katzen.Model.CampañaModel
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.CampaniaFragmentBinding
import kotlinx.coroutines.*

class CampañaFragment : Fragment() {
    val TAG: String = "CampañaFragment"

    private var _binding: CampaniaFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var campañaList: MutableList<CampañaModel>
    private lateinit var campañaListAdapter: CampañaAdapter
    companion object{
        var ADD_CAMPAÑA : CampañaModel = CampañaModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CampaniaFragmentBinding.inflate(inflater, container, false)
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
            initCampañas()
            obtenerCantidadCampañasPorMes()
        }
    }

    fun init() {
        ConfigLoading.showLoadingAnimation()
        campañaList = mutableListOf()
        campañaListAdapter = CampañaAdapter(requireActivity(), campañaList)
        binding.lisMenuCampaA.adapter = campañaListAdapter
    }
    fun listeners() {
        binding.lisMenuCampaA.setOnItemClickListener { adapterView, view, i, l ->
            ADD_CAMPAÑA.año = CalendarioUtil.obtenerAñoActual()
            ADD_CAMPAÑA.mes = String.format("%02d", (i + 1))
            UtilFragment.changeFragment(requireActivity(), CampañaEventoFragment(),TAG)
        }
        binding.buscarMascota.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterMascotas(newText.toString())
                return true
            }
        })
    }
    fun initLoading() {
        ConfigLoading.LOTTIE_ANIMATION_VIEW = binding.lottieAnimationView
        ConfigLoading.CONT_ADD_PRODUCTO = binding.contAddProducto
        ConfigLoading.FRAGMENT_NO_DATA = binding.fragmentNoData.contNoData
    }
    fun initCampañas() {
        campañaList.clear() // Limpiar la lista antes de agregar nuevos elementos

        // Array con los nombres de los meses en español
        val nombresMeses = arrayOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )

        val añoActual = CalendarioUtil.obtenerAñoActual()

        // Iterar del 1 al 12 (meses)
        for (i in 1..12) {
            val mes = String.format("%02d", i) // Formatear el número del mes a dos dígitos (01, 02, ..., 12)
            val mesCompleto = "$mes-$añoActual"

            val campañaModel = CampañaModel().apply {
                cantidadCampañas = "0"
                this.mes = nombresMeses[i - 1] // Obtener el nombre del mes desde el array
            }
            campañaList.add(campañaModel)
        }

        // Notificar al adaptador que los datos han cambiado
        campañaListAdapter.notifyDataSetChanged()
    }
    suspend fun obtenerCantidadCampañasPorMes() {
        for (campañaModel in campañaList) {
            val mesKey = obtenerKeyDesdeNombreMes(campañaModel.mes) // Implementa esta función según tu lógica
            try {
                val cantidadCampañas = FirebaseCampañaUtil.contarCampañasPorMes(mesKey).toInt()
                campañaModel.cantidadCampañas = cantidadCampañas.toString()
                withContext(Dispatchers.Main) {
                    campañaListAdapter.notifyDataSetChanged()
                }
                ConfigLoading.hideLoadingAnimation()
            } catch (e: Exception) {
                ConfigLoading.showNodata()
                println("Error al obtener cantidad de campañas para ${campañaModel.mes}: ${e.message}")
                // Manejar el error si es necesario
            }
        }
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
    // Función para obtener la clave del mes a partir de su nombre
    private fun obtenerKeyDesdeNombreMes(nombreMes: String): String {
        val nombresMeses = arrayOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )
        val index = nombresMeses.indexOf(nombreMes)
        return if (index != -1) {
            String.format("%02d", index + 1) + "-${CalendarioUtil.obtenerAñoActual()}"
        } else {
            ""
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
