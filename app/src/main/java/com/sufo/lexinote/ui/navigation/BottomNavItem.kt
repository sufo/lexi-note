package com.sufo.lexinote.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.sufo.lexinote.R

sealed class BottomNavItem(
    val route: String,
    @StringRes val titleResId: Int,
    val icon: ImageVector
) {
    object Home : BottomNavItem(
        route = Screen.Home.route,
        titleResId = R.string.notebook,
        icon = Icons.Default.Home
    )
    object Review : BottomNavItem(
        route = Screen.ReviewHub.route,
        titleResId = R.string.review,
        icon = Icons.Default.Checklist
    )
    object Explore : BottomNavItem(
        route = Screen.Explore.route,
        titleResId = R.string.explore,
        icon = Icons.Default.Explore
    )
    object Me : BottomNavItem(
        route = Screen.Me.route,
        titleResId = R.string.me,
        icon = Icons.Default.Person
    )
}