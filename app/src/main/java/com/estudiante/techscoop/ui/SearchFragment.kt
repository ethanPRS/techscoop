package com.estudiante.techscoop.ui

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
import com.estudiante.techscoop.R
import com.estudiante.techscoop.databinding.FragmentBusquedaBinding
import com.estudiante.techscoop.model.SearchFilters
import com.estudiante.techscoop.viewmodel.NewsViewModel
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class SearchFragment : Fragment() {

    private var _binding: FragmentBusquedaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NewsViewModel by viewModels()
    private lateinit var adapter: ArticleTestAdapter

    // ─────────────── Filter state ───────────────

    /** Source filter: null = all sources */
    private var selectedSource: FilterOption = sourceOptions[0]

    /** Sort filter */
    private var selectedSort: FilterOption = sortOptions[0]

    /** Period filter: days back (0 = any) */
    private var selectedPeriod: FilterOption = periodOptions[0]

    /** Language filter */
    private var selectedLanguage: FilterOption = languageOptions[0]

    // ─────────────── Filter definitions ───────────────

    data class FilterOption(val label: String, val value: String?)

    companion object {
        val sourceOptions = listOf(
            FilterOption("All Sources", null),
            FilterOption("TechCrunch", "techcrunch"),
            FilterOption("The Verge", "the-verge"),
            FilterOption("Ars Technica", "ars-technica"),
            FilterOption("Wired", "wired"),
            FilterOption("Engadget", "engadget"),
            FilterOption("Hacker News", "hacker-news"),
            FilterOption("BBC News", "bbc-news")
        )

        val sortOptions = listOf(
            FilterOption("Most Recent", "publishedAt"),
            FilterOption("Relevance", "relevancy"),
            FilterOption("Popularity", "popularity")
        )

        val periodOptions = listOf(
            FilterOption("Any Time", "0"),
            FilterOption("Last 24 Hours", "1"),
            FilterOption("Last 3 Days", "3"),
            FilterOption("Last Week", "7"),
            FilterOption("Last Month", "30")
        )

        val languageOptions = listOf(
            FilterOption("English", "en"),
            FilterOption("Spanish", "es"),
            FilterOption("French", "fr"),
            FilterOption("German", "de"),
            FilterOption("Portuguese", "pt")
        )
    }

    // ─────────────── Lifecycle ───────────────

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
        setupRecyclerView()
        setupFilterChips()
        setupListeners()
        setupObservers()
    }

    // ─────────────── RecyclerView ───────────────

    private fun setupRecyclerView() {
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
        binding.rvResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvResults.adapter = adapter
    }

    // ─────────────── Filter chip setup ───────────────

    private fun setupFilterChips() {
        binding.chipFilterSource.setOnClickListener { showFilterPopup(it, sourceOptions, selectedSource) { opt -> selectedSource = opt; updateChipAppearance(binding.chipFilterSource, opt, sourceOptions[0]) } }
        binding.chipFilterSort.setOnClickListener { showFilterPopup(it, sortOptions, selectedSort) { opt -> selectedSort = opt; updateChipAppearance(binding.chipFilterSort, opt, sortOptions[0]) } }
        binding.chipFilterPeriod.setOnClickListener { showFilterPopup(it, periodOptions, selectedPeriod) { opt -> selectedPeriod = opt; updateChipAppearance(binding.chipFilterPeriod, opt, periodOptions[0]) } }
        binding.chipFilterLanguage.setOnClickListener { showFilterPopup(it, languageOptions, selectedLanguage) { opt -> selectedLanguage = opt; updateChipAppearance(binding.chipFilterLanguage, opt, languageOptions[0]) } }

        binding.chipClearFilters.setOnClickListener { resetFilters() }

        // Initialize chip text
        updateChipAppearance(binding.chipFilterSource, selectedSource, sourceOptions[0])
        updateChipAppearance(binding.chipFilterSort, selectedSort, sortOptions[0])
        updateChipAppearance(binding.chipFilterPeriod, selectedPeriod, periodOptions[0])
        updateChipAppearance(binding.chipFilterLanguage, selectedLanguage, languageOptions[0])
    }

    private fun showFilterPopup(
        anchor: View,
        options: List<FilterOption>,
        currentSelection: FilterOption,
        onSelected: (FilterOption) -> Unit
    ) {
        val popup = android.widget.PopupMenu(requireContext(), anchor)
        options.forEachIndexed { index, option ->
            val item = popup.menu.add(0, index, index, option.label)
            // Show a check mark on the currently selected item
            if (option.value == currentSelection.value) {
                item.isChecked = true
            }
        }
        popup.menu.setGroupCheckable(0, true, true)

        popup.setOnMenuItemClickListener { menuItem ->
            val selected = options[menuItem.itemId]
            onSelected(selected)
            updateClearButtonVisibility()
            true
        }
        popup.show()
    }

    @Suppress("ResourceAsColor")
    private fun updateChipAppearance(chip: Chip, selected: FilterOption, default: FilterOption) {
        chip.text = selected.label
        val isActive = selected.value != default.value
        if (isActive) {
            // Active state: filled indigo
            chip.setChipBackgroundColorResource(android.R.color.transparent)
            chip.chipBackgroundColor = android.content.res.ColorStateList.valueOf(0x1A3F51B5.toInt()) // 10% indigo
            chip.chipStrokeColor = android.content.res.ColorStateList.valueOf(0xFF3F51B5.toInt())
            chip.setTextColor(0xFF3F51B5.toInt())
        } else {
            // Default state: outlined gray
            chip.chipBackgroundColor = android.content.res.ColorStateList.valueOf(0xFFFFFFFF.toInt())
            chip.chipStrokeColor = android.content.res.ColorStateList.valueOf(0xFFDDDDDD.toInt())
            chip.setTextColor(0xFF555555.toInt())
        }
    }

    private fun updateClearButtonVisibility() {
        val hasActiveFilter = selectedSource != sourceOptions[0]
                || selectedSort != sortOptions[0]
                || selectedPeriod != periodOptions[0]
                || selectedLanguage != languageOptions[0]
        binding.chipClearFilters.visibility = if (hasActiveFilter) View.VISIBLE else View.GONE
    }

    private fun resetFilters() {
        selectedSource = sourceOptions[0]
        selectedSort = sortOptions[0]
        selectedPeriod = periodOptions[0]
        selectedLanguage = languageOptions[0]

        updateChipAppearance(binding.chipFilterSource, selectedSource, sourceOptions[0])
        updateChipAppearance(binding.chipFilterSort, selectedSort, sortOptions[0])
        updateChipAppearance(binding.chipFilterPeriod, selectedPeriod, periodOptions[0])
        updateChipAppearance(binding.chipFilterLanguage, selectedLanguage, languageOptions[0])
        updateClearButtonVisibility()
    }

    // ─────────────── Search listeners ───────────────

    private fun setupListeners() {
        binding.etQuery.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                doSearch()
                true
            } else false
        }

        binding.btnSearch.setOnClickListener {
            hideKeyboard()
            doSearch()
        }
    }

    // ─────────────── Observers ───────────────

    private fun setupObservers() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressSearch.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSearch.isEnabled = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                binding.tvSearchStatus.text = "❌ $msg"
            }
        }

        viewModel.news.observe(viewLifecycleOwner) { articles ->
            if (articles.isNullOrEmpty()) {
                if (viewModel.error.value == null) {
                    binding.tvSearchStatus.text = "No results found"
                }
                adapter.updateData(emptyList())
            } else {
                val query = binding.etQuery.text?.toString().orEmpty().trim()
                binding.tvSearchStatus.text = "✅ ${articles.size} results for \"$query\""
                adapter.updateData(articles)
            }
        }
    }

    // ─────────────── Search execution ───────────────

    private fun doSearch() {
        val query = binding.etQuery.text?.toString().orEmpty().trim()
        if (query.isBlank()) {
            binding.tvSearchStatus.text = "Enter keywords to search"
            return
        }

        val days = selectedPeriod.value?.toIntOrNull() ?: 0
        val from = if (days > 0) isoDateDaysAgo(days) else null

        viewModel.search(
            SearchFilters(
                query = query,
                sources = selectedSource.value,
                language = selectedLanguage.value,
                sortBy = selectedSort.value,
                from = from,
                to = null
            )
        )
    }

    // ─────────────── Helpers ───────────────

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
