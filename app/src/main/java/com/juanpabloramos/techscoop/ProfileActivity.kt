package com.juanpabloramos.techscoop

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import coil.load
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        bindHeader()

        findViewById<TextView>(R.id.tvEditProfileLink).setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnManageInterests).setOnClickListener {
            showInterestsDialog()
        }

        findViewById<MaterialCardView>(R.id.cardPrivacy).setOnClickListener {
            Toast.makeText(this, R.string.settings_privacy_message, Toast.LENGTH_SHORT).show()
        }
        findViewById<MaterialCardView>(R.id.cardCensorship).setOnClickListener {
            Toast.makeText(this, R.string.settings_censorship_message, Toast.LENGTH_SHORT).show()
        }
        findViewById<MaterialCardView>(R.id.cardNotifications).setOnClickListener {
            Toast.makeText(this, R.string.settings_notifications_message, Toast.LENGTH_SHORT).show()
        }
        findViewById<MaterialCardView>(R.id.cardHelp).setOnClickListener {
            Toast.makeText(this, R.string.settings_help_message, Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            signOutAndGoToLogin()
        }
    }

    private fun signOutAndGoToLogin() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        FirebaseAuth.getInstance().signOut()
        GoogleSignIn.getClient(this, gso).signOut().addOnCompleteListener {
            lifecycleScope.launch { UserLocalRepository.clearAll(this@ProfileActivity) }
            UserPreferences.clearSession(this)
            startActivity(
                Intent(this, LoginActivity::class.java).addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                )
            )
            finishAffinity()
        }
    }

    override fun onResume() {
        super.onResume()
        if (OfflineState.isActive(this)) {
            Toast.makeText(this, R.string.offline_gate_subtitle, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        lifecycleScope.launch {
            UserLocalRepository.loadIntoPreferences(this@ProfileActivity)
            bindHeader()
        }
    }

    private fun bindHeader() {
        val name = UserPreferences.getDisplayName(this).ifBlank { getString(R.string.demo_user_name) }
        val email = UserPreferences.getEmail(this).ifBlank { getString(R.string.demo_user_email) }
        findViewById<TextView>(R.id.tvUserName).text = name
        findViewById<TextView>(R.id.tvUserEmail).text = email

        val avatar = findViewById<ShapeableImageView>(R.id.ivProfileAvatar)
        val uriString = UserPreferences.getAvatarUriString(this)
        if (!uriString.isNullOrBlank()) {
            runCatching { Uri.parse(uriString) }.getOrNull()?.let { uri ->
                avatar.load(uri) {
                    crossfade(true)
                    placeholder(R.drawable.techscoop_logo)
                }
                return
            }
        }
        avatar.load(R.drawable.techscoop_logo)
    }

    private fun showInterestsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.fragment_registro_intereses, null)
        val chipGroup = dialogView.findViewById<ChipGroup>(R.id.cgInterestCategories)
        dialogView.findViewById<View>(R.id.btnSaveInterests).isVisible = false

        val savedKeys = UserPreferences.getInterestKeys(this)
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as? Chip ?: continue
            val tag = chip.tag?.toString() ?: continue
            chip.isChecked = tag in savedKeys
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton(R.string.interests_save) { _, _ ->
                val keys = mutableSetOf<String>()
                for (i in 0 until chipGroup.childCount) {
                    val chip = chipGroup.getChildAt(i) as? Chip ?: continue
                    if (chip.isChecked) chip.tag?.toString()?.let { keys.add(it) }
                }
                UserPreferences.setInterestKeys(this, keys)
                Toast.makeText(
                    this,
                    "${getString(R.string.interests_saved)} (${keys.size})",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
