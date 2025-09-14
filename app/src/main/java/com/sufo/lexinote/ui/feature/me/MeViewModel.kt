// Copyright (c) 2025 sufo
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.sufo.lexinote.ui.feature.me

import android.app.Application
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.sufo.lexinote.data.model.ExportData
import com.sufo.lexinote.data.model.ExportedNotebook
import com.sufo.lexinote.data.model.ExportedReviewLog
import com.sufo.lexinote.data.model.ExportedWord
import com.sufo.lexinote.data.repo.NotebookRepository
import com.sufo.lexinote.data.repo.ReviewLogRepository
import com.sufo.lexinote.data.repo.StatsRepository
import com.sufo.lexinote.data.repo.WordRepository
import com.sufo.lexinote.ui.base.BaseViewModel
import com.sufo.lexinote.ui.navigation.NavigationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject

data class MeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val exportFile: File? = null,
    val triggerImport: Boolean = false,
    val masteredCount: Int = 0,
    val studyDays: Int = 0,
    val currentStreak: Int = 0
)

@HiltViewModel
class MeViewModel @Inject constructor(
    application: Application,
    private val navigationService: NavigationService,
    private val notebookRepository: NotebookRepository,
    private val wordRepository: WordRepository,
    private val statsRepository: StatsRepository,
    private val reviewLogRepository: ReviewLogRepository
) : BaseViewModel(application, navigationService) {

    private val _uiState = MutableStateFlow(MeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            combine(
                wordRepository.getMasteredWordCount(),
                statsRepository.getStudyDaysCount(),
                statsRepository.getCurrentStreak()
            ) { mastered, days, streak ->
                MeUiState(
                    isLoading = false,
                    masteredCount = mastered,
                    studyDays = days,
                    currentStreak = streak
                )
            }.collect { newState ->
                _uiState.update {
                    it.copy(
                        isLoading = newState.isLoading,
                        masteredCount = newState.masteredCount,
                        studyDays = newState.studyDays,
                        currentStreak = newState.currentStreak
                    )
                }
            }
        }
    }

    fun onExportClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val allNotebooks = notebookRepository.getAllNotebooks().first()
                val allWords = wordRepository.getAllWords().first()
                val allReviewLogs = reviewLogRepository.getAllLogs().first()

                val tempExportDir = File(getApplication<Application>().cacheDir, "export_temp")
                if (tempExportDir.exists()) tempExportDir.deleteRecursively()
                tempExportDir.mkdirs()

                val imagesDir = File(tempExportDir, "images")
                imagesDir.mkdirs()
                val audiosDir = File(tempExportDir, "audios")
                audiosDir.mkdirs()

                val exportedNotebooks = allNotebooks.map { notebook ->
                    val wordsInNotebook = allWords
                        .filter { it.notebookId == notebook.id }
                        .map { word ->
                            val imageFileNames = word.imgs?.split(",")?.mapNotNull { path ->
                                val file = File(path)
                                if (file.exists()) {
                                    file.copyTo(File(imagesDir, file.name))
                                    file.name
                                } else null
                            } ?: emptyList()

                            val audioFileNames = word.audios?.split(",")?.mapNotNull { path ->
                                val file = File(path)
                                if (file.exists()) {
                                    file.copyTo(File(audiosDir, file.name))
                                    file.name
                                } else null
                            } ?: emptyList()

                            ExportedWord(
                                originalId = word.id,
                                word = word.word,
                                phonetic = word.phonetic,
                                translation = word.translation,
                                example = word.example,
                                notes = word.notes,
                                imgs = imageFileNames,
                                audios = audioFileNames,
                                repetitions = word.repetitions,
                                easinessFactor = word.easinessFactor,
                                interval = word.interval,
                                nextReviewDate = word.nextReviewDate.time
                            )
                        }
                    ExportedNotebook(
                        name = notebook.name,
                        description = notebook.description,
                        iconResName = notebook.iconResName,
                        words = wordsInNotebook
                    )
                }

                val exportedReviewLogs = allReviewLogs.map { log ->
                    ExportedReviewLog(
                        originalWordId = log.wordId,
                        reviewedAt = log.reviewedAt.time
                    )
                }

                val exportData = ExportData(
                    exportDate = System.currentTimeMillis(),
                    notebooks = exportedNotebooks,
                    reviewLogs = exportedReviewLogs
                )

                val jsonString = Json.encodeToString(exportData)
                File(tempExportDir, "data.json").writeText(jsonString)

                val zipFile = File(getApplication<Application>().cacheDir, "lexinote_backup.zip")
                ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
                    tempExportDir.walkTopDown().forEach { file ->
                        if (file.isFile) {
                            val entry = ZipEntry(file.relativeTo(tempExportDir).path)
                            zipOut.putNextEntry(entry)
                            file.inputStream().use { it.copyTo(zipOut) }
                            zipOut.closeEntry()
                        }
                    }
                }
                tempExportDir.deleteRecursively()
                _uiState.update { it.copy(isLoading = false, exportFile = zipFile) }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onExportHandled() {
        _uiState.value.exportFile?.delete()
        _uiState.update { it.copy(exportFile = null) }
    }

    fun saveExportedFile(uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value.exportFile?.let { file ->
                    getApplication<Application>().contentResolver.openOutputStream(uri)?.use { outputStream ->
                        file.inputStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            } finally {
                onExportHandled()
            }
        }
    }

    fun onImportClicked() {
        _uiState.update { it.copy(triggerImport = true) }
    }

    fun onImportTriggered() {
        _uiState.update { it.copy(triggerImport = false) }
    }

    fun onFileSelectedForImport(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val tempImportDir = File(getApplication<Application>().cacheDir, "import_temp")
                if (tempImportDir.exists()) tempImportDir.deleteRecursively()
                tempImportDir.mkdirs()

                getApplication<Application>().contentResolver.openInputStream(uri)?.use { inputStream ->
                    ZipInputStream(inputStream).use { zipInputStream ->
                        var entry = zipInputStream.nextEntry
                        while (entry != null) {
                            val file = File(tempImportDir, entry.name)
                            if (!entry.isDirectory) {
                                // Ensure the parent directory exists before writing the file
                                val parentDir = file.parentFile
                                if (parentDir != null && !parentDir.exists()) {
                                    parentDir.mkdirs()
                                }
                                file.outputStream().use { fileOutputStream ->
                                    zipInputStream.copyTo(fileOutputStream)
                                }
                            }
                            entry = zipInputStream.nextEntry
                        }
                    }
                }

                val dataFile = File(tempImportDir, "data.json")
                if (dataFile.exists()) {
                    val jsonString = dataFile.readText()
                    val importData = Json.decodeFromString<ExportData>(jsonString)
                    notebookRepository.importData(importData, tempImportDir)
                } else {
                    throw Exception("data.json not found in the backup file.")
                }

                tempImportDir.deleteRecursively()
                _uiState.update { it.copy(isLoading = false) }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
