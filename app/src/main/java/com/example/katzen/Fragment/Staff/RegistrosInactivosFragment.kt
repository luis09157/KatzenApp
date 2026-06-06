package com.example.katzen.Fragment.Staff

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.katzen.Adapter.Cliente.ClienteAdapter
import com.example.katzen.Adapter.Staff.ReactivarAdapter
import com.example.katzen.Adapter.Staff.ReactivarItem
import com.example.katzen.DataBaseFirebase.FirebaseClienteUtil
import com.example.katzen.DataBaseFirebase.FirebasePacienteUtil
import com.example.katzen.Helper.DialogMaterialHelper
import com.example.katzen.Helper.ListUiHelper
import com.google.android.material.tabs.TabLayout
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.FragmentRegistrosInactivosBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistrosInactivosFragment : Fragment() {
    private var _binding: FragmentRegistrosInactivosBinding? = null
    private val binding get() = _binding!!
    private val adapter = ReactivarAdapter { item -> confirmarReactivacion(item) }
    private var showingClientes = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrosInactivosBinding.inflate(inflater, container, false)
        binding.screenHeader.tvHeaderTitle.text = getString(R.string.menu_registros_inactivos)
        binding.screenHeader.tvHeaderSubtitle.text = getString(R.string.inactivos_subtitle)
        binding.screenHeader.imgHeaderIcon.setImageResource(R.drawable.ic_no_data)
        ListUiHelper.setupVerticalList(binding.listaInactivos)
        binding.listaInactivos.adapter = adapter
        setupTabs()
        loadData()
        return binding.root
    }

    private fun setupTabs() {
        binding.tabInactivos.addTab(binding.tabInactivos.newTab().setText(R.string.tab_inactivos_clientes))
        binding.tabInactivos.addTab(binding.tabInactivos.newTab().setText(R.string.tab_inactivos_mascotas))
        binding.tabInactivos.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                showingClientes = tab?.position == 0
                loadData()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadData() {
        binding.lottieLoading.visibility = View.VISIBLE
        binding.listaInactivos.visibility = View.GONE
        binding.tvEmptyInactivos.visibility = View.GONE
        val mascotaFallback = getString(R.string.portal_mascota)
        viewLifecycleOwner.lifecycleScope.launch {
            val items = withContext(Dispatchers.IO) {
                if (showingClientes) {
                    FirebaseClienteUtil.obtenerClientesInactivos().map { cliente ->
                        ReactivarItem(
                            id = cliente.id,
                            titulo = ClienteAdapter.formatNombreCompleto(cliente),
                            subtitulo = "Exp. ${cliente.expediente.ifBlank { "—" }}"
                        )
                    }
                } else {
                    FirebasePacienteUtil.obtenerMascotasInactivas().map { mascota ->
                        ReactivarItem(
                            id = mascota.id,
                            titulo = mascota.nombre.ifBlank { mascotaFallback },
                            subtitulo = listOfNotNull(
                                mascota.especie.takeIf { it.isNotBlank() },
                                mascota.nombreCliente.takeIf { it.isNotBlank() }
                            ).joinToString(" · ").ifBlank { "—" }
                        )
                    }
                }
            }
            if (_binding == null) return@launch
            binding.lottieLoading.visibility = View.GONE
            adapter.submitList(items)
            val isEmpty = items.isEmpty()
            binding.listaInactivos.visibility = if (isEmpty) View.GONE else View.VISIBLE
            binding.tvEmptyInactivos.visibility = if (isEmpty) View.VISIBLE else View.GONE
        }
    }

    private fun confirmarReactivacion(item: ReactivarItem) {
        DialogMaterialHelper.mostrarConfirmDialog(
            requireActivity(),
            getString(R.string.reactivar_confirm, item.titulo)
        ) { confirmed ->
            if (!confirmed) return@mostrarConfirmDialog
            viewLifecycleOwner.lifecycleScope.launch {
                val result = withContext(Dispatchers.IO) {
                    if (showingClientes) {
                        FirebaseClienteUtil.reactivarCliente(item.id)
                    } else {
                        val (ok, msg) = FirebasePacienteUtil.reactivarMascota(item.id)
                        if (ok) Result.success(msg) else Result.failure(Exception(msg))
                    }
                }
                if (result.isSuccess) {
                    DialogMaterialHelper.mostrarSuccessDialog(
                        requireActivity(),
                        result.getOrNull().orEmpty()
                    )
                    loadData()
                } else {
                    DialogMaterialHelper.mostrarErrorDialog(
                        requireActivity(),
                        result.exceptionOrNull()?.message ?: getString(R.string.portal_load_error)
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
