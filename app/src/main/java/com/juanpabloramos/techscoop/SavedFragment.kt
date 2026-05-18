package com.juanpabloramos.techscoop

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SavedFragment : Fragment(R.layout.fragment_saved) {

    private lateinit var adapter: NewsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recycler = view.findViewById<RecyclerView>(R.id.rvSavedNews)
        adapter = NewsAdapter(emptyList(), showOverflowMenu = false)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.setHasFixedSize(true)
        recycler.itemAnimator = null
        recycler.adapter = adapter
        bindList(view)
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    fun refreshList() {
        view?.let { bindList(it) }
    }

    private fun bindList(root: View) {
        val empty = root.findViewById<TextView>(R.id.tvSavedEmpty)
        val title = root.findViewById<TextView>(R.id.tvSavedTitle)
        val items = SavedArticlesStore.load(requireContext())

        title.text = getString(R.string.saved_screen_title)
        adapter.updateData(items)
        empty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        empty.text = getString(R.string.saved_empty_message)
    }
}
