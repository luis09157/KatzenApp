package com.example.katzen.Fragment.Producto

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.MedicamentosAdapter
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseMedicamentoUtil
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Model.ProductoMedicamentoModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.FragmentListaMedicamentosBinding

class ListaMedicamentosFragment : Fragment() {
    private var _binding: FragmentListaMedicamentosBinding? = null
    private val binding get() = _binding!!
    private lateinit var medicamentosAdapter: MedicamentosAdapter
    private val medicamentosList = mutableListOf<ProductoMedicamentoModel>()
    private val medicamentosListOriginal = mutableListOf<ProductoMedicamentoModel>()
    private val TAG = "ListaMedicamentosFragment"
    private var valueEventListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListaMedicamentosBinding.inflate(inflater, container, false)
        initLoading()
        setupAdapter()
        setupListeners()
        setupSearchBar()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargarMedicamentos()
    }

    fun initLoading() {
        ConfigLoading.init(
            binding.lottieAnimationView,
            binding.contAddProducto,
            binding.fragmentNoData.contNoData
        )
    }


    private fun setupAdapter() {
        medicamentosAdapter = MedicamentosAdapter(medicamentosList, { medicamento ->
            editarMedicamento(medicamento)
        }, { medicamento ->
            eliminarMedicamento(medicamento)
        })
        binding.lisMenuMedicamentos.adapter = medicamentosAdapter
        binding.lisMenuMedicamentos.divider = null
    }

    private fun setupListeners() {
        binding.btnAddMedicamento.setOnClickListener {
            UtilFragment.changeFragment(requireContext(), AddProductoMedicamentoFragment(), TAG)
        }
        
        binding.lisMenuMedicamentos.setOnItemClickListener { _, _, position, _ ->
            editarMedicamento(medicamentosList[position])
        }
    }

    private fun setupSearchBar() {
        binding.searchBar.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filtrarMedicamentos(s?.toString())
            }
        })
    }

    private fun filtrarMedicamentos(query: String?) {
        if (query.isNullOrEmpty()) {
            medicamentosList.clear()
            medicamentosList.addAll(medicamentosListOriginal)
        } else {
            val filteredList = medicamentosListOriginal.filter { medicamento ->
                medicamento.nombre?.lowercase()?.contains(query.lowercase()) == true ||
                medicamento.codigoInterno?.lowercase()?.contains(query.lowercase()) == true
            }
            medicamentosList.clear()
            medicamentosList.addAll(filteredList)
        }
        medicamentosAdapter.notifyDataSetChanged()
    }

    private fun cargarMedicamentos() {
        ConfigLoading.showLoadingAnimation()
        
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                
                medicamentosList.clear()
                medicamentosListOriginal.clear()
                for (medicamentoSnapshot in snapshot.children) {
                    val medicamento = medicamentoSnapshot.getValue(ProductoMedicamentoModel::class.java)
                    medicamento?.let {
                        medicamentosList.add(it)
                        medicamentosListOriginal.add(it)
                    }
                }
                medicamentosAdapter.notifyDataSetChanged()

                if (medicamentosList.size > 0) {
                    requireActivity().title = "${getString(R.string.submenu_productos_medicamentos)} (${medicamentosList.size})"
                    ConfigLoading.hideLoadingAnimation()
                } else {
                    ConfigLoading.showNodata()
                    binding.fragmentNoData.tvNoDataMessage.text = "No hay medicamentos registrados. Agrega uno nuevo con el botón inferior."
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded) return
                ConfigLoading.showNodata()
                binding.fragmentNoData.tvNoDataMessage.text = "Error al cargar medicamentos: ${error.message}"
                DialogMaterialHelper.mostrarErrorDialog(
                    requireActivity(),
                    "Error al cargar los medicamentos: ${error.message}"
                )
            }
        }
        
        valueEventListener?.let {
            FirebaseMedicamentoUtil.obtenerListaMedicamentos(it)
        }
    }

    private fun editarMedicamento(medicamento: ProductoMedicamentoModel) {
        val fragment = AddProductoMedicamentoFragment.newInstance(medicamento)
        UtilFragment.changeFragment(requireContext(), fragment, TAG)
    }

    private fun eliminarMedicamento(medicamento: ProductoMedicamentoModel) {
        DialogMaterialHelper.mostrarConfirmDeleteDialog(requireActivity(), "¿Eliminar medicamento?") { confirmed ->
            if (confirmed) {
                ConfigLoading.showLoadingAnimation()
                FirebaseMedicamentoUtil.eliminarMedicamento(medicamento.id)
                    .addOnSuccessListener {
                        ConfigLoading.hideLoadingAnimation()
                        cargarMedicamentos()
                        DialogMaterialHelper.mostrarSuccessDialog(requireActivity(), "Medicamento eliminado correctamente")
                    }
                    .addOnFailureListener { e ->
                        ConfigLoading.hideLoadingAnimation()
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
            FirebaseMedicamentoUtil.removerListener(it)
        }
        _binding = null
    }
} 