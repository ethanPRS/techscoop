package com.estudiante.techscoop.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.estudiante.techscoop.NetworkUtils
import com.estudiante.techscoop.R
import com.estudiante.techscoop.data.SessionManager
import com.estudiante.techscoop.databinding.FragmentHomeBinding
import com.estudiante.techscoop.viewmodel.NewsViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NewsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ArticleTestAdapter(emptyList()) { article ->
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
        binding.rvArticles.layoutManager = LinearLayoutManager(requireContext())
        binding.rvArticles.adapter = adapter

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            if (errorMsg != null) {
                android.widget.Toast.makeText(requireContext(), "Error: $errorMsg", android.widget.Toast.LENGTH_LONG).show()
            }
        }

        viewModel.news.observe(viewLifecycleOwner) { articles ->
            if (!articles.isNullOrEmpty()) {
                adapter.updateData(articles)
                if (!SessionManager.isWelcomeToastShown) {
                    android.widget.Toast.makeText(requireContext(), "Estas viendo las noticias mas recientes de hoy!!", android.widget.Toast.LENGTH_LONG).show()
                    SessionManager.isWelcomeToastShown = true
                }
            }
        }

        if (viewModel.news.value.isNullOrEmpty()) {
            if (NetworkUtils.isOnline(requireContext())) {
                viewModel.fetchNews()
            } else {
                binding.tvStatus.text = getString(R.string.offline_gate_title)
                binding.tvStatus.setBackgroundColor(0xFFFFCDD2.toInt())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
