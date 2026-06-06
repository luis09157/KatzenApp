package com.example.katzen.Fragment.Portal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.katzen.Adapter.Portal.PortalCitaAdapter
import com.example.katzen.DataBaseFirebase.FirebasePortalUtil
import com.example.katzen.Helper.DataLoadUiHelper
import com.example.katzen.Helper.ListUiHelper
import com.example.katzen.Helper.PortalExpedienteUi
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.PortalListSectionFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PortalCitasListFragment : Fragment() {
    private var _binding: PortalListSectionFragmentBinding? = null
    private val binding get() = _binding!!
    private val adapter = PortalCitaAdapter()

    private var mascotaId: String = ""
    private var mascotaNombre: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mascotaId = arguments?.getString(ARG_MASCOTA_ID).orEmpty()
        mascotaNombre = arguments?.getString(ARG_MASCOTA_NOMBRE).orEmpty()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PortalListSectionFragmentBinding.inflate(inflater, container, false)
        PortalExpedienteUi.applySectionHeader(binding, PortalExpedienteUi.Section.CITAS)
        binding.tvSectionTitle.text = getString(R.string.portal_section_citas)
        ListUiHelper.setupVerticalList(binding.listaSection)
        binding.listaSection.adapter = adapter
        loadData()
        return binding.root
    }

    private fun loadData() {
        DataLoadUiHelper.showSectionLoading(binding.sectionLoading, binding.groupSectionContent)
        viewLifecycleOwner.lifecycleScope.launch {
            val items = withContext(Dispatchers.IO) {
                runCatching { FirebasePortalUtil.obtenerCitasPorMascota(mascotaId) }
                    .getOrDefault(emptyList())
                    .sortedByDescending { it.fecha_hora }
            }
            if (_binding == null) return@launch
            DataLoadUiHelper.hideSectionLoading(binding.sectionLoading, binding.groupSectionContent)
            binding.tvSectionSubtitle.text = getString(
                R.string.portal_section_for_pet,
                mascotaNombre.ifBlank { getString(R.string.portal_mascota) },
                resources.getQuantityString(R.plurals.portal_records_count, items.size, items.size)
            )
            adapter.submitList(items)
            val isEmpty = items.isEmpty()
            binding.listaSection.visibility = if (isEmpty) View.GONE else View.VISIBLE
            PortalExpedienteUi.setEmptyState(
                binding.layoutEmptySection,
                isEmpty,
                getString(R.string.portal_empty_citas),
                getString(R.string.portal_empty_section_hint)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_MASCOTA_ID = "mascota_id"
        private const val ARG_MASCOTA_NOMBRE = "mascota_nombre"

        fun newInstance(mascotaId: String, mascotaNombre: String): PortalCitasListFragment {
            return PortalCitasListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_MASCOTA_ID, mascotaId)
                    putString(ARG_MASCOTA_NOMBRE, mascotaNombre)
                }
            }
        }
    }
}
