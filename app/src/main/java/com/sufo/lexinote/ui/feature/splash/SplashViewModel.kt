package com.sufo.lexinote.ui.feature.splash

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sufo.lexinote.data.local.db.DictDatabaseHelper
import com.sufo.lexinote.ui.navigation.NavigationCommand
import com.sufo.lexinote.ui.navigation.NavigationService
import com.sufo.lexinote.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val application: Application,
    private val dbHelper: DictDatabaseHelper,
    nav: NavigationService
) : ViewModel() {

    init {
        viewModelScope.launch {
            // Simulate a minimum splash time so it doesn't just flash on screen

            val prefs = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val isDbInitialized = prefs.getBoolean("db_initialized", false)

            if (!isDbInitialized) {
                try {
//                    val dbHelper = DictDatabaseHelper(application)
                    dbHelper.createDatabaseIfNotExists()
                    prefs.edit().putBoolean("db_initialized", true).apply()
                } catch (e: Exception) {
                    // Handle error, maybe show a dialog or log it
                    // For now, we'll just log and proceed
                    e.printStackTrace()
                }
            }
            
            // Navigate to home screen after initialization or if already initialized
            nav.navigateToAndClearStack(Screen.Home.route)
        }
    }
}
