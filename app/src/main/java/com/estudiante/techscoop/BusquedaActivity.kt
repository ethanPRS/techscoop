package com.estudiante.techscoop

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.estudiante.techscoop.databinding.BusquedaActivityBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class BusquedaActivity : AppCompatActivity() {

    private lateinit var binding: BusquedaActivityBinding
    private val viewModel: NewsViewModel by viewModels()
    private lateinit var adapter: ArticleTestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BusquedaActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarBusqueda.setNavigationOnClickListener { finish() }

        adapter = ArticleTestAdapter(emptyList())
        binding.rvResultados.layoutManager = LinearLayoutManager(this)
        binding.rvResultados.adapter = adapter

        binding.etQuery.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                doSearch()
                true
            } else false
        }

        binding.btnBuscar.setOnClickListener {
            hideKeyboard()
            doSearch()
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.progressBusqueda.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnBuscar.isEnabled = !isLoading
        }

        viewModel.error.observe(this) { msg ->
            if (msg != null) {
                binding.tvBusquedaStatus.text = "❌ $msg"
            }
        }

        viewModel.news.observe(this) { articles ->
            if (articles.isNullOrEmpty()) {
                if (viewModel.error.value == null) {
                    binding.tvBusquedaStatus.text = "Sin resultados"
                }
                adapter.updateData(emptyList())
            } else {
                binding.tvBusquedaStatus.text = "✅ ${articles.size} resultados"
                adapter.updateData(articles)
            }
        }
    }

    private fun doSearch() {
        val query = binding.etQuery.text?.toString().orEmpty().trim()
        if (query.isBlank()) {
            binding.tvBusquedaStatus.text = "Escribe palabras clave para buscar"
            return
        }

        val source = selectedTag(binding.cgSources)
        val language = selectedTag(binding.cgLanguage) ?: "en"
        val sortBy = selectedTag(binding.cgSort) ?: "publishedAt"
        val days = selectedTag(binding.cgPeriod)?.toIntOrNull() ?: 0
        val from = if (days > 0) isoDateDaysAgo(days) else null

        viewModel.search(
            SearchFilters(
                query = query,
                sources = source,
                language = language,
                sortBy = sortBy,
                from = from,
                to = null
            )
        )
    }

    private fun selectedTag(group: ChipGroup): String? {
        val id = group.checkedChipId
        if (id == View.NO_ID) return null
        val chip = group.findViewById<Chip>(id) ?: return null
        return chip.tag?.toString()
    }

    private fun isoDateDaysAgo(days: Int): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.add(Calendar.DAY_OF_YEAR, -days)
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(cal.time)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
    }
}
