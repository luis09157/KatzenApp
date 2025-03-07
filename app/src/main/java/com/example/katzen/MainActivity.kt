package com.example.katzen

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.katzen.Fragment.Cliente.ClienteFragment
import com.example.katzen.Fragment.Paciente.PacienteFragment
import com.example.katzen.Fragment.Viajes.ViajesFragment
import com.example.katzen.Helper.UtilFragment
import com.ninodev.katzen.databinding.ActivityMainBinding
import com.example.katzen.Fragment.Card.PaymetCardFragment
import com.example.katzen.Fragment.Gasolina.FuellFragment
import com.example.katzen.Model.User
import com.example.katzen.PDF.ConvertPDF
import com.google.android.material.navigation.NavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import com.ninodev.katzen.R

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var auth: FirebaseAuth

    private val codeNotification = 101

    companion object {
        const val TAG = "MainActivity"
        var _INFO_USER: User = User()
        const val PICK_IMAGE_REQUEST = 1 // Este es el código de solicitud para abrir la galería
        const val CAMERA_REQUEST_CODE = 2 // Este es el código de solicitud para tomar una foto con la cámara
        const val CAMERA_PERMISSION_CODE = 100 // Este es el código de solicitud para el permiso de cámara
        const val GALLERY_PERMISSION_CODE = 101 // Aquí agregamos el código de solicitud para el permiso de galería
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        firebaseAnalytics = Firebase.analytics
        Firebase.messaging.isAutoInitEnabled = true
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_fuel, R.id.nav_payment_card, R.id.nav_slideshow, R.id.nav_viajes
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        drawerToggle = ActionBarDrawerToggle(
            this, drawerLayout, binding.appBarMain.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            navigateTo(menuItem.itemId)
            drawerLayout.closeDrawers()
            true
        }

        checkPermission(android.Manifest.permission.POST_NOTIFICATIONS, "Notification", codeNotification)
        logRegToken()
        subscribeToTopic("all_users")
        UtilFragment.changeFragment(this, MenuFragment(),TAG)

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    private fun navigateTo(itemId: Int) {
        if (itemId == R.id.nav_cerrar_sesion) {
            logout()
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return  // Salimos de la función para evitar ejecutar el cambio de fragmento
        }

        val fragment = when (itemId) {
            R.id.nav_home -> MenuFragment()
            R.id.nav_fuel -> FuellFragment()
            R.id.nav_payment_card -> PaymetCardFragment()
            R.id.nav_viajes -> ViajesFragment()
            R.id.nav_cliente -> ClienteFragment()
            R.id.nav_paciente -> PacienteFragment()
            else -> return
        }

        binding.appBarMain.toolbar.title = menuItemTitle(itemId)
        UtilFragment.changeFragment(this, fragment, TAG)
    }

    private fun menuItemTitle(itemId: Int) = when (itemId) {
        R.id.nav_home -> getString(R.string.menu_home)
        R.id.nav_fuel -> "Calcular gasolina"
        R.id.nav_payment_card -> "Pago con tarjeta"
        R.id.nav_viajes -> "Domicilios"
        R.id.nav_cliente -> getString(R.string.menu_cliente)
        R.id.nav_paciente -> getString(R.string.menu_paciente)
        R.id.nav_cerrar_sesion -> getString(R.string.menu_cerrar_sesion)
        else -> ""
    }

    private fun checkPermission(permission: String, name: String, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(applicationContext, permission) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "$name permission is granted")

                }
                shouldShowRequestPermissionRationale(permission) -> showDialog(permission, name, requestCode)
                else -> ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            }
        }
    }

    private fun showDialog(permission: String, name: String, requestCode: Int) {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage("Permission to access your $name is required to use this app")
            setTitle("Permission Required")
            setPositiveButton("OK") { dialog, which ->
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun logRegToken() {
        Firebase.messaging.getToken().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d(TAG, "FCM Registration token: $token")
            } else {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
            }
        }
    }

    private fun logout() {
        auth.signOut() // Cierra sesión en Firebase
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            codeNotification -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission Granted")
                } else {
                    Log.d(TAG, "Permission Denied")
                }
            }
            // Manejo de otros permisos...
        }
    }

    private fun subscribeToTopic(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                val msg = if (task.isSuccessful) {
                    "Suscrito al tópico: $topic"
                } else {
                    "Suscripción fallida"
                }
                Log.d("MainActivity", msg)
            }
    }

}
