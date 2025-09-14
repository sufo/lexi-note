// Copyright (c) 2025 sufo
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.sufo.lexinote.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sufo.lexinote.data.local.db.entity.ReviewLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewLogDao {
    @Insert
    suspend fun insert(log: ReviewLog)

    @Query("SELECT * FROM review_logs ORDER BY reviewedAt DESC")
    fun getAllLogs(): Flow<List<ReviewLog>>
}
