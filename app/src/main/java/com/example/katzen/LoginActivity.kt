package com.example.katzen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.katzen.Helper.AuthRoleHelper
import com.example.katzen.Helper.AuthSessionHelper
import com.example.katzen.Helper.FirebaseMonitoringHelper
import com.example.katzen.Helper.HelperUser
import com.example.katzen.Helper.UtilHelper
import com.example.katzen.Helper.UtilHelper.Companion.hideKeyboard
import com.ninodev.katzen.databinding.ActivityLoginBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.ninodev.katzen.R

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding
    private var doubleBackToExitPressedOnce = false
    private var sessionCheckStarted = false

    companion object {
        var _FLAG_IS_REGISTRO = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        title = getString(R.string.nav_home)

        setupBackPress()
        showBootstrap()
        listeners()
        checkExistingSession()
    }

    private fun setupBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isBootstrapVisible()) {
                    if (doubleBackToExitPressedOnce) {
                        finish()
                        return
                    }
                    doubleBackToExitPressedOnce = true
                    Snackbar.make(binding.root, getString(R.string.snackbar_exit_prompt), Snackbar.LENGTH_LONG).show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        doubleBackToExitPressedOnce = false
                    }, 2000)
                    return
                }

                if (doubleBackToExitPressedOnce) {
                    finish()
                    return
                }
                doubleBackToExitPressedOnce = true
                Snackbar.make(binding.root, getString(R.string.snackbar_exit_prompt), Snackbar.LENGTH_LONG).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    doubleBackToExitPressedOnce = false
                }, 2000)
            }
        })
    }

    private fun checkExistingSession() {
        if (sessionCheckStarted) return
        sessionCheckStarted = true

        if (!HelperUser.isUserLoggedIn()) {
            showLoginForm()
            return
        }

        showBootstrap()
        AuthSessionHelper.bootstrap(
            onAuthenticated = { session -> navigateForSession(session) },
            onUnauthenticated = { showLoginForm() },
            onInvalidSession = {
                showLoginForm()
                UtilHelper.showAlert(this, getString(R.string.auth_no_profile_assigned))
            }
        )
    }

    private fun listeners() {
        binding.txtForgotPassword.setOnClickListener {
            showPasswordResetDialog()
        }

        binding.btnLogin.setOnClickListener {
            binding.root.hideKeyboard()
            val email = binding.txtCorreo.editText?.text.toString().trim()
            val password = binding.txtPassword.editText?.text.toString().trim()

            if (email.isEmpty()) {
                UtilHelper.showAlert(this, getString(R.string.msg_login_empty_email))
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                UtilHelper.showAlert(this, getString(R.string.msg_login_invalid_email))
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                UtilHelper.showAlert(this, getString(R.string.msg_login_empty_password))
                return@setOnClickListener
            }

            if (password.length < 6) {
                UtilHelper.showAlert(this, getString(R.string.msg_login_short_password))
                return@setOnClickListener
            }

            showBootstrap(getString(R.string.login_signing_in))
            signIn(email, password)
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.root.hideKeyboard()
                if (task.isSuccessful) {
                    FirebaseMonitoringHelper.logLogin(true)
                    AuthSessionHelper.bootstrap(
                        onAuthenticated = { session -> navigateForSession(session) },
                        onUnauthenticated = {
                            hideBootstrap()
                            showLoginForm()
                        },
                        onInvalidSession = {
                            hideBootstrap()
                            showLoginForm()
                            UtilHelper.showAlert(this, getString(R.string.auth_no_profile_assigned))
                        }
                    )
                } else {
                    hideBootstrap()
                    showLoginForm()
                    FirebaseMonitoringHelper.logLogin(false)
                    FirebaseMonitoringHelper.recordError(
                        "Login fallido",
                        task.exception
                    )
                    val message = task.exception?.localizedMessage
                        ?: getString(R.string.msg_login_failed)
                    UtilHelper.showAlert(this, message)
                }
            }
    }

    private fun navigateForSession(session: AuthRoleHelper.PortalSession) {
        when {
            session.needsRolePicker() -> showDualRolePicker(session)
            session.isClient() -> openPortal(session.clienteId)
            session.isStaff() -> openMain()
        }
    }

    private fun showDualRolePicker(session: AuthRoleHelper.PortalSession) {
        hideBootstrap()
        MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
            .setTitle(getString(R.string.auth_dual_role_title))
            .setMessage(getString(R.string.auth_dual_role_message))
            .setPositiveButton(getString(R.string.auth_dual_role_staff)) { _, _ ->
                openMain()
            }
            .setNegativeButton(getString(R.string.auth_dual_role_portal)) { _, _ ->
                openPortal(session.clienteId)
            }
            .setCancelable(false)
            .show()
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(MainActivity.EXTRA_FORCE_STAFF, true)
        })
        finish()
    }

    private fun openPortal(clienteId: String = "") {
        if (clienteId.isNotBlank()) {
            com.example.katzen.Helper.PortalSessionBridge.clienteId = clienteId
        }
        startActivity(Intent(this, PortalMainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(PortalMainActivity.EXTRA_FORCE_PORTAL, true)
        })
        finish()
    }

    private fun isBootstrapVisible(): Boolean {
        return binding.layoutBootstrap.visibility == View.VISIBLE
    }

    private fun showBootstrap(message: String? = null) {
        binding.layoutBootstrap.visibility = View.VISIBLE
        binding.loginFormContainer.visibility = View.GONE
        binding.lottieAnimationView.visibility = View.VISIBLE
        binding.tvBootstrapMessage.text = message ?: getString(R.string.login_verifying_session)
        binding.btnLogin.isEnabled = false
    }

    private fun hideBootstrap() {
        binding.layoutBootstrap.visibility = View.GONE
        binding.lottieAnimationView.visibility = View.GONE
        binding.btnLogin.isEnabled = true
    }

    private fun showLoginForm() {
        hideBootstrap()
        binding.loginFormContainer.visibility = View.VISIBLE
    }

    private fun showPasswordResetDialog() {
        val builder = MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
            .setTitle(getString(R.string.dialog_reset_password_title))
            .setMessage(getString(R.string.dialog_reset_password_message))

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        input.hint = getString(R.string.input_hint_email)
        input.setText(binding.txtCorreo.editText?.text?.toString()?.trim().orEmpty())
        builder.setView(input)

        builder.setPositiveButton(getString(R.string.btn_send)) { dialog, _ ->
            val email = input.text.toString().trim()
            if (email.isEmpty()) {
                Snackbar.make(binding.root, getString(R.string.error_empty_email), Snackbar.LENGTH_SHORT).show()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Snackbar.make(binding.root, getString(R.string.error_invalid_email), Snackbar.LENGTH_SHORT).show()
            } else {
                sendPasswordResetEmail(email)
            }
            dialog.dismiss()
        }

        builder.setNegativeButton(getString(R.string.btn_cancel)) { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                val message = if (task.isSuccessful) {
                    getString(R.string.msg_reset_email_sent)
                } else {
                    task.exception?.localizedMessage ?: getString(R.string.msg_reset_email_error)
                }
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
    }

    override fun onResume() {
        super.onResume()
        if (_FLAG_IS_REGISTRO) {
            _FLAG_IS_REGISTRO = false
            Snackbar.make(binding.root, getString(R.string.thank_you_for_registering), Snackbar.LENGTH_LONG).show()
        }
    }
}
