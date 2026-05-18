package com.juanpabloramos.techscoop

import android.widget.Toast
import androidx.fragment.app.Fragment

object SaveArticleHelper {
    fun saveFromFragment(fragment: Fragment, item: NewsItem) {
        val context = fragment.requireContext()
        when (SavedArticlesStore.save(context, item)) {
            SaveArticleResult.Success ->
                Toast.makeText(context, R.string.news_saved_to_list, Toast.LENGTH_SHORT).show()
            SaveArticleResult.AlreadySaved ->
                Toast.makeText(context, R.string.saved_already, Toast.LENGTH_SHORT).show()
            is SaveArticleResult.Failed ->
                Toast.makeText(context, R.string.saved_download_failed, Toast.LENGTH_SHORT).show()
        }
    }
}
