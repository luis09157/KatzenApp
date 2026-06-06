package com.example.katzen

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.katzen.DataBaseFirebase.FirebaseClienteUtil
import com.example.katzen.Fragment.Portal.PortalMascotasFragment
import com.example.katzen.Fragment.Portal.PortalNotificacionesFragment
import com.example.katzen.Fragment.Portal.PortalPerfilFragment
import com.example.katzen.Helper.AuthSessionHelper
import com.example.katzen.Helper.AuthRoleHelper
import com.example.katzen.Helper.ImageLoaderHelper
import com.example.katzen.Helper.PortalDeepLinkHelper
import com.example.katzen.Helper.PortalFcmHelper
import com.example.katzen.Helper.PortalSessionBridge
import com.example.katzen.Helper.portalSession
import com.example.katzen.Helper.HelperUser
import com.google.firebase.auth.FirebaseAuth
import com.ninodev.katzen.R
import com.ninodev.katzen.databinding.ActivityPortalMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PortalMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPortalMainBinding
    private lateinit var auth: FirebaseAuth
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        const val TAG = "PortalMainActivity"
        const val EXTRA_FORCE_PORTAL = "force_portal_mode"
        var CLIENTE_ID: String
            get() = PortalSessionBridge.clienteId
            set(value) {
                PortalSessionBridge.clienteId = value
            }
        var CLIENTE_NOMBRE: String
            get() = PortalSessionBridge.clienteNombre
            set(value) {
                PortalSessionBridge.clienteNombre = value
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            goToLogin()
            return
        }

        AuthSessionHelper.verifySessionQuick(
            onAuthenticated = { session ->
                val forcePortal = intent.getBooleanExtra(EXTRA_FORCE_PORTAL, false)
                when {
                    session.isClient() || (session.isDual() && forcePortal) -> {
                        PortalSessionBridge.bind(portalSession())
                        val clienteId = session.clienteId.takeIf { it.isNotBlank() }
                            ?: PortalSessionBridge.clienteId
                        if (clienteId.isBlank()) {
                            goToLogin()
                            return@verifySessionQuick
                        }
                        CLIENTE_ID = clienteId
                        HelperUser._ID_USER = auth.currentUser?.uid.orEmpty()
                        setupUi()
                        loadClienteHeader()
                        PortalFcmHelper.initPortalMessaging(this, CLIENTE_ID)
                        consumeDeepLink(intent)
                        AuthSessionHelper.refreshClaimsInBackground()
                    }
                    session.isStaff() && !forcePortal -> {
                        startActivity(Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            putExtra(MainActivity.EXTRA_FORCE_STAFF, true)
                        })
                        finish()
                    }
                    session.needsRolePicker() -> goToLogin()
                    else -> goToLogin()
                }
            },
            onUnauthenticated = { goToLogin() },
            onInvalidSession = { goToLogin() }
        )
    }

    private fun setupUi() {
        binding = ActivityPortalMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupSystemBars()

        binding.btnHeaderBack.setOnClickListener {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
                updateHeaderForNavigation()
            }
        }

        if (supportFragmentManager.findFragmentById(R.id.portal_container) == null) {
            openFragment(PortalMascotasFragment(), false)
            binding.bottomNav.selectedItemId = R.id.portal_nav_mascotas
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
            updateHeaderForNavigation()
            val fragment: Fragment = when (item.itemId) {
                R.id.portal_nav_mascotas -> PortalMascotasFragment()
                R.id.portal_nav_notificaciones -> PortalNotificacionesFragment()
                R.id.portal_nav_perfil -> PortalPerfilFragment()
                else -> return@setOnItemSelectedListener false
            }
            openFragment(fragment, false)
            true
        }

        supportFragmentManager.addOnBackStackChangedListener {
            updateHeaderForNavigation()
        }
    }

    private fun setupSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        ViewCompat.setOnApplyWindowInsetsListener(binding.portalHeader) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNav) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, systemBars.bottom)
            insets
        }
    }

    private fun updateHeaderForNavigation() {
        binding.btnHeaderBack.visibility =
            if (supportFragmentManager.backStackEntryCount > 0) View.VISIBLE else View.GONE
    }

    fun updateToolbarTitle(nombre: String) {
        updateClienteHeader(nombre, "", "")
    }

    fun updateClienteHeader(nombreCompleto: String, imageUrl: String, imageFileName: String) {
        CLIENTE_NOMBRE = nombreCompleto
        if (!::binding.isInitialized) return

        binding.tvToolbarBadge.text = formatBadgeName(nombreCompleto)

        val resolved = ImageLoaderHelper.resolveProfileImage(imageUrl, imageFileName)
        ImageLoaderHelper.load(
            imageView = binding.imgHeaderAvatar,
            imageUrl = resolved.imageUrl,
            placeholderRes = R.drawable.avatar_sin_imagen,
            errorRes = R.drawable.avatar_sin_imagen,
            storageFolder = "Clientes",
            imageFileName = resolved.imageFileName,
            forList = true
        )
    }

    private fun formatBadgeName(nombreCompleto: String): String {
        val trimmed = nombreCompleto.trim()
        if (trimmed.isBlank()) return getString(R.string.portal_badge_cliente)
        return trimmed.substringBefore(" ")
    }

    private fun loadClienteHeader() {
        if (CLIENTE_ID.isBlank()) return
        scope.launch {
            val cliente = withContext(Dispatchers.IO) {
                runCatching { FirebaseClienteUtil.obtenerClientePorId(CLIENTE_ID) }.getOrNull()
            }
            cliente?.let {
                val nombre = listOf(it.nombre, it.apellidoPaterno)
                    .filter { part -> part.isNotBlank() }
                    .joinToString(" ")
                updateClienteHeader(nombre, it.imageUrl, it.imageFileName)
            }
        }
    }

    private fun openFragment(fragment: Fragment, addToBackStack: Boolean) {
        val tx = supportFragmentManager.beginTransaction()
            .replace(R.id.portal_container, fragment)
        if (addToBackStack) tx.addToBackStack(null)
        tx.commit()
    }

    fun openMascotaDetalle(fragment: Fragment) {
        openFragment(fragment, true)
        updateHeaderForNavigation()
    }

    fun openExpedienteSection(fragment: Fragment) {
        openFragment(fragment, true)
        updateHeaderForNavigation()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (::binding.isInitialized) {
            consumeDeepLink(intent)
        } else {
            portalSession().pendingDeepLink = PortalDeepLinkHelper.parseIntent(intent)
        }
    }

    private fun consumeDeepLink(intent: Intent?) {
        val target = PortalDeepLinkHelper.parseIntent(intent)
            ?: portalSession().pendingDeepLink
            ?: return
        portalSession().pendingDeepLink = null
        PortalDeepLinkHelper.clearIntentExtras(intent)
        PortalDeepLinkHelper.navigate(this, target)
    }

    fun selectBottomNav(itemId: Int) {
        if (!::binding.isInitialized) return
        binding.bottomNav.selectedItemId = itemId
    }

    fun replaceRootFragment(fragment: Fragment) {
        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        openFragment(fragment, false)
        updateHeaderForNavigation()
    }

    override fun onDestroy() {
        PortalFcmHelper.clearPortalSubscription(CLIENTE_ID)
        PortalSessionBridge.unbind()
        super.onDestroy()
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
