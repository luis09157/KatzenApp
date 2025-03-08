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
        fun newInstance(year: String): ViajesFragment {
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
        selectedYear = arguments?.getString("selected_year")
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

    private fun initLoading() {
        ConfigLoading.LOTTIE_ANIMATION_VIEW = binding.lottieAnimationView
        ConfigLoading.CONT_ADD_PRODUCTO = binding.contAddProducto
        ConfigLoading.FRAGMENT_NO_DATA = binding.fragmentNoData.contNoData
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

                    snapshot.children.forEach { mesSnapshot ->
                        Log.d(TAG, "Key del mes: ${mesSnapshot.key}")
                        Log.d(TAG, "Valor del mes: ${mesSnapshot.value}")

                        val ventaMesMap = mesSnapshot.getValue(object : GenericTypeIndicator<Map<String, Any>>() {})
                        if (ventaMesMap == null) {
                            Log.d(TAG, "ventaMesMap es null para ${mesSnapshot.key}")
                            return@forEach
                        }

                        val anioReal = ventaMesMap["anio"]?.toString() ?: year
                        Log.d(TAG, "Año extraído: $anioReal")

                        val ventaMesModel = VentaMesModel(
                            venta = ventaMesMap["venta"]?.toString() ?: "0.00",
                            costo = ventaMesMap["costo"]?.toString() ?: "0.00",
                            mes = ventaMesMap["mes"]?.toString() ?: "",
                            anio = anioReal,
                            fecha = ventaMesMap["fecha"]?.toString() ?: "",
                            ganancia = ventaMesMap["ganancia"]?.toString() ?: "0.00",
                            cargos = ventaMesMap["cargos"]?.toString() ?: "0.00"
                        )

                        if (ventaMesModel.anio == year) {
                            viajesList.add(ventaMesModel)
                            Log.d(TAG, "Agregado viaje: ${ventaMesModel.mes}-${ventaMesModel.anio}")
                        }
                    }

                    Log.d(TAG, "Total de viajes en lista antes de actualizar adapter: ${viajesList.size}")

                    if (viajesList.isEmpty() && year == UtilHelper.getDateYear()) {
                        Log.d(TAG, "Lista vacía. Inicializando datos para el año actual.")
                        initYearFirebase(viajesList)
                    } else {
                        viajesAdapter.notifyDataSetChanged()
                        if (viajesList.isEmpty()) {
                            ConfigLoading.showNodata()
                        } else {
                            ConfigLoading.hideLoadingAnimation()
                        }
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
        val listMonths = UtilHelper.getMontsThisYears()

        for (month in listMonths) {
            val ventaMesModel = VentaMesModel().apply {
                venta = "0.00"
                costo = "0.00"
                ganancia = "0.00"
                anio = UtilHelper.getDateYear()
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
                UtilFragment.changeFragment(requireContext(), MenuFragment(), TAG)
            }
        })
    }
}
