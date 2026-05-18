package com.juanpabloramos.techscoop

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: NewsViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
    }
    private lateinit var adapter: NewsAdapter
    private lateinit var recycler: RecyclerView
    private var cachedNews: List<NewsItem> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.tvHomeSectionTitle).text =
            getString(R.string.home_recommended_title)
        view.findViewById<TextView>(R.id.tvHomeSectionSubtitle).text =
            getString(R.string.home_recommended_subtitle)
        recycler = view.findViewById(R.id.rvHomeNews)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.setHasFixedSize(true)
        recycler.itemAnimator = null
        attachAdapter(emptyList())

        viewModel.news.observe(viewLifecycleOwner) { articles ->
            if (!articles.isNullOrEmpty()) {
                cachedNews = articles.map { it.toNewsItem() }
                adapter.updateData(
                    ArticleLists.withoutDismissed(requireContext(), cachedNews)
                )
            }
        }
        viewModel.error.observe(viewLifecycleOwner) { err ->
            if (!err.isNullOrBlank()) {
                Toast.makeText(requireContext(), err, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshFeed()
    }

    fun onScreenVisible() {
        refreshFeed()
    }

    fun refreshFeed() {
        if (!isAdded) return
        attachAdapter(ArticleLists.withoutDismissed(requireContext(), cachedNews))
        val interests = UserPreferences.getInterestKeys(requireContext())
        viewModel.fetchRecommended(interests)
    }

    private fun attachAdapter(items: List<NewsItem>) {
        adapter = NewsAdapter(
            items,
            showOverflowMenu = true,
            onCardMenuAction = { item, action ->
                when (action) {
                    NewsCardMenuAction.SAVE -> SaveArticleHelper.saveFromFragment(this@HomeFragment, item)
                    NewsCardMenuAction.NOT_INTERESTED -> {
                        DismissedArticlesStore.add(requireContext(), item)
                        Toast.makeText(
                            requireContext(),
                            R.string.news_not_interested_done,
                            Toast.LENGTH_SHORT
                        ).show()
                        adapter.updateData(
                            ArticleLists.withoutDismissed(requireContext(), cachedNews)
                        )
                    }
                }
            }
        )
        recycler.adapter = adapter
    }
}
