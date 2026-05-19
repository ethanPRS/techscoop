package com.estudiante.techscoop.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.estudiante.techscoop.R
import com.estudiante.techscoop.SessionManager
import com.estudiante.techscoop.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginActivity : AppCompatActivity() {

    private val googleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data ?: run {
            if (result.resultCode == RESULT_CANCELED) {
                Toast.makeText(this, R.string.google_sign_in_cancelled, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, R.string.google_sign_in_failed, Toast.LENGTH_LONG).show()
            }
            return@registerForActivityResult
        }

        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                .getResult(ApiException::class.java)
            val token = account.idToken
            if (token != null) {
                signInWithGoogle(token, account.email, account.displayName)
            } else {
                Toast.makeText(this, R.string.google_sign_in_failed, Toast.LENGTH_LONG).show()
            }
        } catch (e: ApiException) {
            if (e.statusCode != 12501) {
                Toast.makeText(this, R.string.google_sign_in_failed, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val savedEmail = SessionManager.getLastEmail(this)
        if (savedEmail.isNotBlank()) {
            etEmail.setText(savedEmail)
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text?.toString()?.trim().orEmpty()
            val password = etPassword.text?.toString().orEmpty()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.login_required_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                AuthRepository.signIn(email, password).fold(
                    onSuccess = { user ->
                        val name = user.displayName ?: email.substringBefore("@")
                        AuthRepository.syncUserToRoom(this@LoginActivity, email, name, password)
                        SessionManager.saveLastEmail(this@LoginActivity, email)
                        goToMain()
                    },
                    onFailure = { e ->
                        Toast.makeText(
                            this@LoginActivity,
                            AuthRepository.userMessage(this@LoginActivity, e),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        }

        findViewById<Button>(R.id.btnGoogleSignIn).setOnClickListener {
            startGoogleSignIn()
        }

        findViewById<TextView>(R.id.tvRegisterLink).setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        findViewById<TextView>(R.id.tvForgotLink).setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun startGoogleSignIn() {
        val playServices = GoogleApiAvailability.getInstance()
        val status = playServices.isGooglePlayServicesAvailable(this)
        if (status != ConnectionResult.SUCCESS) {
            if (playServices.isUserResolvableError(status)) {
                playServices.getErrorDialog(this, status, 9000)?.show()
            } else {
                Toast.makeText(this, R.string.google_play_services_unavailable, Toast.LENGTH_LONG).show()
            }
            return
        }
        val webClientId = getString(R.string.default_web_client_id)
        if (webClientId.isBlank()) {
            Toast.makeText(this, R.string.auth_error_generic, Toast.LENGTH_LONG).show()
            return
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        googleLauncher.launch(GoogleSignIn.getClient(this, gso).signInIntent)
    }

    private fun signInWithGoogle(idToken: String, email: String?, displayName: String?) {
        lifecycleScope.launch {
            try {
                val result = com.google.firebase.auth.FirebaseAuth.getInstance()
                    .signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                    .await()
                val user = result.user ?: return@launch
                val mail = user.email ?: email.orEmpty()
                val name = user.displayName ?: displayName.orEmpty()
                AuthRepository.syncUserToRoom(this@LoginActivity, mail, name)
                SessionManager.saveLastEmail(this@LoginActivity, mail)
                goToMain()
            } catch (e: Exception) {
                Toast.makeText(
                    this@LoginActivity,
                    AuthRepository.userMessage(this@LoginActivity, e),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}


