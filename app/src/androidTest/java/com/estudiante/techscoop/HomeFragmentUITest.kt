package com.estudiante.techscoop.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.estudiante.techscoop.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Prueba Instrumental para HomeFragment.
 * Verifica que los componentes visuales principales carguen al abrir la app.
 */
@RunWith(AndroidJUnit4::class)
class HomeFragmentUITest {

    // Lanzamos la MainActivity que contiene el HomeFragment en su inicio
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    // Prueba Instrumental 5: Verificar que la lista de noticias es visible
    @Test
    fun homeFragment_displaysRecyclerView() {
        // Comprobamos directamente que el componente rvArticles esté en la pantalla
        onView(withId(R.id.rvArticles)).check(matches(isDisplayed()))
    }
}
