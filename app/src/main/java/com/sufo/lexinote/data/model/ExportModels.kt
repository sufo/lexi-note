// Copyright (c) 2025 sufo
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.sufo.lexinote.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ExportedReviewLog(
    val originalWordId: Int,
    val reviewedAt: Long
)

@Serializable
data class ExportedWord(
    val originalId: Int,
    val word: String,
    val phonetic: String?,
    val translation: String?,
    val example: String?,
    val notes: String?,
    val imgs: List<String>,
    val audios: List<String>,
    val repetitions: Int,
    val easinessFactor: Float,
    val interval: Int,
    val nextReviewDate: Long
)

@Serializable
data class ExportedNotebook(
    val name: String,
    val description: String?,
    val iconResName: String,
    val words: List<ExportedWord>
)

@Serializable
data class ExportData(
    val exportDate: Long,
    val notebooks: List<ExportedNotebook>,
    val reviewLogs: List<ExportedReviewLog>
)