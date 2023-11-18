package com.example.katzen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.katzen.Helper.UtilFragment
import com.example.katzen.PDF.ConvertPDF
import com.example.katzen.Service.DomiciliosPendientesService
import com.example.katzen.databinding.ActivityMainBinding
import com.example.katzen.ui.card.PaymetCardFragment
import com.example.katzen.ui.example.ExampleFragment
import com.example.katzen.ui.gasolina.FuellFragment
import com.example.katzen.ui.mascota.MascotaFragment
import com.example.katzen.ui.medical.MedicalFragment
import com.example.katzen.ui.viajes.ViajesFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.messaging.ktx.remoteMessage
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"
    val code_notification =101
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private fun CheckForPermissions(Permission:String,Name:String,RequestCode:Int)
    {
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M)
        {
            when{
                ContextCompat.checkSelfPermission(applicationContext,Permission)== PackageManager.PERMISSION_GRANTED->{
                    Toast.makeText(applicationContext,"$Name permission is granted",Toast.LENGTH_SHORT).show()
                }
                shouldShowRequestPermissionRationale(Permission) -> showDialog(Permission,Name,RequestCode)
                else-> ActivityCompat.requestPermissions(this, arrayOf(Permission),RequestCode)
            }
        }
    }
    private fun showDialog(permission: String, name: String, requestCode: Int) {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage("Permission to access your $name is required to use this app")
            setTitle("Permission Required")
            setPositiveButton("ok"){dialog,which ->
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission),requestCode)
            }
        }
        val dialog =builder.create()
        dialog.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        firebaseAnalytics = Firebase.analytics
        Firebase.messaging.isAutoInitEnabled = true
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_fuel, R.id.nav_payment_card, R.id.nav_slideshow,R.id.nav_viajes
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_fuel -> {
                    binding.appBarMain.toolbar.title = "Calcular gasolina"
                    UtilFragment.changeFragment(this, FuellFragment(),TAG)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_payment_card -> {
                    binding.appBarMain.toolbar.title = "Pago con tarjeta"
                    UtilFragment.changeFragment(this, PaymetCardFragment(),TAG)
                    drawerLayout.closeDrawer(GravityCompat.START)

                    true
                }
                R.id.nav_slideshow -> {
                    binding.appBarMain.toolbar.title = "Dosis"
                    UtilFragment.changeFragment(this, MedicalFragment(),TAG)
                    drawerLayout.closeDrawer(GravityCompat.START)

                    true
                }
                R.id.nav_viajes -> {
                    binding.appBarMain.toolbar.title = "Domicilios"
                    UtilFragment.changeFragment(this, ViajesFragment(),TAG)
                    drawerLayout.closeDrawer(GravityCompat.START)

                    true
                }
                R.id.nav_example -> {
                    binding.appBarMain.toolbar.title = "Example"
                    UtilFragment.changeFragment(this, ExampleFragment(),TAG)
                    drawerLayout.closeDrawer(GravityCompat.START)

                    true
                }
                R.id.nav_mascota -> {
                    binding.appBarMain.toolbar.title = "Agregar Mascota"
                    UtilFragment.changeFragment(this, MascotaFragment(),TAG)
                    drawerLayout.closeDrawer(GravityCompat.START)

                    true
                }
                else -> false
            }
        }


        CheckForPermissions(android.Manifest.permission.POST_NOTIFICATIONS,"Notification",code_notification)

        logRegToken()

       /* val serviceIntent = Intent(this, DomiciliosPendientesService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startService(serviceIntent)
        }*/
    }
    fun logRegToken() {
        // [START log_reg_token]
        Firebase.messaging.getToken().addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = "FCM Registration token: $token"
            Log.d(TAG, msg)
            //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        }
        // [END log_reg_token]
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                Toast.makeText(applicationContext, "click on setting", Toast.LENGTH_LONG).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            ConvertPDF.REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) ===
                                PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
            code_notification -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(this,
                            Manifest.permission.POST_NOTIFICATIONS) ===
                                PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                    }
                } else {
                   // ActivityCompat.requestPermissions(this, arrayOf("android.Manifest.permission.POST_NOTIFICATIONS"),code_notification)
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }
}