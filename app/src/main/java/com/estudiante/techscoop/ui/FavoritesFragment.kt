package com.estudiante.techscoop.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import com.estudiante.techscoop.R
import com.estudiante.techscoop.data.FavoritesManager
import com.estudiante.techscoop.databinding.FragmentFavoritesBinding

/**
 * FavoritesFragment se encarga de mostrar todas las noticias
 * que el usuario ha guardado como favoritas.
 */
class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    // Adaptador que se encarga de dibujar cada noticia en la lista
    private lateinit var adapter: ArticleTestAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Cconectamos la vista XML con este fragmento de código
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuramos el adaptador de la lista.
        adapter = ArticleTestAdapter(emptyList()) { article ->
            // Al hacer clic, abrimos la pantalla de detalles (ArticleDetailFragment)
            // pasándole la información de la noticia seleccionada.
            val fragment = ArticleDetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("article", article)
                }
            }
            parentFragmentManager.commit {
                replace(R.id.main_fragment_container, fragment)
                addToBackStack(null) // Permite volver atrás al presionar el botón de retroceso
                setReorderingAllowed(true)
            }
        }

        // Preparamos el RecyclerView (la lista visual)
        binding.rvFavorites.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavorites.adapter = adapter

        // Observamos los cambios en la lista de favoritos en tiempo real.
        FavoritesManager.favoriteArticles.observe(viewLifecycleOwner) { favorites ->
            if (favorites.isEmpty()) {
                // Si no hay favoritos, mostramos el mensaje de "Lista vacía" y ocultamos la lista
                binding.tvEmptyFavorites.visibility = View.VISIBLE
                binding.rvFavorites.visibility = View.GONE
            } else {
                // Si sí hay favoritos, ocultamos el mensaje, mostramos la lista y actualizamos los datos
                binding.tvEmptyFavorites.visibility = View.GONE
                binding.rvFavorites.visibility = View.VISIBLE
                adapter.updateData(favorites)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Limpiamos el binding para evitar fugas de memoria cuando la vista se destruye
        _binding = null
    }
}
