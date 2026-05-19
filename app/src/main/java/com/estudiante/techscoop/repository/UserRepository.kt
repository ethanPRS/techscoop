package com.estudiante.techscoop.repository

import com.estudiante.techscoop.data.local.UserDao
import com.estudiante.techscoop.data.local.UserEntity

class UserRepository(private val userDao: UserDao) {
    suspend fun getUser() = userDao.getUser()
    suspend fun insertUser(user: UserEntity) = userDao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)
    suspend fun deleteUser(user: UserEntity) = userDao.deleteUser(user)
    suspend fun clearAll() = userDao.clearAll()
}

