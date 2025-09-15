package com.sufo.lexinote.ui.feature.dictionary

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.sufo.lexinote.data.local.db.entity.DictWord
import com.sufo.lexinote.data.local.db.entity.Notebook
import com.sufo.lexinote.data.local.db.entity.Word
import com.sufo.lexinote.data.repo.DictionaryRepository
import com.sufo.lexinote.data.repo.NotebookRepository
import com.sufo.lexinote.data.repo.WordRepository
import com.sufo.lexinote.ui.base.BaseViewModel
import com.sufo.lexinote.ui.navigation.NavigationService
import com.sufo.lexinote.ui.navigation.Screen
import com.sufo.lexinote.utils.TtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class PreviewImageState(
    val images: List<String>,
    val initialIndex: Int
)

data class WordDetailUiState(
    val word: String,
    val dictWord: DictWord? = null,
    val savedWord: Word? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isTtsReady: Boolean = false,
    val notebooks: List<Notebook> = emptyList(),
    val showNotebookSheet: Boolean = false,
    // Audio Playback State
    val playingAudioPath: String? = null,
    val isAudioPlaying: Boolean = false,
    // Image Preview State
    val previewImageState: PreviewImageState? = null
)

@HiltViewModel
class WordDetailViewModel @Inject constructor(
    application: Application,
    savedStateHandle: SavedStateHandle,
    private val dictionaryRepository: DictionaryRepository,
    private val wordRepository: WordRepository,
    private val notebookRepository: NotebookRepository,
//    private val appApi: AppApi,
    private val nav: NavigationService
) : BaseViewModel(application, nav) {

    private val _word: String = checkNotNull(savedStateHandle["word"])
    private val _uiState = MutableStateFlow(WordDetailUiState(word = _word))
    val uiState: StateFlow<WordDetailUiState> = _uiState.asStateFlow()

    private var ttsManager: TtsManager? = null
    private var mediaPlayer: MediaPlayer? = null

    init {
        fetchWordDetails()
        observeSavedWord()
        fetchNotebooks()

        ttsManager = TtsManager(getApplication()) { isSuccess ->
            _uiState.update { it.copy(isTtsReady = isSuccess) }
        }
    }

    // --- Data Fetching ---
    private fun fetchWordDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val localResult = dictionaryRepository.findWordLocally(_word)
            if (localResult != null) {
                _uiState.update { it.copy(isLoading = false, dictWord = localResult) }
                dictionaryRepository.addSearchToHistory(_word, localResult.translation)
            } else {
//                when (val networkResult = appApi.searchWord(_word)) {
//                    is ResultState.Success -> {
//                        val wordEntry = networkResult.data
//                        _uiState.update { it.copy(isLoading = false, dictWord = wordEntry) }
//                        dictionaryRepository.addSearchToHistory(_word, wordEntry?.translation)
//                    }
//                    else -> {
//                        val errorMessage = when(networkResult) {
//                            is ResultState.Empty -> "Word not found online."
//                            is ResultState.Error -> "Error: ${networkResult.message}"
//                            is ResultState.Exception -> "Network Error: ${networkResult.throwable.message}"
//                            else -> "An unknown error occurred."
//                        }
//                        _uiState.update { it.copy(isLoading = false, error = errorMessage) }
//                    }
//                }
            }
        }
    }

    private fun observeSavedWord() {
        viewModelScope.launch {
            wordRepository.getSavedWord(_word).collect { savedWord ->
                _uiState.update { it.copy(savedWord = savedWord) }
            }
        }
    }

    private fun fetchNotebooks() {
        viewModelScope.launch {
            notebookRepository.getAllNotebooks().collect { notebooks ->
                _uiState.update { it.copy(notebooks = notebooks) }
            }
        }
    }

    // --- User Actions ---
    fun onAddToNotebookClicked() {
        _uiState.update { it.copy(showNotebookSheet = true) }
    }

    fun onDismissNotebookSheet() {
        _uiState.update { it.copy(showNotebookSheet = false) }
    }

    fun onNotebookSelected(notebook: Notebook) {
        viewModelScope.launch {
            val dictWord = _uiState.value.dictWord ?: return@launch
            val newWord = Word(
                notebookId = notebook.id!!,
                word = dictWord.word,
                phonetic = dictWord.phonetic ?: "",
                translation = dictWord.translation ?: "",
                example = dictWord.examples.firstOrNull() ?: "",
                nextReviewDate = Date()
            )
            wordRepository.addWord(newWord)
            onDismissNotebookSheet()
        }
    }

    fun onEditWordClicked() {
        val savedWord = _uiState.value.savedWord ?: return
        val notebookName=uiState.value
        nav.navigate("${Screen.AddWord.route}/${savedWord.notebookId}?word=${savedWord.word}")
    }

    // --- TTS & Media Playback ---
    fun speak(text: String) {
        if (_uiState.value.isTtsReady) {
            ttsManager?.speak(text)
        }
    }

    fun playOrPauseAudio(path: String) {
        val currentState = _uiState.value
        if (currentState.playingAudioPath == path && currentState.isAudioPlaying) {
            // Pause current audio
            mediaPlayer?.pause()
            _uiState.update { it.copy(isAudioPlaying = false) }
        } else if (currentState.playingAudioPath == path && !currentState.isAudioPlaying) {
            // Resume current audio
            mediaPlayer?.start()
            _uiState.update { it.copy(isAudioPlaying = true) }
        } else {
            // Stop previous and play new audio
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                try {
                    setDataSource(path)
                    prepare()
                    start()
                    setOnCompletionListener {
                        _uiState.update { it.copy(playingAudioPath = null, isAudioPlaying = false) }
                    }
                    _uiState.update { it.copy(playingAudioPath = path, isAudioPlaying = true) }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _uiState.update { it.copy(playingAudioPath = null, isAudioPlaying = false) }
                }
            }
        }
    }

    // --- Image Preview ---
    fun onImageClicked(path: String) {
        val images = _uiState.value.savedWord?.imgs?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        val index = images.indexOf(path)
        if (index != -1) {
            _uiState.update { it.copy(previewImageState = PreviewImageState(images, index)) }
        }
    }

    fun onDismissImagePreview() {
        _uiState.update { it.copy(previewImageState = null) }
    }

    override fun onCleared() {
        ttsManager?.shutdown()
        mediaPlayer?.release()
        mediaPlayer = null
        super.onCleared()
    }
}
