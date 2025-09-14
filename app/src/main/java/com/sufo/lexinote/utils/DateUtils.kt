package com.sufo.lexinote.utils

import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

object DateUtils {
    fun getTimeAgo(date: Date): String {
        val now = Date()
        val diffInMillis = now.time - date.time
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

        return when {
            diffInDays < 1 -> "Today"
            diffInDays < 2 -> "Yesterday"
            diffInDays < 7 -> "$diffInDays days ago"
            diffInDays < 30 -> "${diffInDays / 7} weeks ago"
            diffInDays < 365 -> "${diffInDays / 30} months ago"
            else -> "${diffInDays / 365} years ago"
        }
    }

    fun getDaysUntil(date: Date): Long {
        val now = Date()
        val diffInMillis = date.time - now.time
        return TimeUnit.MILLISECONDS.toDays(diffInMillis)
    }

    fun subtractDays(date: Date, days: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        return calendar.time
    }
}
