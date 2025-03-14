package com.example.katzen.Fragment.Producto

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.AlimentosAdapter
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilFragment
import com.ninodev.katzen.DataBaseFirebase.FirebaseAlimentoUtil
import com.ninodev.katzen.Model.ProductoAlimentoModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.FragmentListaAlimentosBinding

class ListaAlimentosFragment : Fragment() {
    private var _binding: FragmentListaAlimentosBinding? = null
    private val binding get() = _binding!!
    private lateinit var alimentosAdapter: AlimentosAdapter
    private val alimentosList = mutableListOf<ProductoAlimentoModel>()
    private val alimentosListOriginal = mutableListOf<ProductoAlimentoModel>()
    private val TAG = "ListaAlimentosFragment"
    private var valueEventListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListaAlimentosBinding.inflate(inflater, container, false)
        requireActivity().title = getString(R.string.submenu_productos_alimentos)
        initLoading()
        setupAdapter()
        setupListeners()
        setupSearchBar()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargarAlimentos()
    }

    private fun initLoading() {
        ConfigLoading.init(
            binding.lottieAnimationView,
            binding.contAddProducto,
            binding.fragmentNoData.contNoData
        )
    }

    private fun setupAdapter() {
        alimentosAdapter = AlimentosAdapter(alimentosList, { alimento ->
            editarAlimento(alimento)
        }, { alimento ->
            eliminarAlimento(alimento)
        })
        binding.lisMenuAlimentos.adapter = alimentosAdapter
        binding.lisMenuAlimentos.divider = null
    }

    private fun setupSearchBar() {
        binding.searchBar.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filtrarAlimentos(s?.toString())
            }
        })
    }

    private fun filtrarAlimentos(query: String?) {
        if (query.isNullOrEmpty()) {
            alimentosList.clear()
            alimentosList.addAll(alimentosListOriginal)
        } else {
            val filteredList = alimentosListOriginal.filter { alimento ->
                alimento.nombre.lowercase().contains(query.lowercase()) ||
                alimento.codigoInterno.lowercase().contains(query.lowercase())
            }
            alimentosList.clear()
            alimentosList.addAll(filteredList)
        }
        alimentosAdapter.updateList(alimentosList)
    }

    private fun setupListeners() {
        binding.btnAddAlimento.setOnClickListener {
            UtilFragment.changeFragment(requireContext(), AddProductoAlimentoFragment(), TAG)
        }
        
        binding.lisMenuAlimentos.setOnItemClickListener { _, _, position, _ ->
            editarAlimento(alimentosList[position])
        }
    }

    private fun cargarAlimentos() {
        ConfigLoading.showLoadingAnimation()
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                
                alimentosList.clear()
                alimentosListOriginal.clear()
                for (alimentoSnapshot in snapshot.children) {
                    val alimento = alimentoSnapshot.getValue(ProductoAlimentoModel::class.java)
                    alimento?.let { 
                        alimentosList.add(it)
                        alimentosListOriginal.add(it)
                    }
                }
                alimentosAdapter.updateList(alimentosList)

                if (alimentosList.size > 0) {
                    requireActivity().title = "${getString(R.string.submenu_productos_alimentos)} (${alimentosList.size})"
                    ConfigLoading.hideLoadingAnimation()
                } else {
                    ConfigLoading.showNodata()
                    binding.fragmentNoData.tvNoDataMessage.text = "No hay alimentos registrados. Agrega uno nuevo con el botón inferior."
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded) return
                ConfigLoading.showNodata()
                binding.fragmentNoData.tvNoDataMessage.text = "Error al cargar alimentos: ${error.message}"
                DialogMaterialHelper.mostrarErrorDialog(
                    requireActivity(),
                    "Error al cargar los alimentos: ${error.message}"
                )
            }
        }
        
        valueEventListener = listener
        FirebaseAlimentoUtil.obtenerListaAlimentos(listener)
    }

    private fun editarAlimento(alimento: ProductoAlimentoModel) {
        val fragment = AddProductoAlimentoFragment.newInstance(alimento)
        UtilFragment.changeFragment(requireContext(), fragment, TAG)
    }

    private fun eliminarAlimento(alimento: ProductoAlimentoModel) {
        DialogMaterialHelper.mostrarConfirmDeleteDialog(requireActivity(), "¿Eliminar alimento?") { confirmed ->
            if (confirmed) {
                FirebaseAlimentoUtil.eliminarAlimento(alimento.id)
                    .addOnSuccessListener {
                        cargarAlimentos()
                        DialogMaterialHelper.mostrarSuccessDialog(requireActivity(), "Alimento eliminado correctamente")
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
            FirebaseAlimentoUtil.removerListener(it)
        }
        _binding = null
    }
} 