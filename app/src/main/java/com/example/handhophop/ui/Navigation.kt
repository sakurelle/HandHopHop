package com.example.handhophop.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object ShemeScreen : Screen("sheme_screen")
    object Statistics : Screen("statistics")
    object OnlineSchemes : Screen("online_schemes")
    object Profile : Screen("profile")
    object ProfilePhoto : Screen("profile_photo")
}

@Composable
fun HandHophopNavigation() {
    val navController = rememberNavController()

    // ✅ shared VM для выбранной схемы
    val selectedVm: SelectedSchemeViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController, selectedVm = selectedVm)
        }
        composable(Screen.OnlineSchemes.route) {
            OnlineSchemesScreen(navController = navController, selectedVm = selectedVm)
        }
        composable(Screen.ShemeScreen.route) {
            ShemeScreen(navController = navController, selectedVm = selectedVm)
        }
        composable(Screen.Statistics.route) {
            StatisticsScreen(navController = navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        composable(Screen.ProfilePhoto.route) {
            ProfilePhotoScreen(navController = navController)
        }
    }
}
