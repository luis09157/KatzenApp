package com.example.katzen.Fragment.Paciente

import PacienteModel
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.katzen.Adapter.Paciente.PacienteListAdapter
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebasePacienteUtil
import com.example.katzen.DataBaseFirebase.FirebaseStorageManager
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.MenuFragment
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
        init()
        listeners()

        return root
    }

    private fun init() {
        ConfigLoading.showLoadingAnimation() // Show loading animation
        mascotasList = mutableListOf()
        pacienteListAdapter = PacienteListAdapter(requireActivity(), mascotasList)
        binding.lisMenuMascota.adapter = pacienteListAdapter
        binding.lisMenuMascota.divider = null
        PacienteListAdapter.FLAG_IN_PACIENTE = true

        obtenerMascotas()
    }

    private fun listeners() {
        binding.btnAddPaciente.setOnClickListener {
            AddPacienteFragment.ADD_PACIENTE = PacienteModel()
            FirebaseStorageManager.URI_IMG_SELECTED = Uri.EMPTY
            UtilFragment.changeFragment(requireActivity(), AddPacienteFragment(), TAG)
        }
        binding.buscarMascota.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // No se necesita implementación aquí, ya que filtramos a medida que el usuario escribe
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Aplicar el filtro del adaptador al escribir en el SearchView
                filterMascotas(newText.toString())
                return true
            }
        })
        binding.lisMenuMascota.setOnItemClickListener { _, _, i, _ ->
            EditarPacienteFragment.PACIENTE_EDIT = pacienteListAdapter.getItem(i)!!
            UtilFragment.changeFragment(requireActivity(), PacienteDetalleFragment(), TAG)
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
            FirebasePacienteUtil.obtenerListaMascotas(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        // Limpiar la lista antes de agregar los nuevos datos
                        mascotasList.clear()

                        // Recorrer los datos obtenidos y agregarlos a la lista de productos
                        for (productoSnapshot in snapshot.children) {
                            val producto = productoSnapshot.getValue(PacienteModel::class.java)
                            producto?.let { mascotasList.add(it) }
                        }

                        // Notificar al adaptador que los datos han cambiado
                        pacienteListAdapter.notifyDataSetChanged()

                        if (mascotasList.isNotEmpty()) {
                            requireActivity().title = "${getString(R.string.menu_paciente)} (${mascotasList.size})"
                            ConfigLoading.hideLoadingAnimation() // Hide loading animation
                        } else {
                            ConfigLoading.showNodata() // Show no data view
                        }
                    } catch (e: Exception) {
                        ConfigLoading.hideLoadingAnimation() // Hide loading animation on exception
                        ConfigLoading.showNodata() // Show no data view on exception
                        e.printStackTrace() // Log exception
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    ConfigLoading.hideLoadingAnimation() // Hide loading animation on error
                    ConfigLoading.showNodata() // Show no data view
                    // Manejar errores de la consulta a la base de datos
                    // Por ejemplo, mostrar un mensaje de error
                    error.toException().printStackTrace() // Log database error
                }
            })
        } catch (e: Exception) {
            ConfigLoading.hideLoadingAnimation() // Hide loading animation on exception
            ConfigLoading.showNodata() // Show no data view on exception
            e.printStackTrace() // Log exception
        }
    }

    private fun filterMascotas(text: String) {
        val filteredList = mascotasList.filter { mascota ->
            mascota.nombre.contains(text, ignoreCase = true)
        }
        pacienteListAdapter.updateList(filteredList)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                UtilFragment.changeFragment(requireContext(), MenuFragment(), TAG)
            }
        })
    }
}
