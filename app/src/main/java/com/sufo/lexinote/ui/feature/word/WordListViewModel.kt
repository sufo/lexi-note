package com.sufo.lexinote.ui.feature.word

import android.app.Application
import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.sufo.lexinote.data.local.db.entity.Word
import com.sufo.lexinote.data.repo.WordRepository
import com.sufo.lexinote.ui.base.BaseViewModel
import com.sufo.lexinote.ui.navigation.NavigationService
import com.sufo.lexinote.ui.navigation.Screen
import com.sufo.lexinote.utils.TtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

enum class SortOrder {
    A_TO_Z,
    Z_TO_A,
    PROFICIENCY,
    MODIFICATION_TIME,
    STUDY_TIME,
    RANDOM
}

sealed interface WordListUiState {
    data class Success(
        val notebookId: Int,
        val notebookName: String,
        val words: List<Word> = emptyList(),
        //val searchQuery: TextFieldState = TextFieldState("") //存在问题
        val searchQuery: TextFieldState,
        val isLoadingMore: Boolean = false,
        val endOfListReached: Boolean = false,
        val sortOrder: SortOrder = SortOrder.MODIFICATION_TIME
    ) : WordListUiState

    data class Error(val message: String) : WordListUiState
    object Loading : WordListUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WordListViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val nav: NavigationService,
    application: Application,
    savedStateHandle: SavedStateHandle
) : BaseViewModel(application, nav) {

    private val notebookId: Int = checkNotNull(savedStateHandle["notebookId"])
    private val notebookName: String = checkNotNull(savedStateHandle["notebookName"])

    private val searchQueryState = TextFieldState("")
    private val _uiState = MutableStateFlow<WordListUiState>(WordListUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private var ttsManager: TtsManager? = null

    // Internal state for pagination
    private var currentPage = 1

    companion object {
        private const val PAGE_SIZE = 20
        private const val TAG = "WordListViewModel"
    }

    init {
        // This flow handles search-triggered reloads
        viewModelScope.launch {
            snapshotFlow { searchQueryState.text.toString() }
                .debounce(300L)
                .distinctUntilChanged()
                .collect { query ->
                    currentPage = 1 // Reset page for new search
                    val currentSortOrder = (uiState.value as? WordListUiState.Success)?.sortOrder ?: SortOrder.MODIFICATION_TIME
                    fetchData(query, currentPage, currentSortOrder)
                }
        }

        // Initial data fetch
        fetchData("", 1, SortOrder.MODIFICATION_TIME)

        ttsManager = TtsManager(getApplication()) { /* TTS ready */ }
    }

    private fun fetchData(query: String, page: Int, sortOrder: SortOrder) {
        viewModelScope.launch {
            val currentWords = if (page > 1) (uiState.value as? WordListUiState.Success)?.words ?: emptyList() else emptyList()
            if (page == 1) {
                _uiState.value = WordListUiState.Loading
            } else {
                (_uiState.value as? WordListUiState.Success)?.let {
                    _uiState.value = it.copy(isLoadingMore = true)
                }
            }

            try {
                // Assuming repository can handle sortOrder
                wordRepository.getWordsForNotebook(notebookId, query, PAGE_SIZE, (page - 1) * PAGE_SIZE, sortOrder)
                    .collect { newWords ->
                        val endOfListReached = newWords.size < PAGE_SIZE
                        val combinedWords = if (page == 1) newWords else currentWords + newWords
                        _uiState.value = WordListUiState.Success(
                            notebookId = notebookId,
                            notebookName = notebookName,
                            words = combinedWords,
                            searchQuery = searchQueryState,
                            isLoadingMore = false,
                            endOfListReached = endOfListReached,
                            sortOrder = sortOrder
                        )
                        currentPage = page
                    }
            } catch (e: Exception) {
                _uiState.value = WordListUiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun speak(word:String){
        ttsManager?.speak(word)
    }

    override fun onCleared() {
        ttsManager?.shutdown()
        super.onCleared()
    }

    fun loadMoreWords() {
        val currentState = uiState.value
        if (currentState is WordListUiState.Success && !currentState.isLoadingMore && !currentState.endOfListReached) {
            fetchData(currentState.searchQuery.text.toString(), currentPage + 1, currentState.sortOrder)
        }
    }

    fun onSortOrderChanged(sortOrder: SortOrder) {
        val currentState = uiState.value
        if (currentState is WordListUiState.Success) {
            // Directly trigger a refetch with the new sort order
            currentPage = 1
            fetchData(currentState.searchQuery.text.toString(), currentPage, sortOrder)
        }
    }

    fun deleteWord(word: Word) {
        viewModelScope.launch {
            wordRepository.deleteWord(word)
            // Optimistically update UI
            val currentState = uiState.value
            if (currentState is WordListUiState.Success) {
                val updatedWords = currentState.words.filter { it.id != word.id }
                _uiState.value = currentState.copy(words = updatedWords)
            }
        }
    }

    fun toWordAdd() {
        nav.navigate("${Screen.AddWord.route}/$notebookId")
    }

    fun onMastered(word: Word) {
        val repetitions = if (word.repetitions != 5) 5 else 0
        val nextReviewDate = if (word.repetitions != 5) word.nextReviewDate else Date()
        val updatedWord = word.copy(repetitions = repetitions, nextReviewDate = nextReviewDate)

        viewModelScope.launch {
            wordRepository.updateWord(updatedWord)
            // Optimistically update UI
            val currentState = uiState.value
            if (currentState is WordListUiState.Success) {
                val newWordsList = currentState.words.map { if (it.id == updatedWord.id) updatedWord else it }
                _uiState.value = currentState.copy(words = newWordsList)
            }
        }
    }

    fun toWordDetail(word: String){
        nav.navigate("${Screen.WordDetail.route}/$word")
    }
}
