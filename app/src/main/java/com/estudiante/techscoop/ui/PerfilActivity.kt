package com.estudiante.techscoop.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.estudiante.techscoop.databinding.PerfilActivityBinding

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: PerfilActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PerfilActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarPerfil.setNavigationOnClickListener { finish() }
    }
}
