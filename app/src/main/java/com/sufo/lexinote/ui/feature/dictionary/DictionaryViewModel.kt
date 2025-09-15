package com.sufo.lexinote.ui.feature.dictionary

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.sufo.lexinote.data.local.db.entity.DictWord
import com.sufo.lexinote.data.repo.DictionaryRepository
import com.sufo.lexinote.data.repo.WordRepository
import com.sufo.lexinote.ui.base.BaseViewModel
import com.sufo.lexinote.ui.navigation.NavigationService
import com.sufo.lexinote.utils.TtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DictionaryScreenState(
    val word: String,
    val searchResult: DictWord? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isTtsReady: Boolean = false,
    val isWordSaved: Boolean = false
)

@HiltViewModel
class DictionaryViewModel @Inject constructor(
    application: Application,
    savedStateHandle: SavedStateHandle,
    private val dictionaryRepository: DictionaryRepository,
    private val wordRepository: WordRepository,
//    private val appApi: AppApi,
    navigationService: NavigationService
) : BaseViewModel(application, navigationService) {

    val _word: String = savedStateHandle["word"]!!
    private val _uiState = MutableStateFlow(DictionaryScreenState(_word))
    val uiState: StateFlow<DictionaryScreenState> = _uiState.asStateFlow()

    private var ttsManager: TtsManager? = null

    init {
        searchWord()
        ttsManager = TtsManager(getApplication()) { isSuccess ->
            _uiState.update { it.copy(isTtsReady = isSuccess) }
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

    fun searchWord() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, searchResult = null, error = null) }

            val localResult = dictionaryRepository.findWordLocally(uiState.value.word)

            if (localResult != null) {
                _uiState.update { it.copy(isLoading = false, searchResult = localResult) }
                dictionaryRepository.addSearchToHistory(uiState.value.word,localResult.translation)
            } else {
                //网络获取
//                when (val networkResult = appApi.searchWord(uiState.value.word)) {
//                    is ResultState.Success -> {
//                        val wordEntry = networkResult.data
//                        _uiState.update { it.copy(isLoading = false, searchResult = wordEntry) }
//                        dictionaryRepository.addSearchToHistory(uiState.value.word,wordEntry?.translation)
//                    }
//                    is ResultState.Empty -> {
//                        _uiState.update { it.copy(isLoading = false, error = "Word not found online.") }
//                    }
//                    is ResultState.Error -> {
//                        _uiState.update { it.copy(isLoading = false, error = "Error: ${networkResult.message}") }
//                    }
//                    is ResultState.Exception -> {
//                        _uiState.update { it.copy(isLoading = false, error = "Network Error: ${networkResult.throwable.message}") }
//                    }
//                }
            }
        }
    }

}



