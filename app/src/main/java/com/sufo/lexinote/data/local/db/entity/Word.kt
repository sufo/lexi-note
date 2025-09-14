package com.sufo.lexinote.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "words")
data class Word(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val notebookId: Int,
    val word: String,
    val phonetic: String?=null,
    val translation: String?=null,
    val example: String?=null,
    val notes: String?=null,
    val imgs: String?=null,
    val audios: String?=null,

    // --- Spaced Repetition System Fields ---
    // The number of times the word has been successfully recalled in a row.
    @ColumnInfo(defaultValue = "0")
    val repetitions: Int = 0,

    // A factor that determines how quickly the review interval grows.
    @ColumnInfo(defaultValue = "2.5")
    val easinessFactor: Float = 2.5f,

    // The last interval in days between reviews.
    @ColumnInfo(defaultValue = "0")
    val interval: Int = 0,

    // The next date the word should be reviewed.
    val nextReviewDate: Date,

    @ColumnInfo(name = "createdAt", defaultValue = "CURRENT_TIMESTAMP")
    val createdAt: Date? = null,

    @ColumnInfo(name = "updatedAt", defaultValue = "CURRENT_TIMESTAMP")
    val updatedAt: Date? = null
)
