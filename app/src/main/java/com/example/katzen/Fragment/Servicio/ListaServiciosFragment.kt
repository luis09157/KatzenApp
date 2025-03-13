package com.example.katzen.Fragment.Servicio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.ServiciosAdapter
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseServicioUtil
import com.example.katzen.Fragment.Producto.MenuProductosFragment
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
    private val TAG = "ListaServiciosFragment"
    private var valueEventListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListaServiciosBinding.inflate(inflater, container, false)
        initLoading()
        setupAdapter()
        setupListeners()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargarServicios()
    }

    private fun initLoading() {
        ConfigLoading.init(
            binding.lottieAnimationView,
            binding.contListaServicios,
            binding.fragmentNoData.contNoData
        )
    }

    private fun setupAdapter() {
        serviciosAdapter = ServiciosAdapter(serviciosList) { servicio ->
            editarServicio(servicio)
        }
        binding.lisMenuServicios.adapter = serviciosAdapter
        binding.lisMenuServicios.divider = null
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
                for (servicioSnapshot in snapshot.children) {
                    val servicio = servicioSnapshot.getValue(ServicioModel::class.java)
                    servicio?.let { serviciosList.add(it) }
                }
                serviciosAdapter.notifyDataSetChanged()

                if (serviciosList.size > 0) {
                    requireActivity().title = "${getString(R.string.submenu_servicios)} (${serviciosList.size})"
                    ConfigLoading.hideLoadingAnimation()
                } else {
                    ConfigLoading.showNodata()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded) return
                ConfigLoading.showNodata()
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