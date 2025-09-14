// Copyright (c) 2025 sufo
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.sufo.lexinote.data.repo

import android.app.Application
import androidx.room.withTransaction
import com.sufo.lexinote.data.local.db.AppDatabase
import com.sufo.lexinote.data.local.db.dao.NotebookDao
import com.sufo.lexinote.data.local.db.dao.ReviewLogDao
import com.sufo.lexinote.data.local.db.dao.WordDao
import com.sufo.lexinote.data.local.db.entity.Notebook
import com.sufo.lexinote.data.local.db.entity.ReviewLog
import com.sufo.lexinote.data.local.db.entity.Word
import com.sufo.lexinote.data.model.ExportData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.io.File
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

data class NotebookWithStats(
    val notebook: Notebook,
    val totalWordCount: Int,
    val masteredWordCount: Int,
    val dueForReviewCount: Int
) {
    val isReviewable: Boolean get() = dueForReviewCount > 0
}

@Singleton
class NotebookRepository @Inject constructor(
    private val application: Application,
    private val notebookDao: NotebookDao,
    private val wordDao: WordDao,
    private val reviewLogDao: ReviewLogDao,
    private val appDatabase: AppDatabase
) {

    fun getAllNotebooks(): Flow<List<Notebook>> = notebookDao.getAllNotebooks()

    fun getNotebookById(id: Int): Flow<Notebook?> = notebookDao.getNotebookById(id)

    suspend fun createNotebook(notebook: Notebook): Long {
        return notebookDao.insertNotebook(notebook)
    }

    suspend fun updateNotebook(notebook: Notebook) {
        notebookDao.updateNotebook(notebook)
    }

    suspend fun deleteNotebook(notebook: Notebook) {
        appDatabase.withTransaction {
            notebook.id?.let { wordDao.deleteWordsByNotebookId(it) }
            notebookDao.deleteNotebook(notebook)
        }
    }

    suspend fun importData(importData: ExportData, tempImportDir: File) {
        val imagesDir = File(application.filesDir, "images")
        if (!imagesDir.exists()) imagesDir.mkdirs()
        val audiosDir = File(application.filesDir, "audios")
        if (!audiosDir.exists()) audiosDir.mkdirs()

        val wordIdMap = mutableMapOf<Int, Int>()

        appDatabase.withTransaction {
            importData.notebooks.forEach { exportedNotebook ->
                var newNotebookName = exportedNotebook.name
                val existingNotebook = notebookDao.getNotebookByName(newNotebookName).first()
                if (existingNotebook != null) {
                    newNotebookName = "${exportedNotebook.name} (Imported)"
                }

                val newNotebook = Notebook(
                    name = newNotebookName,
                    description = exportedNotebook.description,
                    iconResName = exportedNotebook.iconResName
                )
                val newNotebookId = notebookDao.insertNotebook(newNotebook)

                exportedNotebook.words.forEach { exportedWord ->
                    val imagePaths = exportedWord.imgs.map { fileName ->
                        val sourceFile = File(tempImportDir, "images/$fileName")
                        val destFile = File(imagesDir, fileName)
                        if (sourceFile.exists()) {
                            sourceFile.copyTo(destFile, true)
                            destFile.absolutePath
                        } else {
                            ""
                        }
                    }.filter { it.isNotBlank() }.joinToString(",")

                    val audioPaths = exportedWord.audios.map { fileName ->
                        val sourceFile = File(tempImportDir, "audios/$fileName")
                        val destFile = File(audiosDir, fileName)
                        if (sourceFile.exists()) {
                            sourceFile.copyTo(destFile, true)
                            destFile.absolutePath
                        } else {
                            ""
                        }
                    }.filter { it.isNotBlank() }.joinToString(",")

                    val wordToInsert = Word(
                        notebookId = newNotebookId.toInt(),
                        word = exportedWord.word,
                        phonetic = exportedWord.phonetic,
                        translation = exportedWord.translation,
                        example = exportedWord.example,
                        notes = exportedWord.notes,
                        imgs = imagePaths,
                        audios = audioPaths,
                        repetitions = exportedWord.repetitions,
                        easinessFactor = exportedWord.easinessFactor,
                        interval = exportedWord.interval,
                        nextReviewDate = Date(exportedWord.nextReviewDate)
                    )
                    val newWordId = wordDao.insertWord(wordToInsert)
                    wordIdMap[exportedWord.originalId] = newWordId.toInt()
                }
            }

            importData.reviewLogs.forEach { exportedLog ->
                val newWordId = wordIdMap[exportedLog.originalWordId]
                if (newWordId != null) {
                    reviewLogDao.insert(
                        ReviewLog(
                            wordId = newWordId,
                            reviewedAt = Date(exportedLog.reviewedAt)
                        )
                    )
                }
            }
        }
    }

    // This is a more complex implementation that might be slow if there are many notebooks.
    // A more optimized version would use a single complex SQL query.
    fun getNotebooksWithStats(): Flow<List<NotebookWithStats>> {
        return notebookDao.getAllNotebooks().combine(wordDao.getAllWords()) { notebooks, words ->
            notebooks.map { notebook ->
                val wordsInNotebook = words.filter { it.notebookId == notebook.id }
                val dueCount = wordsInNotebook.count { it.nextReviewDate.before(Date()) }
                val masteredCount = wordsInNotebook.count { it.repetitions >= 5 }
                NotebookWithStats(
                    notebook = notebook,
                    totalWordCount = wordsInNotebook.size,
                    masteredWordCount = masteredCount,
                    dueForReviewCount = dueCount
                )
            }
        }
    }
}