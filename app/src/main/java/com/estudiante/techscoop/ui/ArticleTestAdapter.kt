package com.estudiante.techscoop.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.estudiante.techscoop.R
import com.estudiante.techscoop.data.FavoritesManager
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
     * Aquí conectamos las variables con los IDs del diseño XML.
     */
    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivArticleImage)
        val tvSource: TextView = view.findViewById(R.id.tvSource)
        val tvTitle: TextView = view.findViewById(R.id.tvArticleTitle)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvAuthor: TextView = view.findViewById(R.id.tvAuthor)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val ivFavorite: ImageView = view.findViewById(R.id.ivFavorite)

        init {
            // Configuramos qué pasa cuando el usuario toca toda la tarjeta de la noticia
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(articles[position])
                }
            }
        }
    }

    /**
     * Este método se llama cuando el RecyclerView necesita crear una nueva celda visual.
     * Solo crea las celdas necesarias para llenar la pantalla.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_article_test, parent, false)
        return VH(view)
    }

    /**
     * Este método asocia los datos de una noticia específica (texto, imagen)
     * a la celda visual en una posición particular.
     */
    override fun onBindViewHolder(holder: VH, position: Int) {
        val article = articles[position]
        
        // Asignamos los textos, usando un valor por defecto si vienen nulos
        holder.tvSource.text = article.source?.name ?: "No Source Available"
        holder.tvTitle.text = article.title ?: "No Title Available"
        holder.tvDescription.text = article.description ?: "No Description Available"
        holder.tvAuthor.text = "✍️ ${article.author ?: "Unkown"}"
        holder.tvDate.text = article.publishedAt?.take(10) ?: "" // Muestra solo YYYY-MM-DD

        // Cargamos la imagen de internet usando la librería Coil
        holder.ivImage.load(article.urlToImage) {
            crossfade(true) // Animación suave al cargar
            placeholder(R.drawable.ic_placeholder) // Imagen mientras carga
            error(R.drawable.ic_placeholder) // Imagen si falla la carga
        }


        // Verificamos si esta noticia ya es favorita al cargar la celda
        val isFav = FavoritesManager.isFavorite(article)
        holder.ivFavorite.setImageResource(
            if (isFav) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        )

        // Cuando el usuario toca el ícono del corazón
        holder.ivFavorite.setOnClickListener {
            // toggleFavorite la agrega o la quita de la lista y devuelve su nuevo estado
            val isNowFav = FavoritesManager.toggleFavorite(article)
            // Actualizamos el ícono visualmente (corazón lleno o vacío)
            holder.ivFavorite.setImageResource(
                if (isNowFav) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
            )
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
