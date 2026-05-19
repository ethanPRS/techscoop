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
import com.estudiante.techscoop.data.PreferencesManager
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

        //  MANEJO DE NOTICIAS
        viewModel.news.observe(viewLifecycleOwner) { articles ->
            adapter.updateData(articles ?: emptyList())
            
            if (!articles.isNullOrEmpty()) {
                // Si la API nos devuelve noticias, mostramos el mensaje de bienvenida 
                // (solo si no se ha mostrado antes en esta sesión)
                if (!SessionManager.isWelcomeToastShown) {
                    android.widget.Toast.makeText(requireContext(), "Estas viendo las noticias mas recientes de hoy!!", android.widget.Toast.LENGTH_LONG).show()
                    SessionManager.isWelcomeToastShown = true
                }
            } else {
                // Si la API devuelve 0 resultados,
                // en lugar de dejar la pantalla en blanco, informamos al usuario de forma amigable.
                android.widget.Toast.makeText(requireContext(), "No se encontraron noticias para estas preferencias", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        // SISTEMA REACTIVO DE PREFERENCIAS
        // Observamos el timbre del PreferencesManager. Cuando el usuario cambia el idioma o categoría
        // en su perfil, este observer se activa automáticamente.
        PreferencesManager.preferencesChanged.observe(viewLifecycleOwner) { timestamp ->
            // Verificamos el timestamp para asegurarnos de que es una configuración nueva
            // y no una vieja señal recargada por rotar la pantalla.
            if (timestamp > viewModel.lastPreferencesTimestamp) {
                viewModel.lastPreferencesTimestamp = timestamp
                
                // Si hay internet, mandamos traer las noticias nuevas inmediatamente
                // usando las nuevas preferencias, actualizando el feed en tiempo real.
                if (NetworkUtils.isOnline(requireContext())) {
                    viewModel.fetchNews()
                } else {
                    android.widget.Toast.makeText(requireContext(), getString(R.string.offline_gate_title), android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }

        if (viewModel.news.value.isNullOrEmpty() && viewModel.lastPreferencesTimestamp == 0L) {
            if (NetworkUtils.isOnline(requireContext())) {
                viewModel.lastPreferencesTimestamp = System.currentTimeMillis() // Marcar como leido inicial
                viewModel.fetchNews()
            } else {
                android.widget.Toast.makeText(requireContext(), getString(R.string.offline_gate_title), android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
