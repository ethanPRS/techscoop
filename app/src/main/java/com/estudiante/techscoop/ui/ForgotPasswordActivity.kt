package com.estudiante.techscoop.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.estudiante.techscoop.R
import com.estudiante.techscoop.databinding.ActivityForgotPasswordBinding
import com.estudiante.techscoop.repository.AuthRepository
import kotlinx.coroutines.launch
import android.view.inputmethod.EditorInfo

// Pantalla para solicitar el correo de restablecimiento de contraseña (Firebase).
class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etEmail.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                binding.btnSendReset.performClick()
                true
            } else {
                false
            }
        }

        // Envía el email de reset; muestra mensaje de éxito o error de red.
        binding.btnSendReset.setOnClickListener {
            val email = binding.etEmail.text?.toString().orEmpty().trim()
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

        binding.tvBackLogin.setOnClickListener { finish() }
    }
}