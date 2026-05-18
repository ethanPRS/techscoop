package com.estudiante.techscoop

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.estudiante.techscoop.databinding.FragmentBusquedaBinding
import com.estudiante.techscoop.model.SearchFilters
import com.estudiante.techscoop.ui.ArticleDetailFragment
import com.estudiante.techscoop.ui.ArticleTestAdapter
import com.estudiante.techscoop.viewmodel.NewsViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class SearchFragment : Fragment() {

    private var _binding: FragmentBusquedaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NewsViewModel by viewModels()
    private lateinit var adapter: ArticleTestAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBusquedaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, bundle: Bundle?) {
        super.onViewCreated(view, bundle)

        // Inicializar adaptador con el evento de clic
        adapter = ArticleTestAdapter(emptyList()) { article ->
            val fragment = ArticleDetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("article", article)
                }
            }
            parentFragmentManager.commit {
                replace(R.id.main_fragment_container, fragment)
                addToBackStack(null)
                setReorderingAllowed(true)
            }
        }

        binding.rvResultados.layoutManager = LinearLayoutManager(requireContext())
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

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBusqueda.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnBuscar.isEnabled = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                binding.tvBusquedaStatus.text = "❌ $msg"
            }
        }

        viewModel.news.observe(viewLifecycleOwner) { articles ->
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
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        view?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
