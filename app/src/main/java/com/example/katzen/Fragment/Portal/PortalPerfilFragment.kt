package com.example.katzen.Fragment.Portal

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.katzen.DataBaseFirebase.FirebaseClienteUtil
import com.example.katzen.Helper.DataLoadUiHelper
import com.example.katzen.Helper.ImageLoaderHelper
import com.example.katzen.LoginActivity
import com.example.katzen.Helper.PortalFcmHelper
import com.example.katzen.PortalMainActivity
import com.example.katzen.Helper.HelperUser
import com.example.katzen.Model.ClienteModel
import com.google.firebase.auth.FirebaseAuth
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.PortalPerfilFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PortalPerfilFragment : Fragment() {
    private var _binding: PortalPerfilFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PortalPerfilFragmentBinding.inflate(inflater, container, false)
        setupActions()
        loadCliente()
        return binding.root
    }

    private fun loadCliente() {
        val clienteId = PortalMainActivity.CLIENTE_ID
        if (clienteId.isBlank()) {
            binding.tvNombre.text = getString(R.string.portal_client_no_profile)
            return
        }

        DataLoadUiHelper.showOverlayLoading(binding.portalLoading, binding.layoutPerfilContent)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val cliente = withContext(Dispatchers.IO) {
                    FirebaseClienteUtil.obtenerClientePorId(clienteId)
                }
                val activeBinding = _binding ?: return@launch
                if (cliente != null) {
                    bindCliente(cliente)
                } else {
                    activeBinding.tvNombre.text = getString(R.string.portal_client_no_profile)
                }
            } catch (_: Exception) {
                _binding?.tvNombre?.text = getString(R.string.portal_load_error)
            } finally {
                _binding?.let { b ->
                    DataLoadUiHelper.hideOverlayLoading(b.portalLoading, b.layoutPerfilContent)
                }
            }
        }
    }

    private fun bindCliente(cliente: ClienteModel) {
        val nombreCompleto = listOf(
            cliente.nombre,
            cliente.apellidoPaterno,
            cliente.apellidoMaterno
        ).filter { it.isNotBlank() }.joinToString(" ")

        binding.tvNombre.text = nombreCompleto.ifBlank { getString(R.string.cliente_sin_nombre) }
        binding.chipExpediente.text = getString(R.string.cliente_expediente, cliente.expediente.ifBlank { "—" })

        bindInfoRow(binding.rowTelefono.root, R.string.cliente_label_telefono, cliente.telefono)
        bindInfoRow(binding.rowCorreo.root, R.string.cliente_label_correo, cliente.correo)

        val direccion = buildDireccion(cliente)
        bindInfoRow(binding.rowDireccion.root, R.string.cliente_label_direccion, direccion)

        binding.tvPortalStatus.text = if (cliente.tienePortalActivo()) {
            getString(
                R.string.portal_perfil_activo,
                cliente.fechaInvitacion.ifBlank { cliente.fecha.ifBlank { "—" } }
            )
        } else {
            getString(R.string.cliente_portal_inactive)
        }

        val resolvedImage = ImageLoaderHelper.resolveProfileImage(
            imageUrl = cliente.imageUrl,
            imageFileName = cliente.imageFileName
        )
        ImageLoaderHelper.load(
            imageView = binding.imgAvatar,
            imageUrl = resolvedImage.imageUrl,
            placeholderRes = R.drawable.avatar_sin_imagen,
            errorRes = R.drawable.avatar_sin_imagen,
            storageFolder = "Clientes",
            imageFileName = resolvedImage.imageFileName
        )

        (requireActivity() as? PortalMainActivity)?.updateClienteHeader(
            nombreCompleto,
            cliente.imageUrl,
            cliente.imageFileName
        )
    }

    private fun buildDireccion(cliente: ClienteModel): String {
        val parts = listOfNotNull(
            cliente.calle.takeIf { it.isNotBlank() }?.let { "$it ${cliente.numero}".trim() },
            cliente.colonia.takeIf { it.isNotBlank() },
            cliente.municipio.takeIf { it.isNotBlank() }
        )
        return parts.joinToString(", ").ifBlank { getString(R.string.perfil_no_disponible) }
    }

    private fun bindInfoRow(row: View, labelRes: Int, value: String) {
        row.findViewById<TextView>(R.id.tv_info_label)?.setText(labelRes)
        row.findViewById<TextView>(R.id.tv_info_value)?.text =
            value.ifBlank { getString(R.string.perfil_no_disponible) }
    }

    private fun setupActions() {
        binding.btnResetPassword.setOnClickListener {
            val email = FirebaseAuth.getInstance().currentUser?.email.orEmpty()
            if (email.isBlank()) {
                Toast.makeText(requireContext(), R.string.perfil_sin_correo, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.dialog_reset_password_title)
                .setMessage(getString(R.string.dialog_reset_password_message))
                .setPositiveButton(R.string.btn_save) { _, _ ->
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            val msg = if (task.isSuccessful) {
                                getString(R.string.msg_reset_email_sent)
                            } else {
                                task.exception?.localizedMessage
                                    ?: getString(R.string.msg_reset_email_error)
                            }
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                        }
                }
                .setNegativeButton(R.string.btn_cancelar, null)
                .show()
        }

        binding.btnLogout.setOnClickListener {
            PortalFcmHelper.clearPortalSubscription(PortalMainActivity.CLIENTE_ID)
            FirebaseAuth.getInstance().signOut()
            HelperUser._ID_USER = ""
            PortalMainActivity.CLIENTE_ID = ""
            startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
