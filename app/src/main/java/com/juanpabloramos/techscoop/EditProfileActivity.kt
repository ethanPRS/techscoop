package com.juanpabloramos.techscoop

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import coil.load
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText

class EditProfileActivity : AppCompatActivity() {

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                UserPreferences.setAvatarUriString(this, uri.toString())
                loadAvatarPreview(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        findViewById<MaterialToolbar>(R.id.toolbarEdit).setNavigationOnClickListener { finish() }

        val etName = findViewById<TextInputEditText>(R.id.etEditName)
        val etEmail = findViewById<TextInputEditText>(R.id.etEditEmail)
        etName.setText(UserPreferences.getDisplayName(this))
        etEmail.setText(UserPreferences.getEmail(this))

        UserPreferences.getAvatarUriString(this)?.let { s ->
            runCatching { Uri.parse(s) }.getOrNull()?.let { loadAvatarPreview(it) }
        }

        findViewById<MaterialButton>(R.id.btnPickPhoto).setOnClickListener {
            pickImage.launch("image/*")
        }

        findViewById<MaterialButton>(R.id.btnSaveProfile).setOnClickListener {
            val name = etName.text?.toString().orEmpty().trim()
            val email = etEmail.text?.toString().orEmpty().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, R.string.login_required_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val display = name.ifBlank { email.substringBefore("@") }
            UserPreferences.setDisplayName(this, display)
            UserPreferences.setEmail(this, email)
            val avatar = UserPreferences.getAvatarUriString(this)
            lifecycleScope.launch {
                UserLocalRepository.updateProfile(this@EditProfileActivity, display, email, avatar)
            }
            Toast.makeText(this, R.string.profile_saved, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadAvatarPreview(uri: Uri) {
        findViewById<ShapeableImageView>(R.id.ivEditAvatar).load(uri) {
            crossfade(true)
            placeholder(R.drawable.techscoop_logo)
        }
    }
}
