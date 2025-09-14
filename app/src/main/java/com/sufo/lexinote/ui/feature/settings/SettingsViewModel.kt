package com.sufo.lexinote.ui.feature.settings

import android.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sufo.lexinote.data.preferences.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: String = "SYSTEM", // Possible values: "LIGHT", "DARK", "SYSTEM"
    val notificationEnable: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Collect the theme mode flow from DataStore and update the UI state
        viewModelScope.launch {
            settingsDataStore.userPreferencesFlow.collect { (themeMode,notificationsEnabled) ->
                _uiState.update { it.copy(themeMode,notificationsEnabled) }
            }
        }
    }

    /**
     * Called when the user selects a new theme mode.
     * This will save the new mode to DataStore, which will then automatically
     * update the uiState via the collector in the init block.
     */
    fun onThemeModeChanged(themeMode: String) {
        viewModelScope.launch {
            settingsDataStore.saveThemeMode(themeMode)
        }
    }
    fun onNotifyChanged(enable: Boolean){
        viewModelScope.launch {
            settingsDataStore.saveNotificationsEnabled(enable)
        }
    }
}
