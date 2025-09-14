package com.sufo.lexinote.data.local.db.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity

/**
 * Created by sufo on 2025/8/2 23:54.
 *
 */
@Immutable
@Entity(tableName = "stardict")
data class DictWord(
    val id: Int?=null,
    val word: String,
    val phonetic: String?=null,
    val translation: String?=null,
    val tag: String? = null,
    val exchange: String?= null,
    val bnc: Int? = null,
    val frq: Int? = null,
    val imageUrl: String? = null,
    val examples: List<String> = emptyList(),
    val sourceDictionary: String? = null,
)
