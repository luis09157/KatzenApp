package com.example.katzen.Fragment.Producto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.ProcedimientosAdapter
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseProcedimientoUtil
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Model.ProcedimientoModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.FragmentListaProcedimientosBinding

class ListaProcedimientosFragment : Fragment() {
    private var _binding: FragmentListaProcedimientosBinding? = null
    private val binding get() = _binding!!
    private lateinit var procedimientosAdapter: ProcedimientosAdapter
    private val procedimientosList = mutableListOf<ProcedimientoModel>()
    private val procedimientosListOriginal = mutableListOf<ProcedimientoModel>()
    private val TAG = "ListaProcedimientosFragment"
    private var valueEventListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListaProcedimientosBinding.inflate(inflater, container, false)
        requireActivity().title = "Procedimientos"
        initLoading()
        setupAdapter()
        setupListeners()
        setupSearchBar()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargarProcedimientos()
    }

    private fun initLoading() {
        ConfigLoading.init(
            binding.lottieAnimationView,
            binding.contAddProducto,
            binding.fragmentNoData.contNoData
        )
    }

    private fun setupAdapter() {
        procedimientosAdapter = ProcedimientosAdapter(
            procedimientosList,
            { procedimiento -> editarProcedimiento(procedimiento) },
            { procedimiento -> eliminarProcedimiento(procedimiento) }
        )
        binding.lisMenuProcedimientos.adapter = procedimientosAdapter
        binding.lisMenuProcedimientos.divider = null
    }

    private fun setupSearchBar() {
        binding.searchBar.editText?.addTextChangedListener { editable ->
            filtrarProcedimientos(editable?.toString())
        }
    }

    private fun filtrarProcedimientos(query: String?) {
        if (query.isNullOrEmpty()) {
            procedimientosList.clear()
            procedimientosList.addAll(procedimientosListOriginal)
        } else {
            val filteredList = procedimientosListOriginal.filter { procedimiento ->
                procedimiento.nombre.lowercase().contains(query.lowercase()) ||
                procedimiento.codigoInterno.lowercase().contains(query.lowercase()) ||
                procedimiento.tipo.lowercase().contains(query.lowercase())
            }
            procedimientosList.clear()
            procedimientosList.addAll(filteredList)
        }
        procedimientosAdapter.updateList(procedimientosList)
    }

    private fun setupListeners() {
        binding.btnAddProcedimiento.setOnClickListener {
            UtilFragment.changeFragment(requireContext(), AddProcedimientoFragment(), TAG)
        }
    }

    private fun cargarProcedimientos() {
        ConfigLoading.showLoadingAnimation()
        
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                
                procedimientosList.clear()
                procedimientosListOriginal.clear()
                for (procedimientoSnapshot in snapshot.children) {
                    val procedimiento = procedimientoSnapshot.getValue(ProcedimientoModel::class.java)
                    procedimiento?.let { 
                        procedimientosList.add(it)
                        procedimientosListOriginal.add(it)
                    }
                }
                procedimientosAdapter.updateList(procedimientosList)

                if (procedimientosList.size > 0) {
                    requireActivity().title = "Procedimientos (${procedimientosList.size})"
                    ConfigLoading.hideLoadingAnimation()
                } else {
                    ConfigLoading.showNodata()
                    binding.fragmentNoData.tvNoDataMessage.text = "No hay procedimientos registrados. Agrega uno nuevo con el botón inferior."
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded) return
                ConfigLoading.showNodata()
                binding.fragmentNoData.tvNoDataMessage.text = "Error al cargar procedimientos: ${error.message}"
                DialogMaterialHelper.mostrarErrorDialog(
                    requireActivity(),
                    "Error al cargar los procedimientos: ${error.message}"
                )
            }
        }
        
        valueEventListener?.let {
            FirebaseProcedimientoUtil.obtenerListaProcedimientos(it)
        }
    }

    private fun editarProcedimiento(procedimiento: ProcedimientoModel) {
        val fragment = AddProcedimientoFragment.newInstance(procedimiento)
        UtilFragment.changeFragment(requireContext(), fragment, TAG)
    }

    private fun eliminarProcedimiento(procedimiento: ProcedimientoModel) {
        DialogMaterialHelper.mostrarConfirmDeleteDialog(requireActivity(), "¿Eliminar procedimiento?") { confirmed ->
            if (confirmed) {
                FirebaseProcedimientoUtil.eliminarProcedimiento(procedimiento.id)
                    .addOnSuccessListener {
                        cargarProcedimientos()
                        DialogMaterialHelper.mostrarSuccessDialog(requireActivity(), "Procedimiento eliminado correctamente")
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
            FirebaseProcedimientoUtil.removerListener(it)
        }
        _binding = null
    }
} 