package com.example.katzen.Fragment.Viajes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.Viaje.ViajeMesDetalleAdapter
import com.example.katzen.Config.Config
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseViajesUtil
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Model.ClienteModel
import com.example.katzen.Model.VentaMesDetalleModel
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.ViajesDetalleFragmentBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViajesDetalleFragment : Fragment() {
    private val TAG: String = "ViajesDetalleFragment"

    private var _binding: ViajesDetalleFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viajesDetalleList: MutableList<VentaMesDetalleModel>
    private lateinit var viajesDetalleAdapter: ViajeMesDetalleAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ViajesDetalleFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        requireActivity().title = getString(R.string.menu_viajes)

        initLoading()
        init()
        listeners()

        return root
    }

    private fun init() {
        ConfigLoading.showLoadingAnimation()
        viajesDetalleList = mutableListOf()
        viajesDetalleAdapter = ViajeMesDetalleAdapter(requireActivity(), viajesDetalleList)
        binding.lisMenuViaje.adapter = viajesDetalleAdapter
        binding.lisMenuViaje.divider = null

        // Obtener los cargos de viaje
        FirebaseViajesUtil.obtenerListaCargosViajes(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                getData(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error al obtener los datos de Firebase: ${error.message}")
                ConfigLoading.showNodata()
                // Manejar errores de la consulta a la base de datos
                // Por ejemplo, mostrar un mensaje de error
            }
        })
    }

    private data class ResultadoDatos(
        val lista: List<VentaMesDetalleModel>,
        val costoTotal: Double,
        val gananciaTotal: Double,
        val ventaTotal: Double
    )

    private fun getData(dataSnapshot: DataSnapshot) {
        CoroutineScope(Dispatchers.Main).launch {
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
        var tempList = mutableListOf<VentaMesDetalleModel>()
        var costoTotal = 0.00
        var gananciaTotal = 0.00
        var ventaTotal = 0.00

        for (postSnapshot in dataSnapshot.children) {
            for (data in postSnapshot.children) {
                val ventaMesDetalleModel = data.getValue(VentaMesDetalleModel::class.java)
                    ?: continue

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
            viajesDetalleAdapter.notifyDataSetChanged()
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
    }

    private fun listeners() {
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

        binding.lisMenuViaje.setOnItemClickListener { _, _, i, _ ->
            // Acción al hacer clic en un ítem de la lista
            // Config.MES_DETALLE = "${UtilHelper.obtenerNumeroMes(viajesDetalleList[i].mes)}-${viajesDetalleList[i].anio}"
            // UtilFragment.changeFragment(requireContext(), ViajesDetalleFragment(), TAG)
        }

        binding.btnAddViaje.setOnClickListener {
            AddViajeFragment.ADD_VIAJE = VentaMesDetalleModel()
            AddViajeFragment.ADD_CLIENTE_VIAJE = ClienteModel()
            AddViajeFragment.EDIT_VIAJE = VentaMesDetalleModel()
            UtilFragment.changeFragment(requireContext(), AddViajeFragment(), TAG)
            // DialogHelper.dialogAddDomicilio(requireActivity())
        }
    }

    private fun initLoading() {
        ConfigLoading.LOTTIE_ANIMATION_VIEW = binding.lottieAnimationView
        ConfigLoading.CONT_ADD_PRODUCTO = binding.contAddProducto
        ConfigLoading.FRAGMENT_NO_DATA = binding.fragmentNoData.contNoData
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        init()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Obtener el año del mes detalle actual
                val year = Config.MES_DETALLE.split("-")[1]
                val fragment = ViajesFragment.newInstance(year)
                UtilFragment.changeFragment(requireContext(), fragment, TAG)
            }
        })
    }
}
