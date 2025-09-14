package com.sufo.lexinote.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.sufo.lexinote.ui.feature.dictionary.SearchScreen
import com.sufo.lexinote.ui.feature.dictionary.WordDetailScreen
import com.sufo.lexinote.ui.feature.word.AddWordScreen
import com.sufo.lexinote.ui.feature.explore.ExploreScreen
import com.sufo.lexinote.ui.feature.flashcard.FlashcardScreen
import com.sufo.lexinote.ui.feature.home.HomeScreen
import com.sufo.lexinote.ui.feature.me.MeScreen
import com.sufo.lexinote.ui.feature.word.WordListScreen
import com.sufo.lexinote.ui.feature.splash.SplashScreen
import androidx.compose.runtime.getValue
import com.sufo.lexinote.ui.feature.home.AddNotebookScreen
import com.sufo.lexinote.ui.feature.review.ReviewScreen
import com.sufo.lexinote.ui.feature.settings.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    navigationService: NavigationService,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val canPop = navController.previousBackStackEntry != null

    BackHandler(enabled = canPop) {
        navigationService.popBackStack()
    }
    NavHost(
        navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) { SplashScreen(navigationService) }
        composable(Screen.Home.route) { HomeScreen(navigationService) }
        composable(Screen.ReviewHub.route) { ReviewScreen(navigationService) }
        composable(Screen.Explore.route) { ExploreScreen(navigationService) }
        composable(Screen.Me.route) { MeScreen(navigationService) }
        composable(Screen.ThemeSettings.route) { SettingsScreen(navigationService) }
        //带参
        composable(Screen.AddNotebook.route) { AddNotebookScreen(navigationService) }
        //带参
        composable(Screen.AddNotebook.route+"/{notebook}",
            arguments = listOf(navArgument("notebook") { type = NavType.StringType })
        ) { AddNotebookScreen(navigationService) }
        composable(Screen.FlashcardView.route+"?notebookId={notebookId}",
            arguments = listOf(
                navArgument("notebookId") { type = NavType.StringType
                    nullable = true //关键！将其标记为可空，表示这个参数是可选的
                    defaultValue = null
                }
            )
        ) { FlashcardScreen() }
//        composable(Screen.ReviewSummary.route) { ReviewSummaryScreen() }
        composable(
            route = Screen.WordList.route + "/{notebookId}/{notebookName}",
            arguments = listOf(navArgument("notebookId") { type = NavType.IntType },
                navArgument("notebookName") { type = NavType.StringType }
            )
        ) { 
            WordListScreen(nav = navigationService)
        }
        composable(
            route = Screen.AddWord.route + "/{notebookId}?word={word}",  //?word=xx
            arguments = listOf(navArgument("notebookId") { type = NavType.IntType },
                navArgument("word") { type = NavType.StringType
                    nullable = true //关键！将其标记为可空，表示这个参数是可选的
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
//            val notebookId = backStackEntry.arguments?.getInt("notebookId") ?: 0
            AddWordScreen(nav = navigationService)
        }
        composable(route = Screen.SearchWord.route) { SearchScreen() }
        composable(
            route = Screen.WordDetail.route + "/{word}",
            arguments = listOf(navArgument("word") { type = NavType.StringType })
        ) { 
            WordDetailScreen(nav = navigationService)
        }
    }
}