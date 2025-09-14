package com.sufo.lexinote.ui.navigation

import java.util.UUID

/**
 * Represents a unique, one-time navigation event.
 * Using a sealed class ensures that even identical consecutive commands (like two back presses)
 * are treated as distinct events, solving the StateFlow issue of not emitting identical consecutive values.
 */
sealed class NavigationCommand {
    /**
     * Command to navigate to a specific destination route.
     * @param route The destination route string.
     */
    data class NavigateTo(val route: String) : NavigationCommand()

    /**
     * Command to pop the back stack.
     * By being a data class with a unique ID, each instance is treated as a new, distinct event.
     */
    data class PopBackStack(val id: String = UUID.randomUUID().toString()) : NavigationCommand()

    /**
     * Command to navigate to a destination and clear the entire back stack.
     * Useful for navigating to a home or login screen where back navigation is not desired.
     * @param route The destination route string.
     */
    data class NavigateToAndClearBackStack(val route: String) : NavigationCommand()
}
