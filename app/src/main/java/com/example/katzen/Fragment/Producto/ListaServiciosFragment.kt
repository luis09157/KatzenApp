package com.example.katzen.Fragment.Producto

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.ServiciosAdapter
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseServicioUtil
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Model.ServicioModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.FragmentListaServiciosBinding

class ListaServiciosFragment : Fragment() {
    private var _binding: FragmentListaServiciosBinding? = null
    private val binding get() = _binding!!
    private lateinit var serviciosAdapter: ServiciosAdapter
    private val serviciosList = mutableListOf<ServicioModel>()
    private val serviciosListOriginal = mutableListOf<ServicioModel>()
    private val TAG = "ListaServiciosFragment"
    private var valueEventListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListaServiciosBinding.inflate(inflater, container, false)
        requireActivity().title = "Servicios"
        initLoading()
        setupAdapter()
        setupListeners()
        setupSearchBar()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargarServicios()
    }

    private fun initLoading() {
        ConfigLoading.init(
            binding.lottieAnimationView,
            binding.contAddProducto,
            binding.fragmentNoData.contNoData
        )
    }

    private fun setupAdapter() {
        serviciosAdapter = ServiciosAdapter(serviciosList, { servicio ->
            editarServicio(servicio)
        }, { servicio ->
            eliminarServicio(servicio)
        })
        binding.lisMenuServicios.adapter = serviciosAdapter
        binding.lisMenuServicios.divider = null
    }

    private fun setupSearchBar() {
        binding.searchBar.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filtrarServicios(s?.toString())
            }
        })
    }

    private fun filtrarServicios(query: String?) {
        if (query.isNullOrEmpty()) {
            serviciosList.clear()
            serviciosList.addAll(serviciosListOriginal)
        } else {
            val filteredList = serviciosListOriginal.filter { servicio ->
                servicio.nombre.lowercase().contains(query.lowercase()) ||
                servicio.codigoInterno.lowercase().contains(query.lowercase())
            }
            serviciosList.clear()
            serviciosList.addAll(filteredList)
        }
        serviciosAdapter.notifyDataSetChanged()
    }

    private fun setupListeners() {
        binding.btnAddServicio.setOnClickListener {
            UtilFragment.changeFragment(requireContext(), AddServicioFragment(), TAG)
        }
        
        binding.lisMenuServicios.setOnItemClickListener { _, _, position, _ ->
            editarServicio(serviciosList[position])
        }
    }

    private fun cargarServicios() {
        ConfigLoading.showLoadingAnimation()
        
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                
                serviciosList.clear()
                serviciosListOriginal.clear()
                for (servicioSnapshot in snapshot.children) {
                    val servicio = servicioSnapshot.getValue(ServicioModel::class.java)
                    servicio?.let { 
                        serviciosList.add(it)
                        serviciosListOriginal.add(it)
                    }
                }
                serviciosAdapter.notifyDataSetChanged()

                if (serviciosList.size > 0) {
                    requireActivity().title = "${getString(R.string.submenu_productos_servicios)} (${serviciosList.size})"
                    ConfigLoading.hideLoadingAnimation()
                } else {
                    ConfigLoading.showNodata()
                    binding.fragmentNoData.tvNoDataMessage.text = "No hay servicios registrados. Agrega uno nuevo con el botón inferior."
                    binding.fragmentNoData.btnAdd.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded) return
                ConfigLoading.showNodata()
                binding.fragmentNoData.tvNoDataMessage.text = "Error al cargar servicios: ${error.message}"
                binding.fragmentNoData.btnAdd.visibility = View.GONE
                DialogMaterialHelper.mostrarErrorDialog(
                    requireActivity(),
                    "Error al cargar los servicios: ${error.message}"
                )
            }
        }
        
        valueEventListener?.let {
            FirebaseServicioUtil.obtenerListaServicios(it)
        }
    }

    private fun editarServicio(servicio: ServicioModel) {
        val fragment = AddServicioFragment.newInstance(servicio)
        UtilFragment.changeFragment(requireContext(), fragment, TAG)
    }

    private fun eliminarServicio(servicio: ServicioModel) {
        DialogMaterialHelper.mostrarConfirmDeleteDialog(requireActivity(), "¿Eliminar servicio?") { confirmed ->
            if (confirmed) {
                FirebaseServicioUtil.eliminarServicio(servicio.id)
                    .addOnSuccessListener {
                        cargarServicios()
                        DialogMaterialHelper.mostrarSuccessDialog(requireActivity(), "Servicio eliminado correctamente")
                    }
                    .addOnFailureListener { e ->
                        DialogMaterialHelper.mostrarErrorDialog(requireActivity(), "Error: ${e.message}")
                    }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    UtilFragment.changeFragment(requireContext(), MenuProductosFragment(), TAG)
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        valueEventListener?.let {
            FirebaseServicioUtil.removerListener(it)
        }
        _binding = null
    }
} 