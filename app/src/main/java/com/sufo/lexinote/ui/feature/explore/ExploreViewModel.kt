package com.sufo.lexinote.ui.feature.explore

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.sufo.lexinote.constants.VocTemplate
import com.sufo.lexinote.constants.VocabularyTemplates
import com.sufo.lexinote.data.local.db.DictDatabaseHelper
import com.sufo.lexinote.data.local.db.entity.Notebook
import com.sufo.lexinote.data.local.db.entity.Word
import com.sufo.lexinote.data.repo.NotebookRepository
import com.sufo.lexinote.data.repo.WordRepository
import com.sufo.lexinote.ui.base.BaseViewModel
import com.sufo.lexinote.ui.navigation.NavigationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class ExploreUiState(
    val templates: List<VocabularyTemplate> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class VocabularyTemplate(
    val vocabulary: VocTemplate,
    val isAdded: Boolean = false
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val notebookRepository: NotebookRepository,
    private val wordRepository: WordRepository,
    private val dictDatabaseHelper: DictDatabaseHelper,
    private val nav: NavigationService,
    private val application: Application
) : BaseViewModel(application, nav) {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                notebookRepository.getAllNotebooks()
                    .combine(MutableStateFlow(VocabularyTemplates.all)) { notebooks, templates ->
                        val addedNotebookNames = notebooks.map { it.name }.toSet()
                        templates.map {
                            VocabularyTemplate(
                                vocabulary = it,
                                isAdded = addedNotebookNames.contains(it.name)
                            )
                        }
                    }.collect { templates ->
                        _uiState.update { it.copy(templates = templates, isLoading = false) }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to load data.", isLoading = false) }
            }
        }
    }

    fun importVocabulary(vocabulary: VocTemplate) {
        viewModelScope.launch {
            // 1. Create new notebook
            val iconResName = application.resources.getResourceEntryName(vocabulary.iconRes)
            val newNotebook = Notebook(name = vocabulary.name, iconResName = iconResName, wordCount = vocabulary.wordCount)
            val notebookId = notebookRepository.createNotebook(newNotebook)

            // 2. Get all words from dict
            val dictWords = dictDatabaseHelper.getAllWordsByTag(vocabulary.key)

            // 3. Convert to Word entities
            val words = dictWords.map {
                Word(
                    notebookId = notebookId.toInt(),
                    word = it.word,
                    phonetic = it.phonetic ?: "",
                    translation = it.translation ?: "",
                    example = "", // No example from dict
                    notes = "",
                    imgs = "",
                    audios = "",
                    nextReviewDate = Date()
                )
            }

            // 4. Add words to repository
            wordRepository.addWords(words) // Assuming addWords method exists
        }
    }
}