package com.juanpabloramos.techscoop

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Punto de entrada legado: redirige al shell principal en la pestaña Explorar.
 */
class FeedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(
            Intent(this, MainActivity::class.java).putExtra(
                MainActivity.EXTRA_INITIAL_TAB,
                MainActivity.TAB_EXPLORE
            )
        )
        finish()
    }
}
