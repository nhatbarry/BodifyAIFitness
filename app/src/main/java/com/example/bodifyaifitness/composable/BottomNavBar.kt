package com.example.bodifyaifitness.composable

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bodifyaifitness.dataclass.NavItem
import com.example.bodifyaifitness.pages.AccountPage
import com.example.bodifyaifitness.pages.ExplorerPage
import com.example.bodifyaifitness.pages.HistoryPage
import com.example.bodifyaifitness.pages.SchedulePage
import com.example.bodifyaifitness.pages.StartPage
import com.example.bodifyaifitness.viewmodel.AuthViewModel

@Composable
fun NavBar(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    val navItemList = listOf(
        NavItem(label = "Explorer", icon = Icons.Default.Home, route = "explorer_page"),
        NavItem(label = "Schedule", icon = Icons.Default.FormatListNumbered, route = "schedule_page"),
        NavItem(label = "Start", icon = Icons.Default.PlayArrow, route = "start_page"),
        NavItem(label = "History", icon = Icons.Default.History, route = "history_page"),
        NavItem(label = "Account", icon = Icons.Default.AccountCircle, route = "account_page"),
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                navItemList.forEach { navItem ->
                    NavigationBarItem(
                        selected = currentRoute == navItem.route,
                        onClick = {
                            navController.navigate(navItem.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(imageVector = navItem.icon, contentDescription = navItem.label)
                        },
                        label = {
                            Text(text = navItem.label)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = "explorer_page",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("explorer_page") { ExplorerPage() }
            composable("schedule_page") { SchedulePage() }
            composable("start_page") { StartPage() }
            composable("history_page") { HistoryPage() }
            composable("account_page") { AccountPage() }
        }
    }
}