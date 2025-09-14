package com.sufo.lexinote.data.repo

import com.sufo.lexinote.data.local.db.dao.UserDao
import com.sufo.lexinote.data.local.db.entity.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(private val userDao: UserDao) {

    fun getUser(id: String): Flow<User> = userDao.getUser(id)

    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }
}