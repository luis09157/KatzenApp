package com.example.katzen.Fragment.Staff

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
import com.example.katzen.DataBaseFirebase.FirebaseAuthPerfilUtil
import com.example.katzen.Helper.ImageLoaderHelper
import com.example.katzen.LoginActivity
import com.example.katzen.MainActivity
import com.example.katzen.Helper.HelperUser
import com.example.katzen.Helper.StaffRoleHelper
import com.google.firebase.auth.FirebaseAuth
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.FragmentStaffPerfilBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StaffPerfilFragment : Fragment() {
    private var _binding: FragmentStaffPerfilBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStaffPerfilBinding.inflate(inflater, container, false)
        requireActivity().title = getString(R.string.perfil_mi_cuenta)
        bindStaticInfo()
        loadProfile()
        setupActions()
        return binding.root
    }

    private fun bindStaticInfo() {
        val versionName = requireContext().packageManager
            .getPackageInfo(requireContext().packageName, 0).versionName
        binding.tvVersion.text = getString(R.string.app_version_label, versionName)
    }

    private fun loadProfile() {
        val user = MainActivity._INFO_USER
        val authUser = FirebaseAuth.getInstance().currentUser
        val displayName = user.nombreUsuario.ifBlank {
            authUser?.displayName.orEmpty().ifBlank {
                authUser?.email?.substringBefore("@").orEmpty()
            }
        }
        binding.tvNombre.text = displayName.ifBlank { getString(R.string.perfil_staff_default) }

        val email = user.correo.ifBlank { authUser?.email.orEmpty() }
        bindInfoRow(binding.rowEmail.root, R.drawable.ic_perfil, R.string.cliente_label_correo, email)
        bindInfoRow(
            binding.rowCuenta.root,
            R.drawable.ic_account_outline,
            R.string.perfil_tipo_cuenta,
            getString(R.string.perfil_staff_label)
        )

        ImageLoaderHelper.load(
            imageView = binding.imgAvatar,
            imageUrl = user.imagenPerfil,
            placeholderRes = R.drawable.logo,
            errorRes = R.drawable.logo
        )

        val uid = HelperUser.getUserId().orEmpty()
        viewLifecycleOwner.lifecycleScope.launch {
            val perfil = withContext(Dispatchers.IO) {
                runCatching { FirebaseAuthPerfilUtil.obtenerPerfil(uid) }.getOrNull()
            }
            binding.chipRol.text = getString(StaffRoleHelper.roleLabelRes(perfil?.staffRole))
        }
    }

    private fun bindInfoRow(row: View, iconRes: Int, labelRes: Int, value: String) {
        row.findViewById<ImageView>(R.id.icon_info)?.setImageResource(iconRes)
        row.findViewById<TextView>(R.id.tv_info_label)?.setText(labelRes)
        row.findViewById<TextView>(R.id.tv_info_value)?.text =
            value.ifBlank { getString(R.string.perfil_no_disponible) }
    }

    private fun setupActions() {
        binding.btnResetPassword.setOnClickListener {
            val email = MainActivity._INFO_USER.correo.ifBlank {
                FirebaseAuth.getInstance().currentUser?.email.orEmpty()
            }
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
            (requireActivity() as? MainActivity)?.let { activity ->
                FirebaseAuth.getInstance().signOut()
                HelperUser._ID_USER = ""
                MainActivity._INFO_USER = com.example.katzen.Model.User()
                startActivity(Intent(activity, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                activity.finish()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
