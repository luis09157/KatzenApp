package com.example.katzen.Fragment.Campaña

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.Campaña.YearListAdapter
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.MenuFragment
import com.example.katzen.Model.YearModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ninodev.katzen.databinding.FragmentYearListBinding

class YearListFragment : Fragment() {
    private var _binding: FragmentYearListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: YearListAdapter
    private val yearList = mutableListOf<YearModel>()
    private val TAG = "YearListFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentYearListBinding.inflate(inflater, container, false)
        val root: View = binding.root
        
        requireActivity().title = "Campaña"
        
        adapter = YearListAdapter(requireActivity(), yearList)
        binding.listViewYears.setOnItemClickListener { _, _, position, _ ->
            val selectedYear = yearList[position].year
            val fragment = CampañaFragment.newInstance(selectedYear)
            UtilFragment.changeFragment(requireActivity(), fragment, TAG)
        }
        binding.listViewYears.adapter = adapter
        
        initLoading()
        loadYears()
        
        return root
    }

    private fun initLoading() {
        ConfigLoading.LOTTIE_ANIMATION_VIEW = binding.lottieAnimationView
        ConfigLoading.FRAGMENT_NO_DATA = binding.fragmentNoData.contNoData
        
        ConfigLoading.CONT_ADD_PRODUCTO = binding.root
    }

    private fun loadYears() {
        Log.d(TAG, "Iniciando carga de años")
        
        binding.lottieAnimationView.visibility = View.VISIBLE
        binding.fragmentNoData.contNoData.visibility = View.GONE
        binding.listViewYears.visibility = View.GONE
        
        val database = FirebaseDatabase.getInstance()
        val campañasRef = database.getReference("Katzen/Campaña")

        campañasRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (!snapshot.exists()) {
                        Log.d(TAG, "No existen datos en la referencia Katzen/Campaña")
                        showNoData()
                        return
                    }

                    Log.d(TAG, "Datos recibidos de Firebase: ${snapshot.childrenCount} registros")
                    val years = mutableMapOf<String, Int>()
                    
                    // Recorrer los años directamente (2024, 2025, etc.)
                    for (yearSnapshot in snapshot.children) {
                        val year = yearSnapshot.key ?: continue
                        Log.d(TAG, "Procesando año: $year")
                        
                        // Contar cuántos meses tiene este año
                        val campañasCount = yearSnapshot.childrenCount.toInt()
                        years[year] = campañasCount
                        Log.d(TAG, "Año encontrado: $year con $campañasCount campañas")
                    }

                    if (years.isEmpty()) {
                        Log.d(TAG, "No se encontraron años")
                        showNoData()
                        return
                    }

                    val yearModelList = years.map { 
                        YearModel(it.key, it.value) 
                    }.sortedByDescending { it.year }

                    Log.d(TAG, "Lista de años creada: ${yearModelList.size} años")

                    requireActivity().runOnUiThread {
                        yearList.clear()
                        yearList.addAll(yearModelList)
                        adapter.notifyDataSetChanged()
                        hideLoading()

                        // Debug de la lista final
                        yearList.forEach { 
                            Log.d(TAG, "Año en lista final: ${it.year}, Campañas: ${it.campañasCount}")
                        }
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error al cargar años: ${e.message}")
                    e.printStackTrace()
                    requireActivity().runOnUiThread {
                        showNoData()
                        DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error al cargar los años: ${e.message}")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error en la base de datos: ${error.message}")
                requireActivity().runOnUiThread {
                    showNoData()
                    DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error: ${error.message}")
                }
            }
        })
    }
    
    private fun hideLoading() {
        binding.lottieAnimationView.visibility = View.GONE
        binding.fragmentNoData.contNoData.visibility = View.GONE
        binding.listViewYears.visibility = View.VISIBLE
    }
    
    private fun showNoData() {
        binding.lottieAnimationView.visibility = View.GONE
        binding.fragmentNoData.contNoData.visibility = View.VISIBLE
        binding.listViewYears.visibility = View.GONE
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
        binding.lottieAnimationView.clearAnimation()
        binding.lottieAnimationView.visibility = View.GONE
        _binding = null
    }
} 