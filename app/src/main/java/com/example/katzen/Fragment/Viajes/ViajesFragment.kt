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
import com.example.katzen.R
import com.example.katzen.databinding.ViajesV2FragmentBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener

class ViajesFragment : Fragment() {
    val TAG : String  = "ViajesFragment"

    private var _binding: ViajesV2FragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viajesList: MutableList<VentaMesModel>
    private lateinit var viajesAdapter: ViajeAdapter

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

    fun init(){
        ConfigLoading.showLoadingAnimation()
        viajesList = mutableListOf()
        viajesAdapter = ViajeAdapter(requireActivity(), viajesList)
        binding.lisMenuViaje.adapter = viajesAdapter
        binding.lisMenuViaje.divider = null

        obtenerViajes()
    }
    fun filterClientes(text: String) {
        val filteredList = viajesList.filter { viaje ->

            viaje.mes.contains(text, ignoreCase = true)
        }
        viajesAdapter.updateList(filteredList)
    }
    fun listeners(){
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
        binding.lisMenuViaje.setOnItemClickListener { adapterView, view, i, l ->
            Config.MES_DETALLE = "${UtilHelper.obtenerNumeroMes(viajesList[i].mes)}-${viajesList[i].anio}"
            UtilFragment.changeFragment(requireContext(), ViajesDetalleFragment(),TAG)
        }

    }
    fun initLoading(){
        ConfigLoading.LOTTIE_ANIMATION_VIEW = binding.lottieAnimationView
        ConfigLoading.CONT_ADD_PRODUCTO = binding.contAddProducto
        ConfigLoading.FRAGMENT_NO_DATA = binding.fragmentNoData.contNoData
    }

    fun obtenerViajes() {
        FirebaseViajesUtil.obtenerListaViajes(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Limpiar la lista antes de agregar los nuevos datos
                viajesList.clear()

                for (viajeSnapshot in snapshot.children) {
                    try {
                        // Ensure the snapshot is of the expected type (Map<String, Any>)
                        if (viajeSnapshot.value !is Map<*, *>) {
                            continue
                        }

                        val genericTypeIndicator = object : GenericTypeIndicator<Map<String, Any>>() {}
                        val ventaMesMap: Map<String, Any> = viajeSnapshot.getValue(genericTypeIndicator) ?: continue

                        val ventaMesModel = VentaMesModel(
                            venta = ventaMesMap["venta"].toString(),
                            costo = ventaMesMap["costo"].toString(),
                            mes = ventaMesMap["mes"].toString(),
                            anio = ventaMesMap["anio"].toString(),
                            fecha = ventaMesMap["fecha"].toString(),
                            ganancia = ventaMesMap["ganancia"].toString()
                        )
                        viajesList.add(ventaMesModel)
                    } catch (e: Exception) {
                        // Log the error for debugging
                        Log.e("DataParsingError", "Error parsing snapshot", e)
                        continue
                    }
                }

                if (viajesList.size != 12) {
                    initYearFirebase(viajesList)
                    obtenerViajes()
                }

                // Notificar al adaptador que los datos han cambiado
                viajesAdapter.notifyDataSetChanged()
                if (viajesList.isNotEmpty()) {
                    ConfigLoading.hideLoadingAnimation()
                } else {
                    ConfigLoading.showNodata()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                ConfigLoading.showNodata()
                // Manejar errores de la consulta a la base de datos
                // Por ejemplo, mostrar un mensaje de error
            }
        })
    }

    fun initYearFirebase(viajesList: MutableList<VentaMesModel>) {
        val listMonths = UtilHelper.getMontsThisYears()

        for (i in 0 until listMonths.size) {
            val ventaMesModel = VentaMesModel().apply {
                venta = "0.00"
                costo = "0.00"
                ganancia = "0.00"
                anio = UtilHelper.getDateYear()
                mes = UtilHelper.getMonthYear(listMonths[i].split("-")[0].toInt())
                fecha = UtilHelper.getDate()
            }

            val exists = viajesList.any { it.mes == ventaMesModel.mes && it.anio == ventaMesModel.anio }

            if (!exists) {
                FirebaseViajesUtil.guardarListaMeses(listMonths[i], ventaMesModel)
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
                UtilFragment.changeFragment(requireContext() , MenuFragment() ,TAG)
            }
        })
    }

}