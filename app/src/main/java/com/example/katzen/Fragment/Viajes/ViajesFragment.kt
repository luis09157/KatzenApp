package com.example.katzen.Fragment.Viajes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.Viaje.ViajeAdapter
import com.example.katzen.Config.Config
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseViajesUtil
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.MenuFragment
import com.example.katzen.Model.VentaMesModel
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.ViajesV2FragmentBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener

class ViajesFragment : Fragment() {
    private val TAG: String = "ViajesFragment"

    private var _binding: ViajesV2FragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viajesList: MutableList<VentaMesModel>
    private lateinit var viajesAdapter: ViajeAdapter

    companion object {
        private var lastSelectedYear: String? = null

        fun newInstance(year: String): ViajesFragment {
            lastSelectedYear = year
            return ViajesFragment().apply {
                arguments = Bundle().apply {
                    putString("selected_year", year)
                }
            }
        }
    }

    private var selectedYear: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedYear = arguments?.getString("selected_year") ?: lastSelectedYear
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ViajesV2FragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        requireActivity().title = getString(R.string.menu_viajes)

        initLoading()
        init()
        listeners()

        return root
    }

    private fun init() {
        ConfigLoading.showLoadingAnimation()
        viajesList = mutableListOf()
        viajesAdapter = ViajeAdapter(requireActivity(), viajesList)
        binding.lisMenuViaje.adapter = viajesAdapter
        binding.lisMenuViaje.divider = null

        obtenerViajes()
    }

    private fun filterClientes(text: String) {
        val filteredList = viajesList.filter { viaje ->
            viaje.mes.contains(text, ignoreCase = true)
        }
        viajesAdapter.updateList(filteredList)
    }

    private fun listeners() {
        binding.buscarCliente.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterClientes(newText?.trim().orEmpty())
                return true
            }
        })

        binding.lisMenuViaje.setOnItemClickListener { _, _, i, _ ->
            Config.MES_DETALLE = "${UtilHelper.obtenerNumeroMes(viajesList[i].mes)}-${viajesList[i].anio}"
            UtilFragment.changeFragment(requireContext(), ViajesDetalleFragment(), TAG)
        }
    }

    fun initLoading() {
        ConfigLoading.init(
            binding.lottieAnimationView,
            binding.contAddProducto,
            binding.fragmentNoData.contNoData
        )
    }


    private fun obtenerViajes() {
        val year = selectedYear ?: return
        
        Log.d(TAG, "Obteniendo viajes para el año: $year")
        FirebaseViajesUtil.obtenerListaViajes(year, object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    Log.d(TAG, "¿Existe snapshot? ${snapshot.exists()}")
                    Log.d(TAG, "Cantidad de hijos: ${snapshot.childrenCount}")
                    
                    viajesList.clear()

                    val mesesValidos = snapshot.children
                        .mapNotNull { it.key }
                        .filter { it.contains("-") }
                        .sorted()

                    for (mesKey in mesesValidos) {
                        val mesSnapshot = snapshot.child(mesKey)
                        Log.d(TAG, "Procesando mes-año: $mesKey")

                        val dataMap = mesSnapshot.value as? Map<String, Any>
                        if (dataMap == null) {
                            Log.d(TAG, "dataMap es null para $mesKey")
                            continue
                        }

                        val mesParts = mesKey.split("-")
                        if (mesParts.size != 2) continue

                        val mesNumero = mesParts[0]
                        val mesAbreviado = UtilHelper.getMonthYear(mesNumero.toInt())

                        val ventaMesModel = VentaMesModel(
                            venta = dataMap["venta"]?.toString() ?: "0.00",
                            costo = dataMap["costo"]?.toString() ?: "0.00",
                            mes = mesAbreviado,
                            anio = year,
                            fecha = dataMap["fecha"]?.toString() ?: "",
                            ganancia = dataMap["ganancia"]?.toString() ?: "0.00",
                            cargos = dataMap["cargos"]?.toString() ?: "0.00"
                        )

                        viajesList.add(ventaMesModel)
                        Log.d(TAG, "Viaje añadido: ${ventaMesModel.mes}-${ventaMesModel.anio}")
                    }



                    Log.d(TAG, "Total de viajes encontrados: ${viajesList.size}")

                    viajesAdapter.notifyDataSetChanged()
                    if (viajesList.isEmpty()) {
                        if (year == UtilHelper.getDateYear()) {
                            initYearFirebase(viajesList)
                        } else {
                            ConfigLoading.showNodata()
                        }
                    } else {
                        ConfigLoading.hideLoadingAnimation()
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error al obtener los viajes: ${e.message}")
                    e.printStackTrace()
                    ConfigLoading.showNodata()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error en la consulta a Firebase: ${error.message}")
                ConfigLoading.showNodata()
            }
        })
    }

    private fun initYearFirebase(viajesList: MutableList<VentaMesModel>) {
        val year = selectedYear ?: return
        val listMonths = ArrayList<String>()

        for (i in 1..12) {
            val monthStr = String.format("%02d", i)
            listMonths.add("$monthStr-$year")
        }

        for (month in listMonths) {
            val ventaMesModel = VentaMesModel().apply {
                venta = "0.00"
                costo = "0.00"
                ganancia = "0.00"
                anio = year
                mes = UtilHelper.getMonthYear(month.split("-")[0].toInt())
                fecha = UtilHelper.getDate()
            }

            val exists = viajesList.any { it.mes == ventaMesModel.mes && it.anio == ventaMesModel.anio }

            if (!exists) {
                FirebaseViajesUtil.guardarListaMeses(month, ventaMesModel)
            }
        }
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
                UtilFragment.changeFragment(requireContext(), YearViajeListFragment(), TAG)
            }
        })
    }
}
