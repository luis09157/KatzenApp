package com.example.katzen.Fragment.Cliente

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.katzen.Adapter.Cliente.ClienteAdapter
import com.example.katzen.Adapter.Paciente.PacienteListAdapter
import com.example.katzen.Config.ConfigLoading
import com.example.katzen.DataBaseFirebase.FirebaseClienteUtil
import com.example.katzen.DataBaseFirebase.FirebasePacienteUtil
import com.example.katzen.Model.ClienteModel
import com.example.katzen.Fragment.Paciente.AddPacienteFragment
import com.example.katzen.Fragment.Paciente.EditarPacienteFragment
import com.example.katzen.Fragment.Paciente.PacienteDetalleFragment
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.PortalInviteHelper
import com.example.katzen.Helper.ImageLoaderHelper
import com.example.katzen.Helper.ListScrollKeys
import com.example.katzen.Helper.ListUiHelper
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Model.PacienteModel
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.ClienteDetalleFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClienteDetalleFragment : Fragment() {
    val TAG: String = "ClienteDetalleFragment"

    private var _binding: ClienteDetalleFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var pacienteListAdapter: PacienteListAdapter
    private var listConfigured = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ClienteDetalleFragmentBinding.inflate(inflater, container, false)
        requireActivity().title = getString(R.string.menu_cliente_detalle)
        initLoading()
        setupList()
        listeners()
        loadData()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    UtilFragment.goBackOrHome(requireContext())
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) {
            loadData()
        }
    }

    private fun setupList() {
        if (listConfigured) return
        PacienteListAdapter.FLAG_IN_PACIENTE = true
        pacienteListAdapter = PacienteListAdapter(requireActivity()) { paciente ->
            EditarPacienteFragment.PACIENTE_EDIT = paciente
            UtilFragment.changeFragment(
                requireActivity(),
                PacienteDetalleFragment(),
                TAG,
                listKey = ListScrollKeys.CLIENTE_DETALLE_PACIENTES,
                listRecyclerView = binding.listaPacientes,
                selectedItemId = paciente.id
            )
        }
        ListUiHelper.setupVerticalList(binding.listaPacientes)
        binding.listaPacientes.adapter = pacienteListAdapter
        listConfigured = true
    }

    private fun loadData() {
        if (EditClienteFragment.CLIENTE_EDIT.nombre.isBlank()) {
            ConfigLoading.showNodata()
            return
        }

        ConfigLoading.showLoadingAnimation()
        viewLifecycleOwner.lifecycleScope.launch {
            val cliente = withContext(Dispatchers.IO) {
                FirebaseClienteUtil.obtenerClientePorId(EditClienteFragment.CLIENTE_EDIT.id)
            } ?: EditClienteFragment.CLIENTE_EDIT

            if (cliente.nombre.isBlank()) {
                ConfigLoading.showNodata()
                return@launch
            }

            EditClienteFragment.CLIENTE_EDIT = cliente
            bindClienteInfo(cliente)
            obtenerPacientes(cliente.id)
        }
    }

    private fun bindClienteInfo(cliente: ClienteModel) {
        binding.textNombreCliente.text = ClienteAdapter.formatNombreCompleto(cliente)
        binding.textExpediente.text = getString(
            R.string.cliente_expediente,
            cliente.expediente.ifBlank { "—" }
        )
        binding.textTelefono.text = cliente.telefono.ifBlank { "—" }
        binding.textCorreo.text = cliente.correo.ifBlank { "—" }
        binding.txtDireccion.text = formatDireccion(cliente)

        val resolvedImage = ImageLoaderHelper.resolveProfileImage(
            imageUrl = cliente.imageUrl,
            imageFileName = cliente.imageFileName
        )
        ImageLoaderHelper.load(
            imageView = binding.imgPerfil,
            imageUrl = resolvedImage.imageUrl,
            placeholderRes = R.drawable.avatar_sin_imagen,
            errorRes = R.drawable.avatar_sin_imagen,
            storageFolder = "Clientes",
            imageFileName = resolvedImage.imageFileName
        )
        bindPortalInfo(cliente)
    }

    private fun bindPortalInfo(cliente: ClienteModel) {
        if (cliente.tienePortalActivo()) {
            binding.textPortalEstado.text = getString(R.string.cliente_portal_active)
            binding.btnActivarPortal.text = getString(R.string.cliente_portal_reinvite)
            binding.btnActivarPortal.isEnabled = true
        } else if (cliente.correo.isBlank()) {
            binding.textPortalEstado.text = getString(R.string.cliente_portal_no_email)
            binding.btnActivarPortal.isEnabled = false
        } else {
            binding.textPortalEstado.text = getString(R.string.cliente_portal_inactive)
            binding.btnActivarPortal.text = getString(R.string.cliente_portal_activate)
            binding.btnActivarPortal.isEnabled = true
        }
    }

    private fun formatDireccion(cliente: ClienteModel): String {
        val partes = listOf(
            listOf(cliente.calle, cliente.numero.takeIf { it.isNotBlank() }?.let { "#$it" })
                .filterNotNull()
                .filter { it.isNotBlank() }
                .joinToString(" "),
            cliente.colonia,
            cliente.municipio
        ).filter { it.isNotBlank() }

        return partes.joinToString(", ").ifBlank { "—" }
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
            UtilFragment.changeFragment(requireActivity(), EditClienteFragment(), TAG)
        }
        binding.btnTelefono.setOnClickListener {
            UtilHelper.llamarCliente(requireActivity(), EditClienteFragment.CLIENTE_EDIT.telefono)
        }
        binding.btnGoogleMaps.setOnClickListener {
            UtilHelper.abrirGoogleMaps(requireActivity(), EditClienteFragment.CLIENTE_EDIT.urlGoogleMaps)
        }
        binding.btnWhatssap.setOnClickListener {
            UtilHelper.enviarMensajeWhatsApp(requireActivity(), EditClienteFragment.CLIENTE_EDIT.telefono)
        }
        binding.btnCorreo.setOnClickListener {
            UtilHelper.enviarCorreoElectronicoGmail(requireActivity(), EditClienteFragment.CLIENTE_EDIT.correo)
        }
        binding.btnAddCliente.setOnClickListener {
            val cliente = EditClienteFragment.CLIENTE_EDIT
            AddPacienteFragment.ADD_PACIENTE = PacienteModel().apply {
                idCliente = cliente.id
                nombreCliente = ClienteAdapter.formatNombreCompleto(cliente)
            }
            UtilFragment.changeFragment(requireActivity(), AddPacienteFragment(), TAG)
        }
        binding.btnActivarPortal.setOnClickListener {
            activarPortalCliente()
        }
    }

    private fun activarPortalCliente() {
        val cliente = EditClienteFragment.CLIENTE_EDIT
        if (cliente.correo.isBlank()) {
            DialogMaterialHelper.mostrarErrorDialog(
                requireActivity(),
                getString(R.string.cliente_portal_no_email)
            )
            return
        }

        DialogMaterialHelper.mostrarConfirmDialog(
            requireActivity(),
            getString(R.string.cliente_portal_confirm, cliente.correo)
        ) { confirmed ->
            if (!confirmed) return@mostrarConfirmDialog
            ConfigLoading.showLoadingAnimation()
            PortalInviteHelper.inviteClientePortal(
                clienteId = cliente.id,
                onSuccess = { message ->
                    ConfigLoading.hideLoadingAnimation()
                    DialogMaterialHelper.mostrarSuccessDialog(requireActivity(), message) {
                        loadData()
                    }
                },
                onFailure = { error ->
                    ConfigLoading.hideLoadingAnimation()
                    DialogMaterialHelper.mostrarErrorDialog(requireActivity(), error)
                }
            )
        }
    }

    private suspend fun obtenerPacientes(clienteId: String) {
        try {
            val pacientes = withContext(Dispatchers.IO) {
                FirebasePacienteUtil.obtenerPacientesDeCliente(clienteId)
            }
            mostrarPacientes(pacientes)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener pacientes del cliente", e)
            ConfigLoading.hideLoadingAnimation()
            DialogMaterialHelper.mostrarErrorDialog(
                requireActivity(),
                "No se pudieron cargar los pacientes: ${e.message ?: "Error desconocido"}"
            )
        }
    }

    private fun mostrarPacientes(pacientes: List<PacienteModel>) {
        pacienteListAdapter.updateList(pacientes)
        ListUiHelper.restoreScrollIfPending(
            ListScrollKeys.CLIENTE_DETALLE_PACIENTES,
            binding.listaPacientes,
            pacientes.map { it.id }
        )
        binding.tvEmptyPacientes.visibility =
            if (pacientes.isEmpty()) View.VISIBLE else View.GONE
        ConfigLoading.hideLoadingAnimation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listConfigured = false
        _binding = null
    }
}
