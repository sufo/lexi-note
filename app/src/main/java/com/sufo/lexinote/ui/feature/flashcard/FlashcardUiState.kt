package com.sufo.lexinote.ui.feature.flashcard

import com.sufo.lexinote.data.local.db.entity.Word

enum class ReviewMode {
    FLASHCARD,
    SPELLING,
    MULTIPLE_CHOICE
}

enum class SpellingState {
    INITIAL,      // Initial state, user is trying to spell
    INCORRECT,    // User made a mistake, showing the correct answer
}

/**
 * Represents all UI state for the Flashcard screen.
 */
data class FlashcardUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    val words: List<Word> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val isFinished: Boolean = false,
    val isTtsReady: Boolean = false,
    val currentMode: ReviewMode = ReviewMode.FLASHCARD,

    // State for Spelling mode
    val spellingInput: String = "",
    val spellingState: SpellingState = SpellingState.INITIAL,
    val userIncorrectSpelling: String? = null, // To show the user what they typed wrong
    val isSpellingCorrect: Boolean = false,

    // State for Multiple Choice mode
    val multipleChoiceOptions: List<String> = emptyList(),
    val selectedOption: String? = null,
    val isAnswerChecked: Boolean = false
) {
    /**
     * A computed property to easily access the current word being displayed.
     */
    val currentWord: Word?
        get() = words.getOrNull(currentIndex)

    /**
     * A computed property for the review progress, e.g., 0.75f for 75%.
     */
    val progress: Float
        get() = if (words.isEmpty()) 0f else (currentIndex).toFloat() / words.size

    /**
     * A computed property for the progress text, e.g., "5 / 20".
     */
    val progressText: String
        get() = "${currentIndex} / ${words.size}"

    val backgroundImage:String?
        get() = words.getOrNull(currentIndex)?.let {
            it.imgs?.split(",")?.shuffled()?.take(1)?.first()
        }
}
