package com.example.katzen

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import com.example.katzen.DataBaseFirebase.FirebaseAuthPerfilUtil
import com.example.katzen.Fragment.Campaña.YearListFragment
import com.example.katzen.Fragment.Cliente.ClienteFragment
import com.example.katzen.Fragment.Inventario.InventarioFragment
import com.example.katzen.Fragment.Paciente.PacienteFragment
import com.example.katzen.Fragment.Producto.MenuProductosFragment
import com.example.katzen.Fragment.Staff.RegistrosInactivosFragment
import com.example.katzen.Fragment.Staff.StaffPerfilFragment
import com.example.katzen.Helper.StaffEditSessionBridge
import com.example.katzen.Helper.StaffRoleHelper
import com.example.katzen.Helper.staffEditSession
import com.example.katzen.Helper.staffSession
import com.example.katzen.Fragment.Venta.VentasFragment
import com.example.katzen.Fragment.Viajes.YearViajeListFragment
import com.example.katzen.Helper.AuthSessionHelper
import com.example.katzen.Helper.AuthRoleHelper
import com.example.katzen.Helper.FirebaseMonitoringHelper
import com.example.katzen.Helper.HelperUser
import com.example.katzen.Helper.ImageLoaderHelper
import com.example.katzen.Helper.UtilFragment
import com.ninodev.katzen.databinding.ActivityMainBinding
import com.example.katzen.Fragment.Card.PaymetCardFragment
import com.example.katzen.Fragment.Gasolina.FuellFragment
import com.example.katzen.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.ktx.Firebase
import com.ninodev.katzen.R

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var auth: FirebaseAuth

    private val codeNotification = 101

    companion object {
        const val TAG = "MainActivity"
        const val EXTRA_FORCE_STAFF = "force_staff_mode"
        var _INFO_USER: User = User()
        const val PICK_IMAGE_REQUEST = 1
        const val CAMERA_REQUEST_CODE = 2
        const val CAMERA_PERMISSION_CODE = 100
        const val GALLERY_PERMISSION_CODE = 101
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
                val forceStaff = intent.getBooleanExtra(EXTRA_FORCE_STAFF, false)
                when {
                    !forceStaff && session.isClient() -> {
                        startActivity(Intent(this, PortalMainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            putExtra(PortalMainActivity.EXTRA_FORCE_PORTAL, true)
                        })
                        finish()
                    }
                    session.isStaff() || (forceStaff && session.staffAccess) -> {
                        initStaffUi(savedInstanceState, session.staffRole)
                        AuthSessionHelper.refreshClaimsInBackground()
                    }
                    session.needsRolePicker() -> goToLogin()
                    else -> {
                        auth.signOut()
                        goToLogin()
                    }
                }
            },
            onUnauthenticated = { goToLogin() },
            onInvalidSession = {
                auth.signOut()
                goToLogin()
            }
        )
    }

    private fun initStaffUi(savedInstanceState: Bundle?, staffRole: String = "") {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        StaffEditSessionBridge.bind(staffEditSession())
        staffSession().staffRole = staffRole
        setSupportActionBar(binding.appBarMain.toolbar)

        Firebase.messaging.isAutoInitEnabled = true
        HelperUser.getUserId()?.let { userId ->
            if (userId.isNotEmpty()) {
                HelperUser._ID_USER = userId
                FirebaseMonitoringHelper.setUserId(userId)
            }
        }
        HelperUser.getDataUserRefresh(this) { updateNavHeader() }

        val versionTextView = findViewById<TextView>(R.id.version_text)
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        versionTextView.text = getString(R.string.app_version_label, versionName)

        drawerToggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.appBarMain.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            if (!staffSession().canAccessNavItem(menuItem.itemId)) {
                Toast.makeText(this, R.string.staff_permission_denied, Toast.LENGTH_SHORT).show()
                binding.drawerLayout.closeDrawers()
                return@setNavigationItemSelectedListener false
            }
            navigateTo(menuItem.itemId)
            binding.drawerLayout.closeDrawers()
            true
        }

        applyNavPermissions()

        binding.navView.getHeaderView(0)?.setOnClickListener {
            navigateTo(R.id.nav_perfil)
            binding.drawerLayout.closeDrawers()
        }

        checkPermission(android.Manifest.permission.POST_NOTIFICATIONS, codeNotification)
        logRegToken()
        subscribeToTopic("all_users")

        if (savedInstanceState == null) {
            binding.appBarMain.toolbar.title = getString(R.string.nav_home)
            FirebaseMonitoringHelper.logScreen("MenuFragment")
            UtilFragment.changeFragment(this, MenuFragment(), TAG, clearBackStack = true)
        }
    }

    fun updateNavHeader() {
        if (!::binding.isInitialized) return

        val header = binding.navView.getHeaderView(0) ?: return
        val user = _INFO_USER
        val authUser = auth.currentUser

        val displayName = user.nombreUsuario.ifBlank {
            authUser?.displayName.orEmpty().ifBlank {
                authUser?.email?.substringBefore("@").orEmpty()
            }
        }
        header.findViewById<TextView>(R.id.nav_header_name)?.text =
            displayName.ifBlank { getString(R.string.perfil_staff_default) }
        header.findViewById<TextView>(R.id.nav_header_email)?.text =
            user.correo.ifBlank { authUser?.email.orEmpty() }

        ImageLoaderHelper.load(
            imageView = header.findViewById<ImageView>(R.id.nav_header_avatar) ?: return,
            imageUrl = user.imagenPerfil,
            placeholderRes = R.drawable.logo,
            errorRes = R.drawable.logo
        )

        val uid = HelperUser.getUserId().orEmpty()
        FirebaseAuthPerfilUtil.obtenerPerfilAsync(
            uid,
            onSuccess = { perfil ->
                perfil?.staffRole?.takeIf { it.isNotBlank() }?.let { staffSession().staffRole = it }
                header.findViewById<TextView>(R.id.nav_header_role)?.text =
                    getString(StaffRoleHelper.roleLabelRes(perfil?.staffRole ?: staffSession().staffRole))
                applyNavPermissions()
            },
            onFailure = {
                header.findViewById<TextView>(R.id.nav_header_role)?.text =
                    getString(StaffRoleHelper.roleLabelRes(staffSession().staffRole))
            }
        )
    }

    private fun applyNavPermissions() {
        if (!::binding.isInitialized) return
        val role = staffSession().staffRole
        val menu = binding.navView.menu
        listOf(
            R.id.nav_cliente,
            R.id.nav_paciente,
            R.id.nav_productos,
            R.id.nav_venta,
            R.id.nav_inventario,
            R.id.nav_inactivos,
            R.id.nav_fuel,
            R.id.nav_payment_card,
            R.id.nav_viajes,
            R.id.nav_campania
        ).forEach { itemId ->
            menu.findItem(itemId)?.isVisible = StaffRoleHelper.canAccessNavItem(role, itemId)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (::drawerToggle.isInitialized) {
            drawerToggle.syncState()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (::drawerToggle.isInitialized) {
            drawerToggle.onConfigurationChanged(newConfig)
        }
    }

    private fun navigateTo(itemId: Int) {
        if (itemId == R.id.nav_cerrar_sesion) {
            logout()
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            goToLogin()
            return
        }

        val fragment = when (itemId) {
            R.id.nav_home -> MenuFragment()
            R.id.nav_fuel -> FuellFragment()
            R.id.nav_payment_card -> PaymetCardFragment()
            R.id.nav_viajes -> YearViajeListFragment()
            R.id.nav_cliente -> ClienteFragment()
            R.id.nav_paciente -> PacienteFragment()
            R.id.nav_productos -> MenuProductosFragment()
            R.id.nav_venta -> VentasFragment()
            R.id.nav_inventario -> InventarioFragment()
            R.id.nav_inactivos -> RegistrosInactivosFragment()
            R.id.nav_campania -> YearListFragment()
            R.id.nav_perfil -> StaffPerfilFragment()
            else -> return
        }

        binding.appBarMain.toolbar.title = menuItemTitle(itemId)
        FirebaseMonitoringHelper.logScreen(fragment.javaClass.simpleName)
        UtilFragment.changeFragment(this, fragment, TAG)
    }

    private fun menuItemTitle(itemId: Int) = when (itemId) {
        R.id.nav_home -> getString(R.string.nav_home)
        R.id.nav_fuel -> getString(R.string.menu_gasolina)
        R.id.nav_payment_card -> getString(R.string.menu_pago_tarjeta)
        R.id.nav_viajes -> getString(R.string.menu_viajes)
        R.id.nav_cliente -> getString(R.string.menu_cliente)
        R.id.nav_paciente -> getString(R.string.menu_paciente)
        R.id.nav_productos -> getString(R.string.menu_productos)
        R.id.nav_venta -> getString(R.string.menu_venta)
        R.id.nav_inventario -> getString(R.string.menu_inventario)
        R.id.nav_inactivos -> getString(R.string.menu_registros_inactivos)
        R.id.nav_campania -> getString(R.string.menu_campania)
        R.id.nav_perfil -> getString(R.string.perfil_mi_cuenta)
        else -> ""
    }

    override fun onDestroy() {
        StaffEditSessionBridge.unbind()
        super.onDestroy()
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun checkPermission(permission: String, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        }
    }

    private fun logRegToken() {
        Firebase.messaging.token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "FCM Registration token: ${task.result}")
            } else {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
            }
        }
    }

    private fun logout() {
        FirebaseMonitoringHelper.logLogout()
        auth.signOut()
        HelperUser._ID_USER = ""
        _INFO_USER = User()
    }

    private fun subscribeToTopic(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                Log.d(TAG, if (task.isSuccessful) "Suscrito al tópico: $topic" else "Suscripción fallida")
            }
    }
}
