package com.example.bodifyaifitness.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bodifyaifitness.pages.EditProfilePage
import com.example.bodifyaifitness.pages.ExerciseDetailPage
import com.example.bodifyaifitness.pages.LoginPage
import com.example.bodifyaifitness.pages.ScheduleDetailPage
import com.example.bodifyaifitness.pages.SetupProfilePage
import com.example.bodifyaifitness.pages.SignUpPage
import com.example.bodifyaifitness.ui.theme.GymSurfaceBg
import com.example.bodifyaifitness.viewmodel.AuthState
import com.example.bodifyaifitness.viewmodel.AuthViewModel

@Composable
fun AppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    val rootNavController = rememberNavController()
    val authState by authViewModel.authState.observeAsState()

    // Chờ authState resolve trước khi render NavHost (tránh flash login page)
    if (authState == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GymSurfaceBg)
        )
        return
    }

    val startDestination = if (authState is AuthState.Authenticated) "main_app" else "login_page"

    NavHost(
        navController = rootNavController,
        startDestination = startDestination
    ) {
        composable("login_page") {
            LoginPage(modifier, rootNavController, authViewModel)
        }
        composable("sign_up_page") {
            SignUpPage(modifier, rootNavController, authViewModel)
        }
        composable("main_app") {
            NavBar(modifier, rootNavController, authViewModel)
        }
        composable("edit_profile") {
            EditProfilePage(modifier = modifier, navController = rootNavController)
        }
        composable("setup_profile") {
            SetupProfilePage(navController = rootNavController)
        }
        composable(
            route = "exercise_detail/{exerciseId}?showLog={showLog}",
            arguments = listOf(
                navArgument("showLog") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: ""
            val showLog    = backStackEntry.arguments?.getBoolean("showLog") ?: false
            ExerciseDetailPage(
                exerciseId = exerciseId,
                navController = rootNavController,
                showLog = showLog
            )
        }
        composable("schedule_detail/{scheduleId}") { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getString("scheduleId") ?: ""
            ScheduleDetailPage(scheduleId = scheduleId, navController = rootNavController)
        }
    }
}