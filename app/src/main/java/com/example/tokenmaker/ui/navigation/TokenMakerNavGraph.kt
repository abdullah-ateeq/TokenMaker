package com.example.tokenmaker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tokenmaker.data.preferences.UserPreferencesRepository
import com.example.tokenmaker.ui.about.AboutScreen
import com.example.tokenmaker.ui.home.HomeScreen
import com.example.tokenmaker.ui.home.HomeViewModel
import com.example.tokenmaker.ui.settings.SettingsScreen
import com.example.tokenmaker.ui.settings.SettingsViewModel
import com.example.tokenmaker.ui.splash.SplashScreen

@Composable
fun TokenMakerNavGraph(
    preferencesRepository: UserPreferencesRepository,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.provideFactory(preferencesRepository)
            )
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) }
            )
        }

        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.provideFactory(preferencesRepository)
            )
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
