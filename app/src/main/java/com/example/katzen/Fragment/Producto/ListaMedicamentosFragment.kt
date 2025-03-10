package com.example.katzen.Fragment.Producto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
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
    private val TAG = "ListaMedicamentosFragment"
    private var valueEventListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListaMedicamentosBinding.inflate(inflater, container, false)
        initLoading()
        setupRecyclerView()
        setupListeners()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargarMedicamentos()
    }

    private fun initLoading() {
        ConfigLoading.LOTTIE_ANIMATION_VIEW = binding.lottieAnimationView
        ConfigLoading.CONT_ADD_PRODUCTO = binding.contAddProducto
        ConfigLoading.FRAGMENT_NO_DATA = binding.fragmentNoData.contNoData
    }

    private fun setupRecyclerView() {
        medicamentosAdapter = MedicamentosAdapter(medicamentosList) { medicamento ->
            editarMedicamento(medicamento)
        }
        binding.rvMedicamentos.apply {
            adapter = medicamentosAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupListeners() {
        binding.fabAgregarMedicamento.setOnClickListener {
            UtilFragment.changeFragment(requireContext(), AddProductoMedicamentoFragment(), TAG)
        }
    }

    private fun cargarMedicamentos() {
        ConfigLoading.showLoadingAnimation()
        
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                
                medicamentosList.clear()
                for (medicamentoSnapshot in snapshot.children) {
                    val medicamento = medicamentoSnapshot.getValue(ProductoMedicamentoModel::class.java)
                    medicamento?.let { medicamentosList.add(it) }
                }
                medicamentosAdapter.notifyDataSetChanged()

                if (medicamentosList.size > 0) {
                    requireActivity().title = "${getString(R.string.submenu_productos_medicamentos)} (${medicamentosList.size})"
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

    override fun onDestroyView() {
        super.onDestroyView()
        valueEventListener?.let {
            FirebaseMedicamentoUtil.removerListener(it)
        }
        _binding = null
    }
} 