package com.sufo.lexinote.ui.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class NavigationService {
    private val _command = MutableSharedFlow<NavigationCommand>(replay = 1, extraBufferCapacity = 1)
    val command = _command.asSharedFlow()

    fun navigate(destination: String) {
        _command.tryEmit(NavigationCommand.NavigateTo(destination))
    }

    fun popBackStack() {
        _command.tryEmit(NavigationCommand.PopBackStack())
    }

    fun toLogin () {
        _command.tryEmit(NavigationCommand.NavigateToAndClearBackStack("login"))
    }

    fun navigateToAndClearStack(route: String) {
        _command.tryEmit(NavigationCommand.NavigateToAndClearBackStack(route))
    }
}
