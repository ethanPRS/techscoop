package com.estudiante.techscoop.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.estudiante.techscoop.R
import com.estudiante.techscoop.SessionManager
import com.estudiante.techscoop.databinding.LoginActivityBinding
import com.estudiante.techscoop.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.view.inputmethod.EditorInfo

// Pantalla inicial (LAUNCHER): login email, Google Sign-In y enlaces a registro/recuperar.
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginActivityBinding

    // Recibe el resultado del intent de Google y extrae el idToken para Firebase.
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
        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val savedEmail = SessionManager.getLastEmail(this)
        if (savedEmail.isNotBlank()) {
            binding.etEmail.setText(savedEmail)
        }

        // Escuchar el "Enter/Done" del teclado en el campo de contraseña
        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                binding.btnLogin.performClick() // Simula el toque físico en el botón
                true
            } else {
                false
            }
        }

        // Login clásico: valida campos, autentica con Firebase y navega a MainActivity.
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text?.toString()?.trim().orEmpty()
            val password = binding.etPassword.text?.toString().orEmpty()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.login_required_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                AuthRepository.signIn(email, password).fold(
                    onSuccess = { user ->
                        val name = user.displayName ?: email.substringBefore("@")

                        // Sincroniza el perfil local
                        AuthRepository.syncUserToRoom(this@LoginActivity, email, name, password)
                        com.estudiante.techscoop.data.SessionManager.init(applicationContext)
                        com.estudiante.techscoop.data.SessionManager.loginUser(email)
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

        binding.btnGoogleSignIn.setOnClickListener {
            startGoogleSignIn()
        }

        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.tvForgotLink.setOnClickListener {
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

    // Intercambia el idToken de Google por una sesión de Firebase Auth.
    private fun signInWithGoogle(idToken: String, email: String?, displayName: String?) {
        lifecycleScope.launch {
            try {
                // 1. Hacemos el inicio de sesión real con Firebase y Google
                val result = com.google.firebase.auth.FirebaseAuth.getInstance()
                    .signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                    .await()

                val user = result.user ?: return@launch
                val mail = user.email ?: email.orEmpty()
                val name = user.displayName ?: displayName.orEmpty()

                // 2. Sincroniza el perfil local
                AuthRepository.syncUserToRoom(this@LoginActivity, mail, name)
                com.estudiante.techscoop.data.SessionManager.init(applicationContext)
                com.estudiante.techscoop.data.SessionManager.loginUser(mail)
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

    // Entra a la app principal y limpia el back stack para no volver al login con Atrás.
    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}