package com.estudiante.techscoop

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.estudiante.techscoop.data.local.AppDatabase
import com.estudiante.techscoop.data.local.UserDao
import com.estudiante.techscoop.data.local.UserEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas Instrumentales para la Base de Datos (Room).
 * Requieren correr en un dispositivo o emulador real para crear la base de datos SQL.
 */

@RunWith(AndroidJUnit4::class)
class DatabaseInstrumentalTest {
    private lateinit var userDao: UserDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Usamos inMemoryDatabaseBuilder para que los datos desaparezcan al terminar la prueba
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        userDao = db.userDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    // Prueba Instrumental 1: Inserción y Lectura de Usuario
    @Test
    fun insertAndReadUser() = runBlocking {
        // Preparación
        val user = UserEntity(
            uid = 1,
            name = "Prueba",
            email = "prueba@mail.com",
            password = "123",
            bio = "Bio de prueba",
            profileImageUri = null,
            status = "activo",
            deactivationDate = null
        )

        // Ejecución (insertar en BD)
        userDao.insertUser(user)

        // Verificación (leer de BD y comparar)
        val readUser = userDao.getUser()
        assertEquals("El nombre guardado debe coincidir", "Prueba", readUser?.name)
        assertEquals("El email guardado debe coincidir", "prueba@mail.com", readUser?.email)
    }

    // Prueba Instrumental 2: Actualización de Usuario existente (No duplicar)
    @Test
    fun updateExistingUser() = runBlocking {
        // Preparación: Insertamos al usuario original
        val originalUser = UserEntity(
            uid = 1,
            name = "Original",
            email = "original@mail.com",
            password = "123",
            bio = null,
            profileImageUri = null,
            status = "activo",
            deactivationDate = null
        )
        userDao.insertUser(originalUser)

        // Ejecución: Actualizamos la biografía de ese mismo usuario
        val updatedUser = originalUser.copy(bio = "Nueva bio actualizada")
        userDao.updateUser(updatedUser) // updateUser existe en UserDao en la implementación real

        // Verificación: Al recuperar al usuario, la biografía debe ser la nueva
        val readUser = userDao.getUser()
        assertEquals("La biografía debe haber cambiado a la versión actualizada", "Nueva bio actualizada", readUser?.bio)
    }
}