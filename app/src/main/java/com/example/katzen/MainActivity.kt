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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
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
import com.example.katzen.ui.medical.MedicalFragment
import com.example.katzen.ui.viajes.ViajesFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.messaging.ktx.remoteMessage
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications
            Toast.makeText(this,"tenemos permiso",Toast.LENGTH_SHORT).show();
        } else {
            // TODO: Inform user that that your app will not show notifications.
            Toast.makeText(this,"no tenemos permiso",Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
                else -> false
            }
        }


        askNotificationPermission()

        logRegToken()
        sendUpstream()

        val serviceIntent = Intent(this, DomiciliosPendientesService::class.java)
        startForegroundService(serviceIntent)
    }

    private suspend fun getAndStoreRegToken(): String {
        val token = Firebase.messaging.token.await()
        // Add token and timestamp to Firestore for this user
        val deviceToken = hashMapOf(
            "token" to token,
            "timestamp" to FieldValue.serverTimestamp(),
        )

        // Get user ID from Firebase Auth or your own server
        Firebase.firestore.collection("fcmTokens").document("myuserid")
            .set(deviceToken).await()
        return token
    }
    fun sendUpstream() {
        val SENDER_ID = "katzen-a0e3e"
        val messageId = 10 // Increment for each
        // [START fcm_send_upstream]
        val fm = Firebase.messaging
        fm.send(
            remoteMessage("$SENDER_ID@fcm.googleapis.com") {
                setMessageId(messageId.toString())
                addData("my_message", "Hello World")
                addData("my_action", "SAY_HELLO")
            },
        )
        // [END fcm_send_upstream]
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
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
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

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
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
        }
    }
}