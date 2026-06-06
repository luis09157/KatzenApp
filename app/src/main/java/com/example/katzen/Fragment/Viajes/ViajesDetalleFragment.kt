package com.example.katzen.Fragment.Viajes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.katzen.Adapter.Viaje.ViajeMesDetalleAdapter
import com.example.katzen.Config.Config
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseViajesUtil
import com.example.katzen.Helper.ListScrollKeys
import com.example.katzen.Helper.ListUiHelper
import com.example.katzen.Helper.SearchUiHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Model.ClienteModel
import com.example.katzen.Model.VentaMesDetalleModel
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.ViajesDetalleFragmentBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViajesDetalleFragment : Fragment() {
    private val TAG: String = "ViajesDetalleFragment"

    private var _binding: ViajesDetalleFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viajesDetalleList: MutableList<VentaMesDetalleModel>
    private lateinit var viajesDetalleAdapter: ViajeMesDetalleAdapter
    private var viajesListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ViajesDetalleFragmentBinding.inflate(inflater, container, false)
        requireActivity().title = getString(R.string.menu_viajes)
        initLoading()
        setupAdapter()
        listeners()
        cargarViajes()
        return binding.root
    }

    private fun setupAdapter() {
        viajesDetalleList = mutableListOf()
        viajesDetalleAdapter = ViajeMesDetalleAdapter(requireActivity()) { viaje ->
            AddViajeFragment.EDIT_VIAJE = viaje
            UtilFragment.changeFragment(
                requireContext(),
                AddViajeFragment(),
                TAG,
                listKey = ListScrollKeys.VIAJES_DETALLE,
                listRecyclerView = binding.lisMenuViaje,
                selectedItemId = viaje.id
            )
        }
        viajesDetalleAdapter.tag = TAG
        ListUiHelper.setupVerticalList(binding.lisMenuViaje)
        binding.lisMenuViaje.adapter = viajesDetalleAdapter
    }

    private fun cargarViajes() {
        ConfigLoading.showLoadingAnimation()
        viajesListener?.let { FirebaseViajesUtil.removerListenerCargosViajes(it) }

        viajesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                getData(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error al obtener los datos de Firebase: ${error.message}")
                ConfigLoading.showNodata()
            }
        }
        viajesListener?.let { FirebaseViajesUtil.obtenerListaCargosViajes(it) }
    }

    private data class ResultadoDatos(
        val lista: List<VentaMesDetalleModel>,
        val costoTotal: Double,
        val gananciaTotal: Double,
        val ventaTotal: Double
    )

    private fun getData(dataSnapshot: DataSnapshot) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.Default) {
                    processSnapshotData(dataSnapshot)
                }
                updateUI(result)
            } catch (e: Exception) {
                Log.e(TAG, "Error al procesar los datos: ${e.message}", e)
                ConfigLoading.showNodata()
            }
        }
    }

    private suspend fun processSnapshotData(dataSnapshot: DataSnapshot): ResultadoDatos {
        val tempList = mutableListOf<VentaMesDetalleModel>()
        var costoTotal = 0.00
        var gananciaTotal = 0.00
        var ventaTotal = 0.00

        for (postSnapshot in dataSnapshot.children) {
            for (data in postSnapshot.children) {
                val ventaMesDetalleModel = data.getValue(VentaMesDetalleModel::class.java) ?: continue
                costoTotal += ventaMesDetalleModel.costo.toDouble()
                ventaTotal += ventaMesDetalleModel.venta.toDouble()
                gananciaTotal += ventaMesDetalleModel.ganancia.toDouble()
                tempList.add(ventaMesDetalleModel)
            }
        }

        return ResultadoDatos(tempList, costoTotal, gananciaTotal, ventaTotal)
    }

    private fun updateUI(result: ResultadoDatos) {
        viajesDetalleList.clear()
        viajesDetalleList.addAll(result.lista)
        Config.COSTO = result.costoTotal
        Config.GANANCIA = result.gananciaTotal
        Config.VENTA = result.ventaTotal

        if (viajesDetalleList.isNotEmpty()) {
            FirebaseViajesUtil.editarResumenViajes()
            viajesDetalleAdapter.updateList(viajesDetalleList.toList())
            ListUiHelper.restoreScrollIfPending(
                ListScrollKeys.VIAJES_DETALLE,
                binding.lisMenuViaje,
                viajesDetalleList.map { it.id }
            )
            ConfigLoading.hideLoadingAnimation()
        } else {
            ConfigLoading.showNodata()
        }
    }

    private fun filterClientes(text: String) {
        val filteredList = viajesDetalleList.filter { viaje ->
            viaje.nombreDomicilio.contains(text, ignoreCase = true)
        }
        viajesDetalleAdapter.updateList(filteredList)
        ListUiHelper.restoreScrollIfPending(
            ListScrollKeys.VIAJES_DETALLE,
            binding.lisMenuViaje,
            filteredList.map { it.id }
        )
    }

    private fun listeners() {
        SearchUiHelper.bindSearch(binding.searchBar.searchEditText) { query ->
            filterClientes(query)
        }

        binding.btnAddViaje.setOnClickListener {
            AddViajeFragment.ADD_VIAJE = VentaMesDetalleModel()
            AddViajeFragment.ADD_CLIENTE_VIAJE = ClienteModel()
            AddViajeFragment.EDIT_VIAJE = VentaMesDetalleModel()
            UtilFragment.changeFragment(requireContext(), AddViajeFragment(), TAG)
        }
    }

    private fun initLoading() {
        ConfigLoading.init(
            binding.lottieAnimationView,
            binding.contAddProducto,
            binding.fragmentNoData.contNoData
        )
    }

    override fun onDestroyView() {
        viajesListener?.let { FirebaseViajesUtil.removerListenerCargosViajes(it) }
        viajesListener = null
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
