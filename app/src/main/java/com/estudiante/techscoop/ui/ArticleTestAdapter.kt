package com.estudiante.techscoop.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.estudiante.techscoop.R
import com.estudiante.techscoop.model.DataArticle

class ArticleTestAdapter(
    private var articles: List<DataArticle>,
    private val onItemClick: (DataArticle) -> Unit
) : RecyclerView.Adapter<ArticleTestAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivArticleImage)
        val tvSource: TextView = view.findViewById(R.id.tvSource)
        val tvTitle: TextView = view.findViewById(R.id.tvArticleTitle)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvAuthor: TextView = view.findViewById(R.id.tvAuthor)
        val tvDate: TextView = view.findViewById(R.id.tvDate)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(articles[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_article_test, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val article = articles[position]
        holder.tvSource.text = article.source?.name ?: "Sin fuente"
        holder.tvTitle.text = article.title ?: "Sin título"
        holder.tvDescription.text = article.description ?: "Sin descripción"
        holder.tvAuthor.text = "✍️ ${article.author ?: "Desconocido"}"
        holder.tvDate.text = article.publishedAt?.take(10) ?: ""

        holder.ivImage.load(article.urlToImage) {
            crossfade(true)
            placeholder(R.drawable.ic_placeholder)
            error(R.drawable.ic_placeholder)
        }
    }

    override fun getItemCount() = articles.size

    fun updateData(newArticles: List<DataArticle>) {
        articles = newArticles
        notifyDataSetChanged()
    }
}
