package com.juanpabloramos.techscoop

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ExploreFragment : Fragment(R.layout.fragment_explore) {

    private val viewModel: NewsViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
    }
    private lateinit var adapter: NewsAdapter
    private var lastError: String? = null
    private var masterList: List<NewsItem> = emptyList()
    private var searchQuery: String = ""
    private var apiSearchActive = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recycler = view.findViewById<RecyclerView>(R.id.rvNewsFeed)
        adapter = NewsAdapter(
            displayedList(),
            showOverflowMenu = true,
            onCardMenuAction = { item, action ->
                when (action) {
                    NewsCardMenuAction.SAVE -> SaveArticleHelper.saveFromFragment(this@ExploreFragment, item)
                    NewsCardMenuAction.NOT_INTERESTED -> {
                        DismissedArticlesStore.add(requireContext(), item)
                        masterList = masterList.filter { it.stableKey() != item.stableKey() }
                        Toast.makeText(
                            requireContext(),
                            R.string.news_not_interested_done,
                            Toast.LENGTH_SHORT
                        ).show()
                        refreshAdapter()
                    }
                }
            }
        )
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.setHasFixedSize(true)
        recycler.itemAnimator = null
        recycler.adapter = adapter

        view.findViewById<SearchView>(R.id.svNewsSearch).setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    val q = query.orEmpty().trim()
                    searchQuery = q
                    if (q.isEmpty()) {
                        apiSearchActive = false
                        viewModel.fetchNews()
                        return true
                    }
                    apiSearchActive = true
                    viewModel.searchNews(q)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    val q = newText.orEmpty()
                    searchQuery = q
                    if (apiSearchActive && q.isNotBlank()) return true
                    apiSearchActive = false
                    refreshAdapter()
                    return true
                }
            }
        )

        observeNews()
        viewModel.fetchNews()
    }

    fun refresh() {
        lastError = null
        apiSearchActive = false
        searchQuery = ""
        viewModel.fetchNews()
    }

    private fun observeNews() {
        viewModel.news.observe(viewLifecycleOwner) { articles ->
            if (!articles.isNullOrEmpty()) {
                masterList = articles.map { it.toNewsItem() }
                refreshAdapter()
            } else if (apiSearchActive) {
                masterList = emptyList()
                refreshAdapter()
            }
        }
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrBlank() && lastError != errorMessage) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                lastError = errorMessage
            }
        }
    }

    private fun displayedList(): List<NewsItem> {
        val base = ArticleLists.withoutDismissed(requireContext(), masterList)
        if (apiSearchActive) return base
        val q = searchQuery.trim()
        if (q.isEmpty()) return base
        return base.filter {
            it.title.contains(q, ignoreCase = true) ||
                it.source.contains(q, ignoreCase = true)
        }
    }

    private fun refreshAdapter() {
        adapter.updateData(displayedList())
    }
}
