package com.sufo.lexinote.ui.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.sufo.lexinote.ui.navigation.NavigationService


abstract class BaseViewModel(application: Application, private val navigationService: NavigationService) : AndroidViewModel(application) {

    /**
     * Navigates back to the previous screen.
     */
    fun popBackStack() {
        navigationService.popBackStack()
    }
}