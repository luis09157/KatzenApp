package com.example.katzen.Fragment.Viajes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.Venta.VentaMesDetalleAdapter
import com.example.katzen.Adapter.Viaje.ViajeAdapter
import com.example.katzen.Adapter.Viaje.ViajeMesDetalleV2Adapter
import com.example.katzen.Config.Config
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseViajesUtil
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.MenuFragment
import com.example.katzen.Model.VentaMesDetalleModel
import com.example.katzen.Model.VentaMesModel
import com.example.katzen.R
import com.example.katzen.databinding.ViajesDetalleV2FragmentBinding
import com.example.katzen.databinding.ViajesV2FragmentBinding
import com.example.katzen.ui.viajes.ViajesDetalleFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener

class ViajesDetalleV2Fragment : Fragment() {
    val TAG : String  = "ViajesDetalleV2Fragment"

    private var _binding: ViajesDetalleV2FragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viajesDetalleList: MutableList<VentaMesDetalleModel>
    private lateinit var viajesDetalleAdapter: ViajeMesDetalleV2Adapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ViajesDetalleV2FragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        requireActivity().title = getString(R.string.menu_viajes)

        initLoading()
        init()
        listeners()

        return root
    }

    fun init(){
        ConfigLoading.showLoadingAnimation()
        viajesDetalleList = mutableListOf()
        viajesDetalleAdapter = ViajeMesDetalleV2Adapter(requireActivity(), viajesDetalleList)
        binding.lisMenuViaje.adapter = viajesDetalleAdapter
        binding.lisMenuViaje.divider = null

        // Obtener los cargos de viaje
        FirebaseViajesUtil.obtenerListaCargosViajes(Config.MES_DETALLE, object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Limpiar la lista antes de agregar los nuevos datos
                viajesDetalleList.clear()

                for (viajeSnapshot in snapshot.children) {
                    val genericTypeIndicator = object : GenericTypeIndicator<Map<String, Any>>() {}
                    val ventaMesMap: Map<String, Any> = viajeSnapshot.getValue(genericTypeIndicator) ?: continue
                    val ventaMesModel = VentaMesDetalleModel(
                        venta = ventaMesMap["venta"].toString(),
                        costo = ventaMesMap["costo"].toString(),
                        fecha = ventaMesMap["fecha"].toString(),
                        ganancia = ventaMesMap["ganancia"].toString()
                    )
                    viajesDetalleList.add(ventaMesModel)
                }

                // Notificar al adaptador que los datos han cambiado
                viajesDetalleAdapter.notifyDataSetChanged()
                if (viajesDetalleList.size > 0) {
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

    fun filterClientes(text: String) {
        val filteredList = viajesDetalleList.filter { viaje ->
            viaje.kilometros.contains(text, ignoreCase = true)
        }
        viajesDetalleAdapter.updateList(filteredList)
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
            //Config.MES_DETALLE = "${UtilHelper.obtenerNumeroMes(viajesDetalleList[i].mes)}-${viajesDetalleList[i].anio}"
            UtilFragment.changeFragment(requireContext(), ViajesDetalleFragment(),TAG)
        }

    }

    fun initLoading(){
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
                UtilFragment.changeFragment(requireContext() , ViajesFragment() ,TAG)
            }
        })
    }

}