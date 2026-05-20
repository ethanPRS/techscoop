package com.estudiante.techscoop.data

import android.content.Context
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Pruebas Unitarias para PreferencesManager.
 *
 * SOLUCIÓN TÉCNICA: PreferencesManager llama internamente a SessionManager.getCurrentUserEmail()
 * para construir la llave por usuario. SessionManager también es un Singleton con lateinit var
 * que no puede inicializarse en JVM pura.
 *
 * Usamos `mockkObject(SessionManager)` — una función de MockK diseñada específicamente para
 * mockear objetos Singleton de Kotlin — para interceptar la llamada sin inicializar Android.
 */
class PreferencesManagerTest {

    // Regla para sincronizar los LiveData en el entorno de prueba JVM
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Mocks para el Context y SharedPreferences de PreferencesManager
    private lateinit var mockContext: Context
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    // Mapa en memoria que simula el almacenamiento real de SharedPreferences
    private val storage = mutableMapOf<String, String?>()

    @Before
    fun setup() {
        // 1. Mockeamos el Singleton SessionManager para que no necesite Context ni SharedPreferences
        //    reales. Cada vez que alguien pida el email actual, responderemos "null" (usuario anónimo).
        mockkObject(SessionManager)
        every { SessionManager.getCurrentUserEmail() } returns null

        // 2. Preparamos el mock de SharedPreferences para PreferencesManager
        mockContext = mockk()
        mockPrefs   = mockk()
        mockEditor  = mockk(relaxed = true)

        // Cuando se llame a getSharedPreferences(), devolvemos nuestro mock
        every { mockContext.getSharedPreferences(any(), any()) } returns mockPrefs
        // Cuando se llame a edit(), devolvemos el editor falso
        every { mockPrefs.edit() } returns mockEditor
        // Cuando se llame a getString(), leemos del mapa en memoria (o devolvemos el default)
        every { mockPrefs.getString(any(), any()) } answers {
            storage[firstArg()] ?: secondArg()
        }

        // 3. Inicializamos PreferencesManager con el Context falso
        PreferencesManager.init(mockContext)

        // 4. Limpiamos el almacenamiento para garantizar aislamiento entre pruebas
        storage.clear()
    }

    @After
    fun tearDown() {
        // Liberamos el mock del Singleton después de cada prueba para no afectar otras clases
        unmockkObject(SessionManager)
    }

    // Prueba 7: Verificar que init() no lanza excepciones
    @Test
    fun `init successfully sets up SharedPreferences`() {
        // Si llegamos aquí sin un crash, la prueba pasa.
        PreferencesManager.init(mockContext)
    }

    // Prueba 8: getLanguage() devuelve "en" cuando no se ha guardado nada
    @Test
    fun `getLanguage returns default value 'en' when not set`() {
        // El mapa de almacenamiento está vacío → getString devuelve el segundo argumento (el default).
        val language = PreferencesManager.getLanguage()
        assertEquals("El idioma por defecto debe ser 'en'", "en", language)
    }

    // Prueba 9: getCategory() devuelve "technology" cuando no se ha guardado nada
    @Test
    fun `getCategory returns default value 'technology' when not set`() {
        // El mapa de almacenamiento está vacío → getString devuelve el segundo argumento (el default).
        val category = PreferencesManager.getCategory()
        assertEquals("La categoría por defecto debe ser 'technology'", "technology", category)
    }
}
