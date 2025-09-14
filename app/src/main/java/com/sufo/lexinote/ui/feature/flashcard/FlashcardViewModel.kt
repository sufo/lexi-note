// Copyright (c) 2025 sufo
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.sufo.lexinote.ui.feature.flashcard

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.sufo.lexinote.data.local.db.entity.ReviewLog
import com.sufo.lexinote.data.local.db.entity.Word
import com.sufo.lexinote.data.repo.ReviewLogRepository
import com.sufo.lexinote.data.repo.ReviewRepository
import com.sufo.lexinote.domain.srs.SpacedRepetitionScheduler
import com.sufo.lexinote.ui.base.BaseViewModel
import com.sufo.lexinote.ui.navigation.NavigationService
import com.sufo.lexinote.utils.TtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

const val NOTEBOOK_ID_ARG = "notebookId"

@HiltViewModel
class FlashcardViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val reviewLogRepository: ReviewLogRepository,
    private val navigationService: NavigationService,
    savedStateHandle: SavedStateHandle,
    nav: NavigationService,
    application: Application,
) : BaseViewModel(application, nav) {

    private val notebookId: Int = savedStateHandle.get<String>(NOTEBOOK_ID_ARG)?.toIntOrNull() ?: -1

    private val _uiState = MutableStateFlow(FlashcardUiState())
    val uiState = _uiState.asStateFlow()
    private var ttsManager: TtsManager? = null

    init {
        ttsManager = TtsManager(getApplication()){ isSuccess ->
            _uiState.update { it.copy(isTtsReady = isSuccess) }
        }
        loadDueWords()
    }

    private fun determineNextMode(): ReviewMode {
        // Simple logic for now: 33/33/33 chance for each mode
        return when ((0..2).random()) {
            0 -> ReviewMode.FLASHCARD
            1 -> ReviewMode.SPELLING
            else -> ReviewMode.MULTIPLE_CHOICE
        }
    }

    private fun loadDueWords() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val words = if (notebookId == -1) {
                reviewRepository.getDueWords()
            } else {
                reviewRepository.getDueWordsForNotebook(notebookId)
            }

            _uiState.update { 
                it.copy(
                    isLoading = false,
                    words = words,
                    isFinished = words.isEmpty()
                )
            }
            // Prepare options for the first card if it's multiple choice
            prepareOptionsForCurrentWord()
        }
    }


    fun speak(text: String) {
        if (_uiState.value.isTtsReady) {
            ttsManager?.speak(text)
        }
    }

    override fun onCleared() {
        ttsManager?.shutdown()
        super.onCleared()
    }

    fun onFlipCard() {
        _uiState.update { it.copy(isFlipped = !it.isFlipped) }
    }

    fun processReview(quality: Int) {
        val currentState = _uiState.value
        val currentWord = currentState.currentWord ?: return

        val updatedWord = SpacedRepetitionScheduler.schedule(currentWord, quality)

        viewModelScope.launch {
            reviewRepository.updateWord(updatedWord)
            // Log the review event
            reviewLogRepository.addReviewLog(
                ReviewLog(
                    wordId = currentWord.id,
                    reviewedAt = Date()
                )
            )
            
            val nextIndex = currentState.currentIndex + 1
            if (nextIndex >= currentState.words.size) {
                _uiState.update { it.copy(isFinished = true) }
            } else {
                _uiState.update {
                    it.copy(
                        currentIndex = nextIndex,
                        isFlipped = false,
                        // Reset spelling state for the new word
                        spellingInput = "",
                        spellingState = SpellingState.INITIAL,
                        userIncorrectSpelling = null,
                        // Reset multiple choice state for the new word
                        multipleChoiceOptions = emptyList(),
                        selectedOption = null,
                        isAnswerChecked = false,
                        currentMode = determineNextMode() // Determine mode for the next card
                    )
                }
                // Prepare options for the next card if it's multiple choice
                prepareOptionsForCurrentWord(nextIndex)
            }
        }
    }

    private fun prepareOptionsForCurrentWord(wordIndex: Int = _uiState.value.currentIndex) {
        val state = _uiState.value
        if (wordIndex < state.words.size && state.currentMode == ReviewMode.MULTIPLE_CHOICE) {
            val currentWord = state.words[wordIndex]
            val correctTranslation = currentWord.translation ?: ""
            val distractors = state.words
                .filter { it.id != currentWord.id && it.translation != null }
                .map { it.translation!! }
                .shuffled()
                .take(3)
            
            val options = (distractors + correctTranslation).shuffled()
            _uiState.update { it.copy(multipleChoiceOptions = options) }
        }
    }

    fun onOptionSelected(option: String) {
        val currentState = uiState.value
        val currentWord = currentState.currentWord ?: return
        val isCorrect = option == currentWord.translation

        // Always update the state first to show the user's selection
        _uiState.update { it.copy(selectedOption = option, isAnswerChecked = true) }

        if (isCorrect) {
            viewModelScope.launch {
                kotlinx.coroutines.delay(600L) // 1-second delay
                processReview(5)
            }
        }
        // If incorrect, the user will have to press the "Next" button, so no further action is needed here.
    }

    fun onNextAfterMultipleChoice() {
        val currentState = _uiState.value
        val currentWord = currentState.currentWord ?: return

        val quality = when {
            currentState.selectedOption == null -> 0 // Skipped
            currentState.selectedOption == currentWord.translation -> 5 // Correct
            else -> 1 // Incorrect
        }
        processReview(quality)
    }

    fun onSpellingInputChanged(input: String) {
        val isCorrect = input.equals(uiState.value.currentWord?.word, ignoreCase = true)
        _uiState.update { it.copy(spellingInput = input, isSpellingCorrect = isCorrect) }
    }

    fun onCheckSpelling() {
        val currentState = _uiState.value
        val currentWord = currentState.currentWord ?: return
        val isCorrect = currentState.spellingInput.equals(currentWord.word, ignoreCase = true)

        if (isCorrect) {
            // If correct, score is based on whether they made a previous mistake on this card
            val quality = if (currentState.spellingState == SpellingState.INITIAL) 5 else 3 // 5 for first-try, 3 for corrected
            processReview(quality)
        } else {
            // If incorrect, enter the INCORRECT state to show the answer
            _uiState.update {
                it.copy(
                    spellingState = SpellingState.INCORRECT,
                    userIncorrectSpelling = it.spellingInput, // Store the wrong answer
                    spellingInput = "" // Clear the input field
                )
            }
        }
    }

    fun onSpellingGiveUp() {
        // User gives up, show the answer and count as lowest score
        _uiState.update {
            it.copy(
                spellingState = SpellingState.INCORRECT,
                userIncorrectSpelling = it.spellingInput.ifBlank { "(no answer)" } // Store something to show
            )
        }
    }
    
    fun onSpellingNextAfterCorrection() {
        val currentState = _uiState.value
        val currentWord = currentState.currentWord ?: return
        val isCorrect = currentState.spellingInput.equals(currentWord.word, ignoreCase = true)
        
        val quality = if(isCorrect) 3 else 1 // Corrected = Vague, Still incorrect = Forgot
        processReview(quality)
    }

    fun onCloseClicked() {
        navigationService.popBackStack()
    }
}