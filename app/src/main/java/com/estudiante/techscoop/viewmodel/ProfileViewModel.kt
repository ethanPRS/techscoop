package com.estudiante.techscoop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.estudiante.techscoop.data.local.AppDatabase
import com.estudiante.techscoop.data.local.UserEntity
import com.estudiante.techscoop.repository.UserRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    // Se inicializa la base de datos y el repositorio
    private val userDao = AppDatabase.getInstance(application).userDao()
    private val repository = UserRepository(userDao)

    val user = MutableLiveData<UserEntity?>()
    val error = MutableLiveData<String?>()
    val loading = MutableLiveData<Boolean>()

    init {
        loadUser()
    }

    fun loadUser() {
        viewModelScope.launch {
            loading.postValue(true)
            try {
                val currentUser = repository.getUser()
                user.postValue(currentUser)
                error.postValue(null)
            } catch (e: Exception) {
                error.postValue(e.message)
                user.postValue(null)
            }
            loading.postValue(false)
        }
    }

    fun updateProfile(name: String, email: String, bio: String, pass: String, uri: String?) {
        viewModelScope.launch {
            loading.postValue(true)
            try {
                user.value?.let { currentUser ->
                    val updatedUser = currentUser.copy(
                        name = name,
                        email = email,
                        bio = bio,
                        password = pass,
                        profileImageUri = uri ?: currentUser.profileImageUri
                    )
                    repository.updateUser(updatedUser)
                    user.postValue(updatedUser)
                    error.postValue(null)
                }
            } catch (e: Exception) {
                error.postValue(e.message)
            }
            loading.postValue(false)
        }
    }

    fun deactivateAccount() {
        viewModelScope.launch {
            loading.postValue(true)
            try {
                user.value?.let { currentUser ->
                    val timeNow = Calendar.getInstance().timeInMillis
                    val updatedUser = currentUser.copy(
                        status = "inactivo",
                        deactivationDate = timeNow
                    )
                    repository.updateUser(updatedUser)
                    user.postValue(updatedUser)
                    error.postValue(null)
                }
            } catch (e: Exception) {
                error.postValue(e.message)
            }
            loading.postValue(false)
        }
    }
}