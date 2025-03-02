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

    companion object {
        var _FLAG_IS_REGISTRO = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance() // Inicializar Firebase Auth
        title = getString(R.string.menu_home)

        init()
        listeners()
    }

    private fun init() {
        try {
            hideLoading()
            if (HelperUser.isUserLoggedIn()) {
                val userId = HelperUser.getUserId()
                if (!userId.isNullOrEmpty()) {
                    HelperUser._ID_USER = userId

                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error: ${e.message}")
        }
    }

    private fun listeners() {
        binding.btnGoogleSignIn.setOnClickListener {

        }
     
        binding.btnLogin.setOnClickListener {
            binding.root.hideKeyboard()
            val email = binding.txtCorreo.editText?.text.toString().trim()
            val password = binding.txtContraseA.editText?.text.toString().trim()

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

            showLoading()
            signIn(email, password)
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.root.hideKeyboard()
                hideLoading()
                if (task.isSuccessful) {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    UtilHelper.showAlert(this, getString(R.string.msg_login_failed))
                }
            }
    }

    private fun showLoading() {
        binding.lottieAnimationView.visibility = View.VISIBLE
        binding.contenedor.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.lottieAnimationView.visibility = View.GONE
        binding.contenedor.visibility = View.VISIBLE
    }

    private fun showPasswordResetDialog() {
        val builder = MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
            .setTitle(getString(R.string.dialog_reset_password_title))
            .setMessage(getString(R.string.dialog_reset_password_message))

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        input.hint = getString(R.string.input_hint_email)
        builder.setView(input)

        builder.setPositiveButton(getString(R.string.btn_send)) { dialog, _ ->
            val email = input.text.toString().trim()
            if (email.isEmpty()) {
                Snackbar.make(binding.root, getString(R.string.error_empty_email), Snackbar.LENGTH_SHORT).show()
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
                if (task.isSuccessful) {
                    Snackbar.make(binding.root, getString(R.string.msg_reset_email_sent), Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(binding.root, getString(R.string.msg_reset_email_error), Snackbar.LENGTH_SHORT).show()
                }
            }
    }

    override fun onResume() {
        super.onResume()

        if (_FLAG_IS_REGISTRO) {
            _FLAG_IS_REGISTRO = false
            Snackbar.make(binding.root, getString(R.string.thank_you_for_registering), Snackbar.LENGTH_LONG).show()
        }

        var doubleBackToExitPressedOnce = false

        this.onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
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
}
