import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class KatzenApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        initializeFirebaseAuth()
    }

    private fun initializeFirebaseAuth() {
        FirebaseAuth.getInstance().apply {
            if (currentUser == null) {
                signInAnonymously()
                    .addOnSuccessListener {
                        Log.d("KatzenApplication", "Autenticación anónima exitosa")
                    }
                    .addOnFailureListener { e ->
                        Log.e("KatzenApplication", "Error en autenticación anónima: ${e.message}")
                    }
            }
        }
    }
} 