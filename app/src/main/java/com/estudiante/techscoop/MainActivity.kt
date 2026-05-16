package com.estudiante.techscoop

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
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

        setupToolbar()

        val adapter = ArticleTestAdapter(emptyList())
        binding.rvArticles.layoutManager = LinearLayoutManager(this)
        binding.rvArticles.adapter = adapter

        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (isLoading) {
                binding.tvStatus.text = "⏳ Cargando últimas noticias..."
                binding.tvStatus.setBackgroundColor(0xFFE8EAF6.toInt())
                binding.tvCount.text = ""
            }
        }

        viewModel.error.observe(this) { errorMsg ->
            if (errorMsg != null) {
                binding.tvStatus.text = "❌ Error: $errorMsg"
                binding.tvStatus.setBackgroundColor(0xFFFFCDD2.toInt())
                binding.tvCount.text = ""
            }
        }

        viewModel.news.observe(this) { articles ->
            if (!articles.isNullOrEmpty()) {
                binding.tvStatus.text = "✅ Noticias actualizadas"
                binding.tvStatus.setBackgroundColor(0xFFE8F5E9.toInt())
                binding.tvCount.text = "  ${articles.size} artículos de TechCrunch"
                adapter.updateData(articles)
            }
        }

        if (savedInstanceState == null && viewModel.news.value.isNullOrEmpty()) {
            viewModel.fetchNews()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_search -> {
                    startActivity(Intent(this, BusquedaActivity::class.java))
                    true
                }
                R.id.action_perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    true
                }
                R.id.action_cerrar_sesion -> {
                    confirmLogout()
                    true
                }
                else -> false
            }
        }
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Seguro que quieres cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}

class ArticleTestAdapter(private var articles: List<DataArticle>) :
    RecyclerView.Adapter<ArticleTestAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivArticleImage)
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
