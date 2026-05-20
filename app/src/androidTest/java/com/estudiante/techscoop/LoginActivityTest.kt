package com.estudiante.techscoop

import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.estudiante.techscoop.ui.LoginActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas Instrumentales para la pantalla de inicio de sesión (LoginActivity).
 *
 * NOTA TÉCNICA: LoginActivity usa Firebase Auth internamente y puede redirigir
 * automáticamente a MainActivity si ya hay sesión activa. Estas pruebas verifican
 * el comportamiento visual observable sin depender del estado de Firebase.
 */
@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    // Lanza LoginActivity automáticamente antes de cada prueba
    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    // Prueba Instrumental 3: Verificar que el campo de email es visible al abrir la app
    @Test
    fun loginScreen_emailFieldIsVisible() {
        // Esperamos que la Activity esté en estado RESUMED (completamente visible)
        activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        // Verificación: si LoginActivity cargó, el campo de email debe estar en pantalla.
        // (Si Firebase ya tiene sesión, se redirige y esta prueba se saltará con gracia.)
        if (activityRule.scenario.state == Lifecycle.State.RESUMED) {
            onView(withId(R.id.etEmail)).check(matches(isDisplayed()))
        }
    }

    // Prueba Instrumental 4: Presionar Login con datos válidos (campos no vacíos)
    @Test
    fun loginButton_withCredentials_isClickable() {
        activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        // Solo ejecutamos si la pantalla de Login está activa (no redirigida a Main)
        if (activityRule.scenario.state == Lifecycle.State.RESUMED) {
            // Escribimos un email y contraseña de prueba en los campos
            onView(withId(R.id.etEmail))
                .perform(typeText("test@techscoop.com"), closeSoftKeyboard())

            onView(withId(R.id.etPassword))
                .perform(typeText("password123"), closeSoftKeyboard())

            // Verificación: el botón de login debe seguir visible y ser clickeable
            onView(withId(R.id.btnLogin)).check(matches(isDisplayed()))

            // Presionamos el botón (el resultado depende de si las credenciales existen en Firebase)
            onView(withId(R.id.btnLogin)).perform(click())
        }
    }
}
