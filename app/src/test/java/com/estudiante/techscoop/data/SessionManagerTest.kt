package com.estudiante.techscoop.data

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Pruebas Unitarias para SessionManager.
 *
 * PROBLEMA CONOCIDO: SessionManager usa SharedPreferences de Android, que NO existe
 * en pruebas JVM puras. La solución es usar MockK para crear un Context y
 * SharedPreferences "falsos" que simulan el comportamiento sin necesitar el SO de Android.
 */
class SessionManagerTest {

    // Variables que "juegan el papel" de Android en la prueba
    private lateinit var mockContext: Context
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    // Mapa en memoria que simula el almacenamiento de SharedPreferences
    private val inMemoryStorage = mutableMapOf<String, String?>()

    @Before
    fun setup() {
        mockContext = mockk()
        mockPrefs  = mockk()
        mockEditor = mockk(relaxed = true)

        // Cuando se llame a getSharedPreferences(), devolvemos nuestro mock
        every { mockContext.getSharedPreferences(any(), any()) } returns mockPrefs

        // Cuando alguien pida editar, devolvemos el editor falso
        every { mockPrefs.edit() } returns mockEditor

        // Simulamos putString: guarda en el mapa en memoria
        every { mockEditor.putString(any(), any()) } answers {
            inMemoryStorage[firstArg()] = secondArg()
            mockEditor
        }

        // Simulamos remove: borra del mapa en memoria
        every { mockEditor.remove(any()) } answers {
            inMemoryStorage.remove(firstArg<String>())
            mockEditor
        }

        // Simulamos getString: lee del mapa en memoria
        every { mockPrefs.getString(any(), any()) } answers {
            inMemoryStorage[firstArg()] ?: secondArg()
        }

        // Inicializamos el SessionManager con nuestro Context falso
        SessionManager.init(mockContext)

        // Limpiamos el almacenamiento antes de cada prueba para garantizar aislamiento
        inMemoryStorage.clear()
    }

    // Prueba 5: Verificar que al hacer login, el email se guarda correctamente
    @Test
    fun `loginUser stores the current email`() {
        val testEmail = "test@techscoop.com"

        // Ejecución: simulamos el login
        SessionManager.loginUser(testEmail)

        // Verificación: el email almacenado debe coincidir
        assertEquals("El email actual debería ser igual al guardado", testEmail, SessionManager.getCurrentUserEmail())
    }

    // Prueba 6: Verificar que al cerrar sesión, el email se limpia
    @Test
    fun `logoutUser clears the current email`() {
        val testEmail = "test@techscoop.com"

        // Preparación: Hacemos login primero
        SessionManager.loginUser(testEmail)

        // Ejecución: Hacemos logout
        SessionManager.logoutUser()

        // Verificación: El usuario actual debe ser nulo (el mapa en memoria estará vacío)
        assertNull("Después del logout, el usuario actual debe ser nulo", SessionManager.getCurrentUserEmail())
    }
}
