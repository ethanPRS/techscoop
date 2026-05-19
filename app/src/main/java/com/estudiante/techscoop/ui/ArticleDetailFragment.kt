package com.estudiante.techscoop.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import coil.load
import com.estudiante.techscoop.R
import com.estudiante.techscoop.databinding.DetalleFragmentBinding
import com.estudiante.techscoop.model.DataArticle

class ArticleDetailFragment : Fragment(R.layout.detalle_fragment) {

    private var _binding: DetalleFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = DetalleFragmentBinding.bind(view)

        val article = arguments?.getSerializable("article") as? DataArticle

        article?.let {
            setupUI(it)
            setupWebView(it.url)
        }
    }

    private fun setupUI(article: DataArticle) {
        binding.tvTituloDetalle.text = article.title
        binding.tvContenido.text = article.description ?: "Cargando noticia completa..."
        
        binding.ivNoticiaHeader.load(article.urlToImage) {
            placeholder(R.drawable.ic_placeholder)
            error(R.drawable.ic_placeholder)
            crossfade(true)
        }
    }

    private fun setupWebView(url: String?) {
        if (url.isNullOrBlank()) return

        binding.webView.apply {
            ViewCompat.setNestedScrollingEnabled(this, true)

            @android.annotation.SuppressLint("SetJavaScriptEnabled")
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    _binding?.pbWebView?.visibility = View.VISIBLE
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    _binding?.pbWebView?.visibility = View.GONE
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    _binding?.pbWebView?.visibility = View.GONE
                }
            }

            loadUrl(url)
        }
    }

    override fun onDestroyView() {
        if (_binding != null) {
            binding.webView.stopLoading()
        }
        super.onDestroyView()
        _binding = null
    }
}
