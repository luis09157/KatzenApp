package com.example.katzen.Fragment.Paciente

import com.example.katzen.Model.PacienteModel
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.Paciente.PacienteListAdapter
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebasePacienteUtil
import com.example.katzen.DataBaseFirebase.FirebaseStorageManager
import com.example.katzen.Helper.RtdbActiveRecords
import com.example.katzen.Helper.FirebaseUiHelper
import com.example.katzen.Helper.ListScrollKeys
import com.example.katzen.Helper.ListUiHelper
import com.example.katzen.Helper.SearchUiHelper
import com.example.katzen.Helper.UtilFragment
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.PacienteFragmentBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class PacienteFragment : Fragment() {
    val TAG: String = "PacienteFragment"

    private var _binding: PacienteFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var mascotasList: MutableList<PacienteModel>
    private lateinit var pacienteListAdapter: PacienteListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PacienteFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        requireActivity().title = getString(R.string.menu_paciente)

        initLoading()
        setupScreenHeader()
        init()
        listeners()

        return root
    }

    private fun setupScreenHeader() {
        binding.screenHeader.tvHeaderTitle.text = getString(R.string.staff_header_pacientes_title)
        binding.screenHeader.tvHeaderSubtitle.text = getString(R.string.staff_header_pacientes_sub)
        binding.screenHeader.imgHeaderIcon.setImageResource(R.drawable.ic_pet)
    }

    private fun init() {
        ConfigLoading.showLoadingAnimation() // Show loading animation
        mascotasList = mutableListOf()
        pacienteListAdapter = PacienteListAdapter(requireActivity()) { paciente ->
            EditarPacienteFragment.PACIENTE_EDIT = paciente
            UtilFragment.changeFragment(
                requireActivity(),
                PacienteDetalleFragment(),
                TAG,
                listKey = ListScrollKeys.PACIENTES,
                listRecyclerView = binding.lisMenuMascota,
                selectedItemId = paciente.id
            )
        }
        ListUiHelper.setupVerticalList(binding.lisMenuMascota)
        binding.lisMenuMascota.adapter = pacienteListAdapter
        PacienteListAdapter.FLAG_IN_PACIENTE = true

        obtenerMascotas()
    }

    private fun listeners() {
        binding.btnAddPaciente.setOnClickListener {
            AddPacienteFragment.ADD_PACIENTE = PacienteModel()
            FirebaseStorageManager.URI_IMG_SELECTED = Uri.EMPTY
            UtilFragment.changeFragment(requireActivity(), AddPacienteFragment(), TAG)
        }
        SearchUiHelper.bindSearch(binding.searchBar.searchEditText) { query ->
            filterMascotas(query)
        }
    }

    fun initLoading() {
        ConfigLoading.init(
            binding.lottieAnimationView,
            binding.contAddProducto,
            binding.fragmentNoData.contNoData
        )
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun obtenerMascotas() {
        try {
            FirebasePacienteUtil.obtenerListaMascotasUnaVez(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        mascotasList.clear()

                        for (productoSnapshot in snapshot.children) {
                            if (!RtdbActiveRecords.isActive(productoSnapshot)) continue
                            val producto = FirebasePacienteUtil.parsePaciente(productoSnapshot)
                            producto?.let { mascotasList.add(it) }
                        }

                        pacienteListAdapter.updateList(mascotasList)
                        ListUiHelper.restoreScrollIfPending(
                            ListScrollKeys.PACIENTES,
                            binding.lisMenuMascota,
                            mascotasList.map { it.id }
                        )

                        if (mascotasList.isNotEmpty()) {
                            requireActivity().title = "${getString(R.string.menu_paciente)} (${mascotasList.size})"
                            ConfigLoading.hideLoadingAnimation()
                        } else {
                            ConfigLoading.showNodata()
                        }
                    } catch (e: Exception) {
                        ConfigLoading.hideLoadingAnimation()
                        ConfigLoading.showNodata()
                        e.printStackTrace()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    FirebaseUiHelper.handleLoadError(requireContext(), error) {
                        obtenerMascotas()
                    }
                }
            })
        } catch (e: Exception) {
            ConfigLoading.hideLoadingAnimation()
            ConfigLoading.showNodata()
            e.printStackTrace()
        }
    }

    private fun filterMascotas(text: String) {
        val filteredList = mascotasList.filter { mascota ->
            mascota.nombre.contains(text, ignoreCase = true)
        }
        pacienteListAdapter.updateList(filteredList)
        ListUiHelper.restoreScrollIfPending(
            ListScrollKeys.PACIENTES,
            binding.lisMenuMascota,
            filteredList.map { it.id }
        )
    }

    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                UtilFragment.goHome(requireContext())
            }
        })
    }
}
