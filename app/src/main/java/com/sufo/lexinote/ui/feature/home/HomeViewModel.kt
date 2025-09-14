package com.sufo.lexinote.ui.feature.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sufo.lexinote.data.local.db.entity.Notebook
import com.sufo.lexinote.data.repo.NotebookRepository
import com.sufo.lexinote.data.repo.NotebookWithStats
import com.sufo.lexinote.ui.base.BaseViewModel
import com.sufo.lexinote.ui.navigation.NavigationService
import com.sufo.lexinote.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: NotebookRepository,
    private val application: Application,
    private val nav: NavigationService,
) : BaseViewModel(application,nav) {

    val notebooksWithStats: Flow<List<NotebookWithStats>> = repository.getNotebooksWithStats()

    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> = _showBottomSheet.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    private val _showAddEditDialog = MutableStateFlow(false)
    val showAddEditDialog: StateFlow<Boolean> = _showAddEditDialog.asStateFlow()

    private val _selectedNotebook = MutableStateFlow<Notebook?>(null)
    val selectedNotebook: StateFlow<Notebook?> = _selectedNotebook.asStateFlow()

    fun onShowBottomSheet(notebook: Notebook) {
        _selectedNotebook.value = notebook
        _showBottomSheet.value = true
    }

    fun onHideBottomSheet() {
        _showBottomSheet.value = false
    }

    fun onShowDeleteDialog() {
        _showDeleteDialog.value = true
        _showBottomSheet.value = false // Close bottom sheet when dialog opens
    }

    fun onHideDeleteDialog() {
        _showDeleteDialog.value = false
    }

    fun onShowAddEditDialog(notebook: Notebook? = null) {
        _selectedNotebook.value = notebook
        _showAddEditDialog.value = true
        _showBottomSheet.value = false // Hide bottom sheet as well
    }

    fun onHideAddEditDialog() {
        _showAddEditDialog.value = false
        _selectedNotebook.value = null // Clear selection when dialog is hidden
    }

    // --- CUD Operations --- //

    fun notebooks() {
        viewModelScope.launch {
            repository.getAllNotebooks()
        }
    }

    fun createNotebook(name: String, iconResId: Int, description: String?) {
        viewModelScope.launch {
            val iconResName = application.resources.getResourceEntryName(iconResId)
            val newNotebook = Notebook(
                name = name,
                iconResName = iconResName,
                description = description
            )
            repository.createNotebook(newNotebook)
            onHideAddEditDialog()
        }
    }

    fun updateNotebook(newName: String, newIconResId: Int, newDescription: String?) {
        viewModelScope.launch {
            _selectedNotebook.value?.let { notebook ->
                val newIconResName = application.resources.getResourceEntryName(newIconResId)
                val updatedNotebook = notebook.copy(
                    name = newName,
                    iconResName = newIconResName,
                    description = newDescription
                )
                repository.updateNotebook(updatedNotebook)
            }
            onHideAddEditDialog()
        }
    }

    fun deleteSelectedNotebook() {
        viewModelScope.launch {
            _selectedNotebook.value?.let { notebook ->
                repository.deleteNotebook(notebook)
            }
            onHideDeleteDialog()
        }
    }
    fun onStartReviewNotebookClicked(notebookId: Int) {
        nav.navigate(Screen.FlashcardView.route+"?notebookId=$notebookId",)
    }

    fun toWordList(notebookId: Int, name:String) {
        nav.navigate("${Screen.WordList.route}/${notebookId}/${name}")
    }

}