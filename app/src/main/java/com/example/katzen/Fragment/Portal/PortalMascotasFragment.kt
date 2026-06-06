package com.example.katzen.Fragment.Portal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.katzen.Adapter.Portal.PortalMascotaAdapter
import com.example.katzen.DataBaseFirebase.FirebaseClienteUtil
import com.example.katzen.DataBaseFirebase.FirebasePacienteUtil
import com.example.katzen.Helper.DataLoadUiHelper
import com.example.katzen.Helper.ListUiHelper
import com.example.katzen.PortalMainActivity
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.PortalMascotasFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PortalMascotasFragment : Fragment() {
    private var _binding: PortalMascotasFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PortalMascotaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PortalMascotasFragmentBinding.inflate(inflater, container, false)
        setupList()
        loadMascotas()
        return binding.root
    }

    private fun setupList() {
        adapter = PortalMascotaAdapter(requireActivity()) { mascota ->
            (requireActivity() as PortalMainActivity).openMascotaDetalle(
                PortalMascotaDetalleFragment.newInstance(mascota.id, mascota.nombre)
            )
        }
        ListUiHelper.setupVerticalList(binding.listaMascotasPortal)
        binding.listaMascotasPortal.adapter = adapter
    }

    private fun loadMascotas() {
        val clienteId = PortalMainActivity.CLIENTE_ID
        if (clienteId.isBlank()) {
            DataLoadUiHelper.setGroupVisible(binding.groupMascotasContent, true)
            binding.tvEmptyMascotasPortal.visibility = View.VISIBLE
            return
        }

        DataLoadUiHelper.showSectionLoading(binding.portalLoading, binding.groupMascotasContent)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val mascotas = withContext(Dispatchers.IO) {
                    FirebasePacienteUtil.obtenerPacientesDeCliente(clienteId)
                }
                val cliente = withContext(Dispatchers.IO) {
                    runCatching { FirebaseClienteUtil.obtenerClientePorId(clienteId) }.getOrNull()
                }

                val activeBinding = _binding ?: return@launch
                DataLoadUiHelper.hideSectionLoading(activeBinding.portalLoading, activeBinding.groupMascotasContent)
                bindWelcome(activeBinding, cliente?.nombre.orEmpty(), mascotas.size)
                adapter.submitList(mascotas)
                activeBinding.tvEmptyMascotasPortal.visibility =
                    if (mascotas.isEmpty()) View.VISIBLE else View.GONE
            } catch (_: Exception) {
                _binding?.let { b ->
                    DataLoadUiHelper.hideSectionLoading(b.portalLoading, b.groupMascotasContent)
                    b.tvEmptyMascotasPortal.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun bindWelcome(
        activeBinding: PortalMascotasFragmentBinding,
        primerNombre: String,
        count: Int
    ) {
        val greeting = if (primerNombre.isNotBlank()) {
            getString(R.string.menu_welcome_user, primerNombre)
        } else {
            getString(R.string.portal_welcome_generic)
        }
        activeBinding.tvPortalGreeting.text = greeting
        activeBinding.chipMascotasCount.text = resources.getQuantityString(
            R.plurals.portal_mascotas_count,
            count,
            count
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
