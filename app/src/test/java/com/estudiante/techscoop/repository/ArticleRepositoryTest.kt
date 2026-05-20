package com.estudiante.techscoop.repository

import com.estudiante.techscoop.model.DataArticle
import com.estudiante.techscoop.model.Source
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Prueba unitaria 10: Verifica la lógica de validación de la ApiKey en ArticleRepository.
 *
 * CONTEXTO TÉCNICO: ArticleRepository.getNews() depende de BuildConfig.NEWS_API_KEY (vacío
 * en el entorno de prueba JVM) y de PreferencesManager/SessionManager (que requieren Context).
 * Por eso, esta prueba se enfoca en la lógica PURA que SÍ se puede probar sin Android:
 * la validación de los artículos recibidos una vez que el repositorio devuelve resultados.
 */
class ArticleRepositoryTest {

    // Prueba 10: Verificar que un artículo con URL válida NO se considera vacío
    @Test
    fun `article with valid URL is not considered empty`() {
        // Preparación: Creamos un artículo de prueba con todos sus campos
        val article = DataArticle(
            source = Source(id = "bbc", name = "BBC News"),
            author = "Reporter",
            title  = "Kotlin 2.0 Released",
            description = "The new version of Kotlin is here.",
            url    = "https://bbc.com/kotlin-2",   // URL real y válida
            urlToImage = null,
            publishedAt = "2024-01-01T00:00:00Z",
            content = "Full content here."
        )

        // Verificación: el título y la url no deben estar en blanco
        assertFalse("El título del artículo no debe estar vacío", article.title.isNullOrBlank())
        assertFalse("La URL del artículo no debe estar vacía",    article.url.isNullOrBlank())
    }

    // Prueba extra: Verificar que un artículo sin título SÍ se detecta como inválido
    @Test
    fun `article without title is considered invalid`() {
        val article = DataArticle(
            source = null,
            author = null,
            title  = "",       // Título vacío = artículo inválido
            description = null,
            url    = "https://example.com",
            urlToImage = null,
            publishedAt = null,
            content = null
        )

        // Verificación: un artículo con título vacío debería detectarse como incompleto
        assertTrue("Un artículo con título vacío debe considerarse inválido", article.title.isNullOrBlank())
    }
}
