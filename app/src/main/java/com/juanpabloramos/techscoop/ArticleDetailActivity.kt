package com.juanpabloramos.techscoop

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class ArticleDetailActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_detail)

        val url = intent.getStringExtra(EXTRA_URL)?.trim()
        val titleExtra = intent.getStringExtra(EXTRA_TITLE)?.trim().orEmpty()

        if (url.isNullOrBlank()) {
            finish()
            return
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarArticle)
        toolbar.title = titleExtra.ifBlank { getString(R.string.article_reader_title) }
        toolbar.setNavigationOnClickListener { navigateBack() }

        val progressBar = findViewById<View>(R.id.progressArticle)
        webView = findViewById(R.id.webViewArticle)

        webView.settings.apply {
            javaScriptEnabled = false
            domStorageEnabled = false
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean = true
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                findViewById<android.widget.ProgressBar>(R.id.progressArticle).apply {
                    visibility = if (newProgress in 1..99) View.VISIBLE else View.GONE
                    progress = newProgress
                }
            }
        }

        webView.loadUrl(url)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navigateBack()
                }
            }
        )
    }

    private fun navigateBack() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.clearHistory()
            webView.removeAllViews()
            webView.destroy()
        }
        super.onDestroy()
    }

    companion object {
        const val EXTRA_URL = "extra_article_url"
        const val EXTRA_TITLE = "extra_article_title"
    }
}
