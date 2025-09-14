// Copyright (c) 2025 sufo
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.sufo.lexinote.data.repo

import com.sufo.lexinote.data.local.db.dao.ReviewLogDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepository @Inject constructor(private val reviewLogDao: ReviewLogDao) {

    fun getStudyDaysCount(): Flow<Int> {
        return reviewLogDao.getAllLogs().map { logs ->
            logs.map { log ->
                val cal = Calendar.getInstance()
                cal.time = log.reviewedAt
                cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR)
            }.toSet().size
        }
    }

    fun getCurrentStreak(): Flow<Int> {
        return reviewLogDao.getAllLogs().map { logs ->
            if (logs.isEmpty()) return@map 0

            val distinctDays = logs.map { log ->
                val cal = Calendar.getInstance()
                cal.time = log.reviewedAt
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }.toSortedSet(reverseOrder())

            var streak = 0
            val today = Calendar.getInstance()
            today.set(Calendar.HOUR_OF_DAY, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)
            
            val yesterday = today.clone() as Calendar
            yesterday.add(Calendar.DAY_OF_YEAR, -1)

            if (distinctDays.first() == today.timeInMillis || distinctDays.first() == yesterday.timeInMillis) {
                var currentDay = today.timeInMillis
                if (distinctDays.first() == yesterday.timeInMillis) {
                    currentDay = yesterday.timeInMillis
                }

                for (day in distinctDays) {
                    if (day == currentDay) {
                        streak++
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = currentDay
                        cal.add(Calendar.DAY_OF_YEAR, -1)
                        currentDay = cal.timeInMillis
                    } else {
                        break
                    }
                }
            }
            streak
        }
    }
}
