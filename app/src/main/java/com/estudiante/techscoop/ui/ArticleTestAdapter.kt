package com.estudiante.techscoop.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.estudiante.techscoop.R
import com.estudiante.techscoop.data.FavoritesManager
import com.estudiante.techscoop.databinding.ItemArticleTestBinding
import com.estudiante.techscoop.model.DataArticle

/**
 * ArticleTestAdapter es el puente entre la lista de noticias
 * y el RecyclerView que muestra la lista en pantalla.
 */
class ArticleTestAdapter(
    private var articles: List<DataArticle>,
    private val onItemClick: (DataArticle) -> Unit // Acción a ejecutar cuando se toca una noticia
) : RecyclerView.Adapter<ArticleTestAdapter.VH>() {

    /**
     * Aquí conectamos las variables usando ViewBinding directamente en el ViewHolder.
     */
    inner class VH(val binding: ItemArticleTestBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            // Configuramos qué pasa cuando el usuario toca toda la tarjeta de la noticia
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(articles[position])
                }
            }
        }
    }

    /**
     * Este método se llama cuando el RecyclerView necesita crear una nueva celda visual.
     * Solo crea las celdas necesarias para llenar la pantalla usando ViewBinding.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemArticleTestBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    /**
     * Este método asocia los datos de una noticia específica (texto, imagen)
     * a la celda visual en una posición particular.
     */
    override fun onBindViewHolder(holder: VH, position: Int) {
        val article = articles[position]

        // Usamos el bloque 'with' para evitar escribir 'holder.binding' en cada línea
        with(holder.binding) {
            // Asignamos los textos, usando un valor por defecto si vienen nulos
            tvSource.text = article.source?.name ?: "No Source Available"
            tvArticleTitle.text = article.title ?: "No Title Available"
            tvDescription.text = article.description ?: "No Description Available"
            tvAuthor.text = "✍️ ${article.author ?: "Unknown"}"
            tvDate.text = article.publishedAt?.take(10) ?: "" // Muestra solo YYYY-MM-DD

            // Cargamos la imagen de internet usando la librería Coil
            ivArticleImage.load(article.urlToImage) {
                crossfade(true) // Animación suave al cargar
                placeholder(R.drawable.ic_placeholder) // Imagen mientras carga
                error(R.drawable.ic_placeholder) // Imagen si falla la carga
            }

            // Verificamos si esta noticia ya es favorita al cargar la celda
            val isFav = FavoritesManager.isFavorite(article)
            ivFavorite.setImageResource(
                if (isFav) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
            )

            // Cuando el usuario toca el ícono del corazón
            ivFavorite.setOnClickListener {
                // toggleFavorite la agrega o la quita de la lista y devuelve su nuevo estado
                val isNowFav = FavoritesManager.toggleFavorite(article)
                // Actualizamos el ícono visualmente (corazón lleno o vacío)
                ivFavorite.setImageResource(
                    if (isNowFav) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
                )
            }
        }
    }

    /**
     * Le dice a la lista cuántos elementos en total hay para mostrar.
     */
    override fun getItemCount() = articles.size

    /**
     * Función útil para actualizar toda la lista de noticias de golpe
     * y notificar a la interfaz que debe volverse a cargar.
     */
    fun updateData(newArticles: List<DataArticle>) {
        articles = newArticles
        notifyDataSetChanged()
    }
}