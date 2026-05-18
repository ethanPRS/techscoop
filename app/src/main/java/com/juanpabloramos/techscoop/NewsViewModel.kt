package com.juanpabloramos.techscoop

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class NewsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ArticleRepository(application.applicationContext)

    val news = MutableLiveData<List<DataArticle>>()
    val error = MutableLiveData<String?>()

    fun fetchNews() {
        viewModelScope.launch {
            when (val result = repository.getNews()) {
                is ApiResult.Success -> {
                    error.postValue(null)
                    news.postValue(result.articles)
                }
                is ApiResult.Error -> {
                    error.postValue(result.message)
                    news.postValue(emptyList())
                }
                is ApiResult.Exception -> {
                    error.postValue(result.error)
                    news.postValue(emptyList())
                }
            }
        }
    }

    fun searchNews(query: String) {
        viewModelScope.launch {
            when (val result = repository.searchNews(SearchFilters(query = query))) {
                is ApiResult.Success -> {
                    error.postValue(null)
                    news.postValue(result.articles)
                }
                is ApiResult.Error -> {
                    error.postValue(result.message)
                    news.postValue(emptyList())
                }
                is ApiResult.Exception -> {
                    error.postValue(result.error)
                    news.postValue(emptyList())
                }
            }
        }
    }

    fun fetchRecommended(interestKeys: Set<String>) {
        viewModelScope.launch {
            when (val result = repository.getTechnologyNews()) {
                is ApiResult.Success -> {
                    error.postValue(null)
                    news.postValue(
                        ArticleRepository.filterByInterests(result.articles, interestKeys)
                    )
                }
                is ApiResult.Error -> {
                    error.postValue(result.message)
                    news.postValue(emptyList())
                }
                is ApiResult.Exception -> {
                    error.postValue(result.error)
                    news.postValue(emptyList())
                }
            }
        }
    }
}
