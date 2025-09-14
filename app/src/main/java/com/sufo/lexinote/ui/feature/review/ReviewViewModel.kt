package com.sufo.lexinote.ui.feature.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sufo.lexinote.data.local.db.entity.Notebook
import com.sufo.lexinote.data.repo.NotebookRepository
import com.sufo.lexinote.data.repo.ReviewRepository
import com.sufo.lexinote.ui.navigation.NavigationService
import com.sufo.lexinote.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotebookWithReviewCount(
    val notebook: Notebook,
    val reviewCount: Int
)

data class ReviewHubUiState(
    val isLoading: Boolean = true,
    val totalReviewCount: Int = 0,
    val notebooks: List<NotebookWithReviewCount> = emptyList()
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val navigationService: NavigationService,
    private val reviewRepository: ReviewRepository,
    private val notebookRepository: NotebookRepository // Assuming this repository exists
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewHubUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadReviewData()
    }

    private fun loadReviewData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val totalCount = reviewRepository.getDueWordsCount()

            notebookRepository.getAllNotebooks().collect { notebookList ->
                val notebooksWithCounts = notebookList.map { notebook ->
                    val count = reviewRepository.getDueWordsCountForNotebook(notebook.id!!)
                    NotebookWithReviewCount(notebook = notebook, reviewCount = count)
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        totalReviewCount = totalCount,
                        notebooks = notebooksWithCounts
                    )
                }
            }
        }
    }

    fun onStartReviewAllClicked() {
        // Navigate to FlashcardScreen with a special ID (e.g., -1) for all notebooks
        navigationService.navigate(Screen.FlashcardView.route)
    }

    fun onStartReviewNotebookClicked(notebookId: Int) {
        navigationService.navigate(Screen.FlashcardView.route+"?notebookId=$notebookId",)
    }
}
