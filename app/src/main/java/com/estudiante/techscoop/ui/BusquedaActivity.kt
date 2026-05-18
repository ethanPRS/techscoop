package com.estudiante.techscoop.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.estudiante.techscoop.R
import com.estudiante.techscoop.databinding.BusquedaActivityBinding

class BusquedaActivity : AppCompatActivity() {

    private lateinit var binding: BusquedaActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BusquedaActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarBusqueda.setNavigationOnClickListener { finish() }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, SearchFragment())
                .commit()
        }
    }
}
