package com.example.katzen.Fragment.Producto

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.AuxiliaresAdapter
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseAuxiliarUtil
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Model.AuxiliarModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.FragmentListaAuxiliaresBinding

class ListaAuxiliaresFragment : Fragment() {
    private var TAG  = "ListaAuxiliaresFragment"
    private var _binding: FragmentListaAuxiliaresBinding? = null
    private val binding get() = _binding!!
    private lateinit var auxiliaresAdapter: AuxiliaresAdapter
    private val auxiliaresList = mutableListOf<AuxiliarModel>()
    private val auxiliaresListOriginal = mutableListOf<AuxiliarModel>()
    private var valueEventListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListaAuxiliaresBinding.inflate(inflater, container, false)
        requireActivity().title = getString(R.string.submenu_productos_m_complementarios)
        initLoading()
        setupAdapter()
        setupListeners()
        setupSearchBar()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargarAuxiliares()
    }

    private fun initLoading() {
        ConfigLoading.init(
            binding.lottieAnimationView,
            binding.contAddAuxiliar,
            binding.fragmentNoData.contNoData
        )
    }

    private fun setupAdapter() {
        auxiliaresAdapter = AuxiliaresAdapter(auxiliaresList, { auxiliar ->
            editarAuxiliar(auxiliar)
        }, { auxiliar ->
            eliminarAuxiliar(auxiliar)
        })
        binding.lisMenuAuxiliares.adapter = auxiliaresAdapter
        binding.lisMenuAuxiliares.divider = null
    }

    private fun setupSearchBar() {
        binding.searchBar.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filtrarAuxiliares(s?.toString())
            }
        })
    }

    private fun filtrarAuxiliares(query: String?) {
        if (query.isNullOrEmpty()) {
            auxiliaresList.clear()
            auxiliaresList.addAll(auxiliaresListOriginal)
        } else {
            val filteredList = auxiliaresListOriginal.filter { auxiliar ->
                auxiliar.nombre.lowercase().contains(query.lowercase()) ||
                auxiliar.codigoInterno.lowercase().contains(query.lowercase())
            }
            auxiliaresList.clear()
            auxiliaresList.addAll(filteredList)
        }
        auxiliaresAdapter.notifyDataSetChanged()
    }

    private fun setupListeners() {
        binding.btnAddAuxiliar.setOnClickListener {
            UtilFragment.changeFragment(requireContext(), AddAuxiliarFragment(), TAG)
        }
        
        binding.lisMenuAuxiliares.setOnItemClickListener { _, _, position, _ ->
            editarAuxiliar(auxiliaresList[position])
        }
    }

    private fun cargarAuxiliares() {
        ConfigLoading.showLoadingAnimation()

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return

                auxiliaresList.clear()
                auxiliaresListOriginal.clear()
                for (auxiliarSnapshot in snapshot.children) {
                    val auxiliar = auxiliarSnapshot.getValue(AuxiliarModel::class.java)
                    auxiliar?.let {
                        auxiliaresList.add(it)
                        auxiliaresListOriginal.add(it)
                    }
                }
                auxiliaresAdapter.notifyDataSetChanged()

                if (auxiliaresList.size > 0) {
                    requireActivity().title = "${getString(R.string.submenu_productos_m_complementarios)} (${auxiliaresList.size})"
                    ConfigLoading.hideLoadingAnimation()
                } else {
                    ConfigLoading.showNodata()
                    binding.fragmentNoData.tvNoDataMessage.text = "No hay productos auxiliares registrados. Agrega uno nuevo con el botón inferior."
                    binding.fragmentNoData.btnAdd.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded) return
                ConfigLoading.showNodata()
                binding.fragmentNoData.tvNoDataMessage.text = "Error al cargar auxiliares: ${error.message}"
                binding.fragmentNoData.btnAdd.visibility = View.GONE
                DialogMaterialHelper.mostrarErrorDialog(
                    requireActivity(),
                    "Error al cargar los auxiliares: ${error.message}"
                )
            }
        }

        valueEventListener?.let {
            FirebaseAuxiliarUtil.obtenerListaAuxiliares(it)
        }
    }

    private fun editarAuxiliar(auxiliar: AuxiliarModel) {
        val fragment = AddAuxiliarFragment.newInstance(auxiliar)
        UtilFragment.changeFragment(requireContext(), fragment, TAG)
    }
    
    private fun eliminarAuxiliar(auxiliar: AuxiliarModel) {
        DialogMaterialHelper.mostrarConfirmDeleteDialog(requireActivity(), "¿Eliminar auxiliar?") { confirmed ->
            if (confirmed) {
                ConfigLoading.showLoadingAnimation()
                FirebaseAuxiliarUtil.eliminarAuxiliar(auxiliar.id)
                    .addOnSuccessListener {
                        ConfigLoading.hideLoadingAnimation()
                        cargarAuxiliares()
                        DialogMaterialHelper.mostrarSuccessDialog(requireActivity(), "Auxiliar eliminado correctamente")
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
            FirebaseAuxiliarUtil.removerListener(it)
        }
        _binding = null
    }
} 