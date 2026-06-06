package com.example.katzen.Fragment.Portal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.katzen.DataBaseFirebase.FirebasePacienteUtil
import com.example.katzen.DataBaseFirebase.FirebasePortalUtil
import com.example.katzen.Helper.DataLoadUiHelper
import com.example.katzen.Helper.PortalExpedienteUi
import com.example.katzen.PortalMainActivity
import com.example.katzen.Model.PacienteModel
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.PortalMascotaDetalleFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PortalMascotaDetalleFragment : Fragment() {
    private var _binding: PortalMascotaDetalleFragmentBinding? = null
    private val binding get() = _binding!!

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
        _binding = PortalMascotaDetalleFragmentBinding.inflate(inflater, container, false)
        setupMenuCards()
        loadHub()
        return binding.root
    }

    private fun setupMenuCards() {
        setupMenuItem(
            binding.menuVacunas.root,
            PortalExpedienteUi.Section.VACUNAS,
            getString(R.string.portal_section_vacunas),
            getString(R.string.portal_menu_vacunas_hint)
        ) {
            (requireActivity() as PortalMainActivity).openExpedienteSection(
                PortalVacunasListFragment.newInstance(mascotaId, mascotaNombre)
            )
        }

        setupMenuItem(
            binding.menuCitas.root,
            PortalExpedienteUi.Section.CITAS,
            getString(R.string.portal_section_citas),
            getString(R.string.portal_menu_citas_hint)
        ) {
            (requireActivity() as PortalMainActivity).openExpedienteSection(
                PortalCitasListFragment.newInstance(mascotaId, mascotaNombre)
            )
        }

        setupMenuItem(
            binding.menuHistorial.root,
            PortalExpedienteUi.Section.HISTORIAL,
            getString(R.string.portal_section_historial),
            getString(R.string.portal_menu_historial_hint)
        ) {
            (requireActivity() as PortalMainActivity).openExpedienteSection(
                PortalHistorialListFragment.newInstance(mascotaId, mascotaNombre)
            )
        }
    }

    private fun setupMenuItem(
        root: View,
        section: PortalExpedienteUi.Section,
        title: String,
        defaultSubtitle: String,
        onClick: () -> Unit
    ) {
        PortalExpedienteUi.applyMenuItem(root, section)
        root.findViewById<TextView>(R.id.tv_menu_title)?.text = title
        root.findViewById<TextView>(R.id.tv_menu_subtitle)?.text = defaultSubtitle
        root.setOnClickListener { onClick() }
    }

    private fun loadHub() {
        if (mascotaId.isBlank()) return

        DataLoadUiHelper.showOverlayLoading(binding.portalLoading, binding.layoutHubContent)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val mascota = withContext(Dispatchers.IO) {
                    FirebasePacienteUtil.obtenerPacientePorId(mascotaId)
                }
                val counts = withContext(Dispatchers.IO) {
                    coroutineScope {
                        val vacunas = async {
                            runCatching { FirebasePortalUtil.obtenerVacunasPorMascota(mascotaId) }
                                .getOrDefault(emptyList())
                        }
                        val citas = async {
                            runCatching { FirebasePortalUtil.obtenerCitasPorMascota(mascotaId) }
                                .getOrDefault(emptyList())
                        }
                        val historiales = async {
                            runCatching { FirebasePortalUtil.obtenerHistorialesPorMascota(mascotaId) }
                                .getOrDefault(emptyList())
                        }
                        Triple(vacunas.await(), citas.await(), historiales.await())
                    }
                }

                bindMascota(mascota)
                bindStats(counts.first.size, counts.second.size, counts.third.size)
                updateMenuCount(binding.menuVacunas.root, counts.first.size)
                updateMenuCount(binding.menuCitas.root, counts.second.size)
                updateMenuCount(binding.menuHistorial.root, counts.third.size)
            } finally {
                _binding?.let { b ->
                    DataLoadUiHelper.hideOverlayLoading(b.portalLoading, b.layoutHubContent)
                }
            }
        }
    }

    private fun bindMascota(mascota: PacienteModel?) {
        mascotaNombre = mascota?.nombre?.ifBlank { mascotaNombre } ?: mascotaNombre
        binding.tvMascotaNombre.text = mascotaNombre.ifBlank { getString(R.string.portal_mascota) }
        binding.tvMascotaInfo.text = listOfNotNull(
            mascota?.especie?.takeIf { it.isNotBlank() },
            mascota?.raza?.takeIf { it.isNotBlank() },
            mascota?.sexo?.takeIf { it.isNotBlank() }
        ).joinToString(" · ").ifBlank { "—" }
    }

    private fun bindStats(vacunas: Int, citas: Int, historial: Int) {
        PortalExpedienteUi.applyStatPill(
            binding.statVacunas.root,
            PortalExpedienteUi.Section.VACUNAS,
            vacunas,
            getString(R.string.portal_section_vacunas)
        )
        PortalExpedienteUi.applyStatPill(
            binding.statCitas.root,
            PortalExpedienteUi.Section.CITAS,
            citas,
            getString(R.string.portal_section_citas)
        )
        PortalExpedienteUi.applyStatPill(
            binding.statHistorial.root,
            PortalExpedienteUi.Section.HISTORIAL,
            historial,
            getString(R.string.portal_stat_historial)
        )
    }

    private fun updateMenuCount(menuRoot: View, count: Int) {
        val badge = menuRoot.findViewById<TextView>(R.id.tv_count_badge)
        val subtitle = menuRoot.findViewById<TextView>(R.id.tv_menu_subtitle)
        badge?.text = count.toString()
        badge?.visibility = View.VISIBLE
        subtitle?.text = if (count == 0) {
            getString(R.string.portal_no_records)
        } else {
            resources.getQuantityString(R.plurals.portal_records_count, count, count)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_MASCOTA_ID = "mascota_id"
        private const val ARG_MASCOTA_NOMBRE = "mascota_nombre"

        fun newInstance(mascotaId: String, mascotaNombre: String = ""): PortalMascotaDetalleFragment {
            return PortalMascotaDetalleFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_MASCOTA_ID, mascotaId)
                    putString(ARG_MASCOTA_NOMBRE, mascotaNombre)
                }
            }
        }
    }
}
