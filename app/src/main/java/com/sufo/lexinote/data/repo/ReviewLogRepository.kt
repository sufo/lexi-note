// Copyright (c) 2025 sufo
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.sufo.lexinote.data.repo

import com.sufo.lexinote.data.local.db.dao.ReviewLogDao
import com.sufo.lexinote.data.local.db.entity.ReviewLog
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewLogRepository @Inject constructor(private val reviewLogDao: ReviewLogDao) {

    suspend fun addReviewLog(log: ReviewLog) {
        reviewLogDao.insert(log)
    }

    fun getAllLogs(): Flow<List<ReviewLog>> {
        return reviewLogDao.getAllLogs()
    }
}
