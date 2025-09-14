
package com.sufo.lexinote.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class SearchHistory(
    @PrimaryKey
    val word: String,
    val transition: String?=null,
    val timestamp: Long = System.currentTimeMillis()
)
