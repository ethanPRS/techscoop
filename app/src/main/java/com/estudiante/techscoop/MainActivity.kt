package com.estudiante.techscoop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.estudiante.techscoop.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: NewsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val adapter = ArticleTestAdapter(emptyList())
        binding.rvArticles.layoutManager = LinearLayoutManager(this)
        binding.rvArticles.adapter = adapter

        // Observar errores
        viewModel.error.observe(this) { errorMsg ->
            if (errorMsg != null) {
                binding.progressBar.visibility = View.GONE
                binding.btnFetch.isEnabled = true
                binding.tvStatus.text = "❌ Error: $errorMsg"
                binding.tvStatus.setBackgroundColor(0xFFFFCDD2.toInt())
                binding.tvCount.text = ""
            }
        }

        // Observar artículos
        viewModel.news.observe(this) { articles ->
            binding.progressBar.visibility = View.GONE
            binding.btnFetch.isEnabled = true

            if (!articles.isNullOrEmpty()) {
                binding.tvStatus.text = "✅ API respondió correctamente"
                binding.tvStatus.setBackgroundColor(0xFFE8F5E9.toInt())
                binding.tvCount.text = "  ${articles.size} artículos obtenidos de TechCrunch"
                adapter.updateData(articles)
            }
        }

        // Botón → llamar API
        binding.btnFetch.setOnClickListener {
            binding.btnFetch.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
            binding.tvStatus.text = "⏳ Llamando a la API..."
            binding.tvStatus.setBackgroundColor(0xFFE8EAF6.toInt())
            binding.tvCount.text = ""
            adapter.updateData(emptyList())
            viewModel.fetchNews()
        }
    }
}

// ─── Adapter ─────────────────────────────────────────────────────────────────

class ArticleTestAdapter(private var articles: List<DataArticle>) :
    RecyclerView.Adapter<ArticleTestAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvSource: TextView = view.findViewById(R.id.tvSource)
        val tvTitle: TextView = view.findViewById(R.id.tvArticleTitle)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvAuthor: TextView = view.findViewById(R.id.tvAuthor)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
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
    }

    override fun getItemCount() = articles.size

    fun updateData(newArticles: List<DataArticle>) {
        articles = newArticles
        notifyDataSetChanged()
    }
}
