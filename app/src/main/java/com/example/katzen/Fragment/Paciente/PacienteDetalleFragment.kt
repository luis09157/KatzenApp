package com.example.katzen.Fragment.Paciente

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.katzen.Helper.ImageLoaderHelper
import com.example.katzen.Adapter.Cliente.ClienteListAdapter
import com.example.katzen.Adapter.MenuAdapter
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseClienteUtil
import com.example.katzen.Helper.CalendarioUtil
import com.example.katzen.Helper.ListUiHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Model.MenuModel
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.PacienteDetalleFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PacienteDetalleFragment : Fragment() {
    val TAG: String = "PacienteDetalleFragment"

    private var _binding: PacienteDetalleFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var clienteListAdapter: ClienteListAdapter
    private var adaptersInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PacienteDetalleFragmentBinding.inflate(inflater, container, false)
        requireActivity().title = getString(R.string.menu_paciente_detalle)
        initLoading()
        setupAdapters()
        listeners()
        refreshPacienteData()
        return binding.root
    }

    private fun setupAdapters() {
        if (adaptersInitialized) return
        clienteListAdapter = ClienteListAdapter(requireActivity())
        ListUiHelper.setupVerticalList(binding.listaPacientes)
        binding.listaPacientes.adapter = clienteListAdapter

        val menuList = listOf(
            MenuModel(requireActivity().getString(R.string.submenu_vacunas), R.drawable.img_vacunas),
            MenuModel(requireActivity().getString(R.string.submenu_historial_clinico), R.drawable.ic_recordatorio)
        )
        val menuAdapter = MenuAdapter { menu ->
            val idPaciente = EditarPacienteFragment.PACIENTE_EDIT.id
            val idCliente = EditarPacienteFragment.PACIENTE_EDIT.idCliente
            when (menu.titulo) {
                requireActivity().getString(R.string.submenu_vacunas) -> {
                    val fragment = VacunasListaFragment.newInstance(idPaciente, idCliente)
                    UtilFragment.changeFragment(requireContext(), fragment, TAG)
                }
                requireActivity().getString(R.string.submenu_historial_clinico) -> {
                    val fragment = StaffHistorialListFragment.newInstance(
                        idPaciente,
                        EditarPacienteFragment.PACIENTE_EDIT.nombre
                    )
                    UtilFragment.changeFragment(requireContext(), fragment, TAG)
                }
            }
        }
        menuAdapter.updateList(menuList)
        ListUiHelper.setupGridList(binding.menuOpciones, 2)
        binding.menuOpciones.adapter = menuAdapter
        adaptersInitialized = true
    }

    private fun refreshPacienteData() {
        val paciente = EditarPacienteFragment.PACIENTE_EDIT
        if (paciente.nombre.isBlank()) {
            ConfigLoading.showNodata()
            return
        }

        ConfigLoading.showLoadingAnimation()
        ImageLoaderHelper.load(
            imageView = binding.imgPerfil,
            imageUrl = paciente.imageUrl,
            placeholderRes = R.drawable.avatar_sin_imagen_mascota,
            errorRes = R.drawable.avatar_sin_imagen_mascota,
            storageFolder = "Mascotas",
            imageFileName = paciente.imageFileName
        )
        binding.textNombreCliente.text = paciente.nombre.ifBlank { "—" }
        binding.textEspecie.text = paciente.especie.ifBlank { "—" }
        binding.textRaza.text = paciente.raza.ifBlank { "—" }
        binding.textSexo.text = paciente.sexo.ifBlank { "—" }
        try {
            val (anios, meses) = CalendarioUtil.calcularEdadMascota(paciente.edad)
            binding.textEdad.text = when {
                anios == 0 && meses == 0 -> "—"
                anios == 0 -> "$meses meses"
                meses == 0 -> "$anios años"
                else -> "$anios años y $meses meses"
            }
        } catch (e: Exception) {
            binding.textEdad.text = "—"
            Log.w(TAG, "Error al calcular edad: ${e.message}")
        }

        val peso = paciente.peso.trim()
        binding.textPeso.text = when {
            peso.isBlank() || peso == "0" -> "—"
            else -> "$peso kg"
        }

        ConfigLoading.hideLoadingAnimation()
        cargarCliente(paciente.idCliente)
    }

    private fun cargarCliente(idCliente: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val cliente = withContext(Dispatchers.IO) {
                    FirebaseClienteUtil.obtenerClientePorId(idCliente)
                }
                if (cliente != null && isAdded) {
                    clienteListAdapter.updateList(listOf(cliente))
                }
            } catch (e: Exception) {
                if (isAdded) {
                    Log.e(TAG, "Error al obtener el cliente", e)
                }
            }
        }
    }

    private fun initLoading() {
        ConfigLoading.init(
            binding.lottieAnimationView,
            binding.contAddProducto,
            binding.fragmentNoData.contNoData
        )
    }

    private fun listeners() {
        binding.btnEdit.setOnClickListener {
            UtilFragment.changeFragment(requireActivity(), EditarPacienteFragment(), TAG)
        }
    }

    override fun onDestroyView() {
        adaptersInitialized = false
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        refreshPacienteData()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    UtilFragment.goBackOrHome(requireContext())
                }
            })
    }
}
