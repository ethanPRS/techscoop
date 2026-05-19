package com.estudiante.techscoop.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.estudiante.techscoop.R
import com.estudiante.techscoop.repository.AuthRepository
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

// Pantalla para solicitar el correo de restablecimiento de contraseña (Firebase).
class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)

                // Envía el email de reset; muestra mensaje de éxito o error de red.
        findViewById<Button>(R.id.btnSendReset).setOnClickListener {
            val email = etEmail.text?.toString().orEmpty().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, R.string.login_required_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                AuthRepository.sendPasswordReset(email).fold(
                    onSuccess = {
                        Toast.makeText(this@ForgotPasswordActivity, R.string.forgot_success, Toast.LENGTH_LONG).show()
                        finish()
                    },
                    onFailure = { e ->
                        Toast.makeText(
                            this@ForgotPasswordActivity,
                            AuthRepository.userMessage(this@ForgotPasswordActivity, e),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        }

        findViewById<TextView>(R.id.tvBackLogin).setOnClickListener { finish() }
    }
}


