package com.sufo.lexinote.ui.feature.home

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.sufo.lexinote.data.local.db.entity.Notebook
import com.sufo.lexinote.data.repo.NotebookRepository
import com.sufo.lexinote.ui.base.BaseViewModel
import com.sufo.lexinote.ui.navigation.NavigationService
import com.sufo.lexinote.utils.Serialization
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddNotebookUiState(
    val notebook: Notebook = Notebook(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEditMode: Boolean = false
)

@HiltViewModel
class AddNotebookViewmodel @Inject constructor(
    private val repository: NotebookRepository,
    private val application: Application,
    nav: NavigationService,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel(application, nav) {

    private val _uiState = MutableStateFlow(AddNotebookUiState())
    val uiState = _uiState.asStateFlow()

    init {
        savedStateHandle.get<String>("notebook")?.let { notebookString ->
            Serialization.decodeFromString(Notebook.serializer(), notebookString)?.let { notebook ->
                _uiState.update {
                    it.copy(
                        notebook = notebook,
                        isEditMode = true
                    )
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(notebook = it.notebook.copy(name = name)) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(notebook = it.notebook.copy(description = description)) }
    }

    fun onIconChange(iconResName: String) {
        _uiState.update { it.copy(notebook = it.notebook.copy(iconResName = iconResName)) }
    }

    fun createOrUpdateNotebook() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val currentNotebook = _uiState.value.notebook
                if (_uiState.value.isEditMode) {
                    repository.updateNotebook(currentNotebook)
                } else {
                    repository.createNotebook(currentNotebook)
                }
                popBackStack()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
