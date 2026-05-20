package com.estudiante.techscoop.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.estudiante.techscoop.R
import com.estudiante.techscoop.SessionManager
import com.estudiante.techscoop.databinding.ActivitySignUpBinding
import com.estudiante.techscoop.repository.AuthRepository
import kotlinx.coroutines.launch
import android.view.inputmethod.EditorInfo

// Registro de cuenta nueva con Firebase (email/contraseña) y validación de formulario.
class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Escuchar el "Enter/Done" del teclado en el último campo
        binding.etConfirmPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                binding.btnSignUp.performClick()
                true
            } else {
                false
            }
        }

        // Valida contraseñas coincidentes y longitud mínima antes de llamar a Firebase.
        binding.btnSignUp.setOnClickListener {
            val name = binding.etName.text?.toString().orEmpty().trim()
            val email = binding.etEmail.text?.toString().orEmpty().trim()
            val password = binding.etPassword.text?.toString().orEmpty()
            val confirm = binding.etConfirmPassword.text?.toString().orEmpty()

            when {
                email.isEmpty() || password.isEmpty() -> {
                    Toast.makeText(this, R.string.login_required_fields, Toast.LENGTH_SHORT).show()
                }
                password.length < 6 -> {
                    Toast.makeText(this, R.string.signup_password_short, Toast.LENGTH_SHORT).show()
                }
                password != confirm -> {
                    Toast.makeText(this, R.string.signup_password_mismatch, Toast.LENGTH_SHORT).show()
                }
                else -> register(name, email, password)
            }
        }

        binding.tvGoToLogin.setOnClickListener { finish() }
    }

    // Crea usuario en Firebase, sincroniza Room y abre MainActivity.
    private fun register(name: String, email: String, password: String) {
        lifecycleScope.launch {
            AuthRepository.signUp(email, password, name).fold(
                onSuccess = { user ->
                    val display = user.displayName ?: name.ifBlank { email.substringBefore("@") }
                    AuthRepository.syncUserToRoom(this@SignUpActivity, email, display, password)
                    SessionManager.saveLastEmail(this@SignUpActivity, email)
                    goToMain()
                },
                onFailure = { e ->
                    Toast.makeText(
                        this@SignUpActivity,
                        AuthRepository.userMessage(this@SignUpActivity, e),
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
    }

    private fun goToMain() {
        startActivity(
            Intent(this, MainActivity::class.java).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
        )
        finish()
    }
}