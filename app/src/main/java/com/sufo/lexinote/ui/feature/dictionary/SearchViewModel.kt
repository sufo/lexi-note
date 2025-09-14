package com.sufo.lexinote.ui.feature.dictionary

import android.app.Application
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import com.sufo.lexinote.data.local.db.entity.DictWord
import com.sufo.lexinote.data.local.db.entity.SearchHistory
import com.sufo.lexinote.data.remote.api.AppApi
import com.sufo.lexinote.data.repo.DictionaryRepository
import com.sufo.lexinote.ui.base.BaseViewModel
import com.sufo.lexinote.ui.navigation.NavigationService
import com.sufo.lexinote.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchScreenState(
    val suggestions: List<DictWord> = emptyList(),
    val history: List<SearchHistory> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    application: Application,
    private val dictionaryRepository: DictionaryRepository,
    private val appApi: AppApi,
    private val nav: NavigationService
) : BaseViewModel(application,nav) {

    val queryState = TextFieldState()

    private val _uiState = MutableStateFlow(SearchScreenState())
    val uiState: StateFlow<SearchScreenState> = _uiState.asStateFlow()

    init {
        // Load initial search history
        viewModelScope.launch {
            dictionaryRepository.getSearchHistory().collect { historyList ->
                _uiState.update { it.copy(history = historyList) }
            }
        }

        // Reactively listen to text field changes for suggestions
        viewModelScope.launch {
            snapshotFlow { queryState.text }
                .debounce(300)
                .distinctUntilChanged()  // 去重复
                .collect { query ->
                    val queryString = query.toString()
                    if (queryString.isNotBlank()) {
                        val suggestions = dictionaryRepository.findWordsStartingWith(queryString)
                        _uiState.update { it.copy(suggestions = suggestions, error = null) }
                    } else {
                        _uiState.update { it.copy(suggestions = emptyList()) }
                    }
                }
        }
    }

    fun clearQuery() {
        queryState.edit { this.replace(0, this.length, "") }
        _uiState.update { it.copy(suggestions = emptyList(), error = null) }
    }

    fun clearHistory() {
        viewModelScope.launch {
            dictionaryRepository.clearSearchHistory()
        }
    }

    fun toWordDetail(word:String){
        nav.navigate("${Screen.WordDetail.route}/${word}")
    }
}



