package com.juanpabloramos.techscoop

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import coil.load

enum class NewsCardMenuAction {
    SAVE,
    NOT_INTERESTED
}

class NewsAdapter(
    private var items: List<NewsItem>,
    private val showOverflowMenu: Boolean = true,
    private val onCardMenuAction: ((NewsItem, NewsCardMenuAction) -> Unit)? = null
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.ivNewsImage)
        val titleText: TextView = view.findViewById(R.id.tvNewsTitle)
        val sourceText: TextView = view.findViewById(R.id.tvNewsSource)
        val dateText: TextView = view.findViewById(R.id.tvNewsDate)
        val overflowButton: ImageButton = view.findViewById(R.id.btnOverflowMenu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news_card, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val item = items[position]
        holder.titleText.text = item.title
        holder.sourceText.text = item.source
        holder.dateText.text = item.date

        holder.imageView.load(item.imageUrl) {
            placeholder(R.drawable.news_image_placeholder)
            error(R.drawable.news_image_placeholder)
            crossfade(300)
        }

        holder.overflowButton.visibility =
            if (showOverflowMenu) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            val articleUrl = item.articleUrl
            if (!articleUrl.isNullOrBlank()) {
                val ctx = holder.itemView.context
                ctx.startActivity(
                    Intent(ctx, ArticleDetailActivity::class.java).apply {
                        putExtra(ArticleDetailActivity.EXTRA_URL, articleUrl)
                        putExtra(ArticleDetailActivity.EXTRA_TITLE, item.title)
                    }
                )
            } else {
                Toast.makeText(
                    holder.itemView.context,
                    holder.itemView.context.getString(R.string.news_opened),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        holder.overflowButton.setOnClickListener { anchor ->
            val ctx = anchor.context
            val popup = PopupMenu(ctx, anchor)
            popup.inflate(R.menu.news_card_overflow)
            popup.setOnMenuItemClickListener { mi ->
                when (mi.itemId) {
                    R.id.menu_save_article -> {
                        onCardMenuAction?.invoke(item, NewsCardMenuAction.SAVE)
                            ?: Toast.makeText(ctx, R.string.news_saved, Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.menu_not_interested -> {
                        onCardMenuAction?.invoke(item, NewsCardMenuAction.NOT_INTERESTED)
                            ?: Toast.makeText(ctx, R.string.news_not_interested_done, Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<NewsItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
