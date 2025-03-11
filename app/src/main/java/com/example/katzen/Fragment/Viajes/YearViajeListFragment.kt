package com.example.katzen.Fragment.Viajes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.Viajes.YearViajeListAdapter
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.MenuFragment
import com.example.katzen.Model.YearViajeModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ninodev.katzen.databinding.FragmentYearListBinding

class YearViajeListFragment : Fragment() {
    private var _binding: FragmentYearListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: YearViajeListAdapter
    private val yearList = mutableListOf<YearViajeModel>()
    private val TAG = "YearViajeListFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentYearListBinding.inflate(inflater, container, false)
        val root: View = binding.root
        
        requireActivity().title = "Viajes"
        
        adapter = YearViajeListAdapter(requireActivity(), yearList)
        binding.listViewYears.setOnItemClickListener { _, _, position, _ ->
            val selectedYear = yearList[position].year
            val fragment = ViajesFragment.newInstance(selectedYear)
            UtilFragment.changeFragment(requireActivity(), fragment, TAG)
        }
        binding.listViewYears.adapter = adapter
        
        initLoading()
        loadYears()
        
        return root
    }
    fun initLoading() {
        ConfigLoading.init(
            binding.lottieAnimationView,
            binding.root,
            binding.fragmentNoData.contNoData
        )
    }
    private fun loadYears() {
        Log.d(TAG, "Iniciando carga de años")
        ConfigLoading.showLoadingAnimation()
        val database = FirebaseDatabase.getInstance()
        val viajesRef = database.getReference("Katzen/Gasolina")

        viajesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (!snapshot.exists()) {
                        Log.d(TAG, "No existen datos en la referencia Katzen/Viajes")
                        ConfigLoading.showNodata()
                        return
                    }

                    Log.d(TAG, "Datos recibidos de Firebase: ${snapshot.childrenCount} registros")
                    val years = mutableMapOf<String, Int>()
                    
                    for (yearSnapshot in snapshot.children) {
                        val year = yearSnapshot.key ?: continue
                        Log.d(TAG, "Procesando año: $year")
                        
                        val viajesCount = yearSnapshot.childrenCount.toInt()
                        years[year] = viajesCount
                        Log.d(TAG, "Año encontrado: $year con $viajesCount viajes")
                    }

                    if (years.isEmpty()) {
                        Log.d(TAG, "No se encontraron años")
                        ConfigLoading.showNodata()
                        return
                    }

                    val yearModelList = years.map { 
                        YearViajeModel(it.key, it.value) 
                    }.sortedByDescending { it.year }

                    Log.d(TAG, "Lista de años creada: ${yearModelList.size} años")

                    requireActivity().runOnUiThread {
                        yearList.clear()
                        yearList.addAll(yearModelList)
                        adapter.notifyDataSetChanged()
                        ConfigLoading.hideLoadingAnimation()

                        yearList.forEach { 
                            Log.d(TAG, "Año en lista final: ${it.year}, Viajes: ${it.viajesCount}")
                        }
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error al cargar años: ${e.message}")
                    e.printStackTrace()
                    requireActivity().runOnUiThread {
                        ConfigLoading.showNodata()
                        DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error al cargar los años: ${e.message}")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error en la base de datos: ${error.message}")
                requireActivity().runOnUiThread {
                    ConfigLoading.showNodata()
                    DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error: ${error.message}")
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                UtilFragment.changeFragment(requireContext(), MenuFragment(), TAG)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ConfigLoading.hideLoadingAnimation()
        _binding = null
    }
} 