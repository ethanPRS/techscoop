package com.juanpabloramos.techscoop

import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var appContent: View
    private lateinit var offlineGate: View
    private lateinit var homeFragment: HomeFragment
    private lateinit var exploreFragment: ExploreFragment
    private lateinit var savedFragment: SavedFragment
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var wasOffline = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (!UserPreferences.isLoggedIn(this) && firebaseUser == null) {
            startActivity(
                Intent(this, LoginActivity::class.java).addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                )
            )
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainRoot)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        appContent = findViewById(R.id.appContent)
        offlineGate = findViewById(R.id.offlineGate)
        toolbar = findViewById(R.id.topToolbar)
        bottomNav = findViewById(R.id.bottomNavigation)
        setSupportActionBar(toolbar)

        findViewById<MaterialButton>(R.id.btnRetryConnection).setOnClickListener {
            applyOfflineUi(userTriggeredRetry = true)
        }

        if (savedInstanceState == null) {
            homeFragment = HomeFragment()
            exploreFragment = ExploreFragment()
            savedFragment = SavedFragment()
            supportFragmentManager.commit {
                add(R.id.mainFragmentContainer, homeFragment, TAG_HOME)
                add(R.id.mainFragmentContainer, exploreFragment, TAG_EXPLORE).hide(exploreFragment)
                add(R.id.mainFragmentContainer, savedFragment, TAG_SAVED).hide(savedFragment)
            }
        } else {
            homeFragment =
                supportFragmentManager.findFragmentByTag(TAG_HOME) as HomeFragment
            exploreFragment =
                supportFragmentManager.findFragmentByTag(TAG_EXPLORE) as ExploreFragment
            savedFragment =
                supportFragmentManager.findFragmentByTag(TAG_SAVED) as SavedFragment
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> showFragment(homeFragment)
                R.id.nav_explore -> showFragment(exploreFragment)
                R.id.nav_saved -> showFragment(savedFragment)
            }
            updateToolbarForTab()
            invalidateOptionsMenu()
            true
        }

        val initialTab = intent.getIntExtra(EXTRA_INITIAL_TAB, TAB_HOME)
        if (savedInstanceState == null) {
            when (initialTab) {
                TAB_EXPLORE -> bottomNav.selectedItemId = R.id.nav_explore
                TAB_SAVED -> bottomNav.selectedItemId = R.id.nav_saved
                else -> bottomNav.selectedItemId = R.id.nav_home
            }
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (offlineGate.visibility == View.VISIBLE) {
                        moveTaskToBack(true)
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )

        applyOfflineUi()
    }

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

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: android.net.NetworkCapabilities
            ) {
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_unified, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (offlineGate.visibility == View.VISIBLE) {
            menu.findItem(R.id.action_refresh_feed)?.isVisible = false
            menu.findItem(R.id.action_profile)?.isVisible = false
            return super.onPrepareOptionsMenu(menu)
        }
        val explore = bottomNav.selectedItemId == R.id.nav_explore
        menu.findItem(R.id.action_refresh_feed).isVisible = explore
        menu.findItem(R.id.action_profile).isVisible = true
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (OfflineState.isActive(this)) return true
        when (item.itemId) {
            R.id.action_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                return true
            }
            R.id.action_refresh_feed -> {
                exploreFragment.refresh()
                Toast.makeText(this, R.string.menu_refresh_done, Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showFragment(target: Fragment) {
        supportFragmentManager.commit {
            hide(homeFragment)
            hide(exploreFragment)
            hide(savedFragment)
            show(target)
        }
        if (target === homeFragment) {
            homeFragment.onScreenVisible()
        } else if (target === savedFragment) {
            savedFragment.refreshList()
        }
    }

    private fun applyOfflineUi(userTriggeredRetry: Boolean = false) {
        val offline = OfflineState.isActive(this)

        if (offline) {
            offlineGate.visibility = View.VISIBLE
            appContent.visibility = View.GONE
            wasOffline = true
            if (userTriggeredRetry) {
                Toast.makeText(this, R.string.offline_gate_still_offline, Toast.LENGTH_SHORT).show()
            }
            return
        }

        offlineGate.visibility = View.GONE
        appContent.visibility = View.VISIBLE

        if (wasOffline) {
            Toast.makeText(this, R.string.offline_gate_back_online, Toast.LENGTH_SHORT).show()
            wasOffline = false
        }

        updateToolbarForTab()
        invalidateOptionsMenu()

        homeFragment.onScreenVisible()
        if (bottomNav.selectedItemId == R.id.nav_saved) {
            savedFragment.refreshList()
        }
    }

    private fun updateToolbarForTab() {
        toolbar.title = when (bottomNav.selectedItemId) {
            R.id.nav_explore -> getString(R.string.toolbar_explore_title)
            R.id.nav_saved -> getString(R.string.toolbar_saved_title)
            else -> getString(R.string.app_name)
        }
        toolbar.subtitle = null
    }

    companion object {
        const val EXTRA_INITIAL_TAB = "extra_initial_tab"
        const val TAB_HOME = 0
        const val TAB_EXPLORE = 1
        const val TAB_SAVED = 2

        private const val TAG_HOME = "home"
        private const val TAG_EXPLORE = "explore"
        private const val TAG_SAVED = "saved"
    }
}
