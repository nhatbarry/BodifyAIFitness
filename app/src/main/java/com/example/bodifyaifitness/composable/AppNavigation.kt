package com.example.bodifyaifitness.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bodifyaifitness.pages.LoginPage
import com.example.bodifyaifitness.pages.SignUpPage
import com.example.bodifyaifitness.viewmodel.AuthViewModel

@Composable
fun AppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel){
    val rootNavController = rememberNavController()
    val is_logged_in = false
    NavHost(
        navController = rootNavController,
        startDestination = if (is_logged_in) "main_app" else "login_page"
    ){
        composable("login_page"){
            LoginPage(modifier, rootNavController, authViewModel)
        }
        composable("sign_up_page"){
            SignUpPage(modifier, rootNavController, authViewModel)
        }
        composable("main_app"){
            NavBar(modifier, rootNavController, authViewModel)
        }
    }
}