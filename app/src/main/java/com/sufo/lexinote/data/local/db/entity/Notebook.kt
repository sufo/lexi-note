package com.sufo.lexinote.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "notebooks")
data class Notebook(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String="",
    val iconResName: String="", // Storing the resource name as a String
    val description: String?=null,
    val wordCount: Int = 0, // Total words in this notebook
    val masteredWordCount: Int = 0, // Words with masteryLevel >= 5
    val reviewCount: Int = 0, // Number of words due for review
    val learnedToday: Int = 0, // Number of words learned today
    val createdAt: Long = System.currentTimeMillis()
)