package com.juanpabloramos.techscoop

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()

    private val googleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken ?: run {
                Toast.makeText(this, "No se obtuvo token de Google", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            firebaseAuthWithGoogle(idToken, account.email, account.displayName)
        } catch (e: ApiException) {
            Toast.makeText(this, "Error Google: ${e.statusCode}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth.currentUser?.let { user ->
            syncUserToPrefs(user.email.orEmpty(), user.displayName, user.uid)
            goToMain()
            finish()
            return
        }

        val emailInput = findViewById<TextInputEditText>(R.id.etEmail)
        val passwordInput = findViewById<TextInputEditText>(R.id.etPassword)

        val savedEmail = UserPreferences.getEmail(this).trim()
        if (savedEmail.isNotBlank()) {
            emailInput.setText(savedEmail)
        }

        findViewById<MaterialButton>(R.id.btnLogin).setOnClickListener {
            val email = emailInput.text?.toString().orEmpty().trim()
            val password = passwordInput.text?.toString().orEmpty().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.login_required_fields), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val display = email.substringBefore("@").ifBlank { "TechScoop" }
            UserPreferences.setLoggedIn(this, true)
            UserPreferences.saveUser(this, email, display)
            lifecycleScope.launch {
                UserLocalRepository.saveSessionUser(this@LoginActivity, email, display)
            }
            goToMain()
            finish()
        }

        findViewById<MaterialButton>(R.id.btnGoogleSignIn).setOnClickListener {
            launchGoogleSignIn()
        }

        findViewById<TextView>(R.id.tvRegisterLink).setOnClickListener {
            Toast.makeText(this, R.string.login_register_soon, Toast.LENGTH_SHORT).show()
        }

        findViewById<TextView>(R.id.tvForgotLink).setOnClickListener {
            Toast.makeText(this, R.string.login_forgot_soon, Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(this, gso)
        googleLauncher.launch(client.signInIntent)
    }

    private fun firebaseAuthWithGoogle(
        idToken: String,
        email: String?,
        displayName: String?
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user ?: return@addOnSuccessListener
                syncUserToPrefs(
                    user.email ?: email.orEmpty(),
                    user.displayName ?: displayName,
                    user.uid
                )
                goToMain()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    e.message ?: "Error al iniciar sesion",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun syncUserToPrefs(email: String, name: String?, uid: String) {
        UserPreferences.setLoggedIn(this, true)
        UserPreferences.saveUser(this, email, name.orEmpty())
        UserPreferences.setFirebaseUid(this, uid)
        lifecycleScope.launch {
            UserLocalRepository.saveSessionUser(
                this@LoginActivity,
                email = email,
                displayName = name.orEmpty()
            )
        }
    }

    private fun goToMain() {
        startActivity(
            Intent(this, MainActivity::class.java).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
        )
    }
}
