package com.estudiante.techscoop

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class NewsViewModel : ViewModel() {

    private val repository = ArticleRepository()

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
}
