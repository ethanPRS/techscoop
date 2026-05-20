package com.estudiante.techscoop.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import com.estudiante.techscoop.data.PreferencesManager
import com.estudiante.techscoop.data.SessionManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.estudiante.techscoop.OfflineState
import com.estudiante.techscoop.R
import com.estudiante.techscoop.databinding.ActivityMainBinding
import com.estudiante.techscoop.notifications.NotificationScheduler

// Pantalla principal: navegación inferior, bloqueo offline y permiso/programación de notificaciones.
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    // Escucha cambios de red para mostrar u ocultar la pantalla offline automáticamente.
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var wasOffline = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        SessionManager.init(applicationContext)
        PreferencesManager.init(applicationContext)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Botón de la pantalla offline: vuelve a comprobar conectividad.
        binding.offlineGate.btnRetryConnection.setOnClickListener {
            applyOfflineUi(userTriggeredRetry = true)
        }

        setupBottomNavigation()

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val gate = binding.offlineGate.root
                    if (gate.visibility == View.VISIBLE) {
                        moveTaskToBack(true)
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )

        // Al abrir la app: comprueba red, pide permiso de notificaciones (API 33+) y programa WorkManager.
        applyOfflineUi()
        requestNotificationPermissionIfNeeded()
        NotificationScheduler.schedule(this)
    }

    // Registra NetworkCallback para reaccionar cuando vuelve Wi‑Fi o datos.
    override fun onStart() {
        super.onStart()
        val cm = getSystemService(ConnectivityManager::class.java) ?: return
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                runOnUiThread { applyOfflineUi() }
            }

            override fun onLost(network: Network) {
                runOnUiThread { applyOfflineUi() }
            }
        }
        networkCallback = callback
        cm.registerDefaultNetworkCallback(callback)
    }

    override fun onStop() {
        networkCallback?.let {
            getSystemService(ConnectivityManager::class.java)?.unregisterNetworkCallback(it)
        }
        networkCallback = null
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        applyOfflineUi()
    }

    // Android 13+: permiso POST_NOTIFICATIONS obligatorio para mostrar avisos.
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            // Sin Internet no se cambia de pestaña (evita fragments que llaman API).
            if (OfflineState.isActive(this)) return@setOnItemSelectedListener false
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_search -> SearchFragment()
                R.id.nav_favorites -> FavoritesFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> HomeFragment()
            }
            loadFragment(fragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_fragment_container, fragment)
            .commit()
    }

    // Muestra u oculta el include offlineGate y el contenido principal de la app.
    private fun applyOfflineUi(userTriggeredRetry: Boolean = false) {
        val gate = binding.offlineGate.root
        if (OfflineState.isActive(this)) {
            gate.visibility = View.VISIBLE
            binding.main.visibility = View.GONE
            wasOffline = true
            if (userTriggeredRetry) {
                Toast.makeText(this, R.string.offline_gate_still_offline, Toast.LENGTH_SHORT).show()
            }
            return
        }
        gate.visibility = View.GONE
        binding.main.visibility = View.VISIBLE
        if (wasOffline) {
            Toast.makeText(this, R.string.offline_gate_back_online, Toast.LENGTH_SHORT).show()
            wasOffline = false
        }
    }
}