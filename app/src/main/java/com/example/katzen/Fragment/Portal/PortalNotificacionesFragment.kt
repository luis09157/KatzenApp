package com.example.katzen.Fragment.Portal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.katzen.Adapter.Portal.PortalNotificacionAdapter
import com.example.katzen.DataBaseFirebase.FirebasePortalUtil
import com.example.katzen.Helper.DataLoadUiHelper
import com.example.katzen.Helper.ListUiHelper
import com.example.katzen.Helper.PortalDeepLinkHelper
import com.example.katzen.Helper.PortalNotificacionUi
import com.example.katzen.PortalMainActivity
import com.ninodev.katzen.databinding.PortalNotificacionesFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PortalNotificacionesFragment : Fragment() {
    private var _binding: PortalNotificacionesFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PortalNotificacionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PortalNotificacionesFragmentBinding.inflate(inflater, container, false)
        adapter = PortalNotificacionAdapter { item ->
            if (!item.leida && item.id.isNotBlank()) {
                viewLifecycleOwner.lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        runCatching {
                            FirebasePortalUtil.marcarNotificacionLeida(
                                PortalMainActivity.CLIENTE_ID,
                                item.id
                            )
                        }
                    }
                    loadNotificaciones()
                }
            }
            val activity = activity as? PortalMainActivity ?: return@PortalNotificacionAdapter
            PortalDeepLinkHelper.navigate(
                activity,
                PortalDeepLinkHelper.targetFromNotificacion(item)
            )
        }
        ListUiHelper.setupVerticalList(binding.listaNotificacionesPortal)
        binding.listaNotificacionesPortal.adapter = adapter
        loadNotificaciones()
        return binding.root
    }

    private fun loadNotificaciones() {
        val clienteId = PortalMainActivity.CLIENTE_ID
        if (clienteId.isBlank()) {
            DataLoadUiHelper.setGroupVisible(binding.groupNotifContent, true)
            showEmptyState(true, 0, 0)
            return
        }

        DataLoadUiHelper.showSectionLoading(binding.notifLoading, binding.groupNotifContent)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val items = withContext(Dispatchers.IO) {
                    FirebasePortalUtil.obtenerNotificaciones(clienteId)
                }
                if (_binding == null) return@launch
                DataLoadUiHelper.hideSectionLoading(binding.notifLoading, binding.groupNotifContent)
                val unread = items.count { !it.leida }
                PortalNotificacionUi.bindHeader(
                    binding.tvNotifHeaderSubtitle,
                    binding.tvUnreadBadge,
                    unread,
                    items.size,
                    requireContext()
                )
                adapter.submitList(items)
                showEmptyState(items.isEmpty(), unread, items.size)
            } catch (_: Exception) {
                _binding?.let { b ->
                    DataLoadUiHelper.hideSectionLoading(b.notifLoading, b.groupNotifContent)
                    showEmptyState(true, 0, 0)
                }
            }
        }
    }

    private fun showEmptyState(show: Boolean, unread: Int, total: Int) {
        val activeBinding = _binding ?: return
        activeBinding.layoutEmptyNotificaciones.visibility = if (show) View.VISIBLE else View.GONE
        activeBinding.listaNotificacionesPortal.visibility = if (show) View.GONE else View.VISIBLE
        activeBinding.tvNotifOrderHint.visibility = if (show) View.GONE else View.VISIBLE
        if (show) {
            PortalNotificacionUi.bindHeader(
                activeBinding.tvNotifHeaderSubtitle,
                activeBinding.tvUnreadBadge,
                unread,
                total,
                requireContext()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
