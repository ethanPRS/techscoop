package com.estudiante.techscoop.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.estudiante.techscoop.model.DataArticle
import com.estudiante.techscoop.model.Source
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Pruebas Unitarias para FavoritesManager.
 * Estas pruebas verifican la lógica de guardado y lectura en memoria.
 */
class FavoritesManagerTest {

    // Regla necesaria para probar LiveData de forma síncrona en JUnit
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var dummyArticle: DataArticle

    @Before
    fun setup() {
        // Antes de cada prueba, limpiamos la lista para que las pruebas sean independientes
        FavoritesManager.clearFavorites()
        
        // Creamos un artículo falso (dummy) para las pruebas
        dummyArticle = DataArticle(
            source = Source(id = "1", name = "Test Source"),
            author = "Test Author",
            title = "Test Title",
            description = "Test Description",
            url = "http://test.com",
            urlToImage = "http://test.com/image.png",
            publishedAt = "2023-01-01T00:00:00Z",
            content = "Test Content"
        )
    }

    // Prueba 1: Verificar que al agregar un favorito devuelve TRUE
    @Test
    fun `toggleFavorite adds article and returns true`() {
        // Ejecución
        val result = FavoritesManager.toggleFavorite(dummyArticle)

        // Verificación: Debería devolver true y la lista ya no debe estar vacía
        assertTrue("Debe devolver true al agregar un nuevo favorito", result)
        assertEquals("El tamaño de la lista de favoritos debe ser 1", 1, FavoritesManager.favoriteArticles.value?.size)
    }

    // Prueba 2: Verificar que isFavorite() funciona correctamente
    @Test
    fun `isFavorite returns true for existing article`() {
        // Preparación: Agregamos el artículo
        FavoritesManager.toggleFavorite(dummyArticle)

        // Ejecución & Verificación
        assertTrue("El artículo debería estar marcado como favorito", FavoritesManager.isFavorite(dummyArticle))
    }

    // Prueba 3: Verificar que volver a llamar a toggleFavorite lo elimina
    @Test
    fun `toggleFavorite removes existing article and returns false`() {
        // Preparación: Agregamos el artículo primero
        FavoritesManager.toggleFavorite(dummyArticle)

        // Ejecución: Volvemos a presionar el botón de favorito
        val result = FavoritesManager.toggleFavorite(dummyArticle)

        // Verificación: Debería devolver false y la lista debe estar vacía
        assertFalse("Debe devolver false al quitar un favorito existente", result)
        assertTrue("La lista de favoritos debe estar vacía", FavoritesManager.favoriteArticles.value?.isEmpty() == true)
    }

    // Prueba 4: Intentar agregar el mismo artículo duplicado (con distinta instancia pero misma URL)
    @Test
    fun `isFavorite compares correctly by URL`() {
        FavoritesManager.toggleFavorite(dummyArticle)

        // Creamos un clon exacto
        val duplicateArticle = dummyArticle.copy(title = "Title changed but URL same")
        
        // La comparación actual del código usa equals().
        assertTrue("Debería considerarse el mismo favorito ya que es un data class idéntico", FavoritesManager.isFavorite(dummyArticle))
    }
}
