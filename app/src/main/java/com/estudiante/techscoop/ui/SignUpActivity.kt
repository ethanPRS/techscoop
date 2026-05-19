package com.estudiante.techscoop.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.estudiante.techscoop.R
import com.estudiante.techscoop.SessionManager
import com.estudiante.techscoop.repository.AuthRepository
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val etName = findViewById<TextInputEditText>(R.id.etName)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirm = findViewById<TextInputEditText>(R.id.etConfirmPassword)

        findViewById<Button>(R.id.btnSignUp).setOnClickListener {
            val name = etName.text?.toString().orEmpty().trim()
            val email = etEmail.text?.toString().orEmpty().trim()
            val password = etPassword.text?.toString().orEmpty()
            val confirm = etConfirm.text?.toString().orEmpty()

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

        findViewById<TextView>(R.id.tvGoToLogin).setOnClickListener { finish() }
    }

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

