package com.example.bodifyaifitness.composable

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.bodifyaifitness.dataclass.NavItem
import com.example.bodifyaifitness.pages.AccountPage
import com.example.bodifyaifitness.pages.ExplorerPage
import com.example.bodifyaifitness.pages.HistoryPage
import com.example.bodifyaifitness.pages.SchedulePage
import com.example.bodifyaifitness.pages.StartPage

@Composable
fun NavBar(modifier: Modifier = Modifier){

    val navItemList = listOf(
        NavItem(label = "Explorer", icon = Icons.Default.Home),
        NavItem(label = "Schedule", icon = Icons.Default.FormatListNumbered),
        NavItem(label = "Start", icon = Icons.Default.PlayArrow),
        NavItem(label = "History", icon = Icons.Default.History),
        NavItem(label = "Account", icon = Icons.Default.AccountCircle),
    )

    var selectedIndex by remember {
        mutableIntStateOf(0)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                        },
                        icon = {
                            Icon(imageVector = navItem.icon, contentDescription = "Icon")
                        },
                        label = {
                            Text(text = navItem.label)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        ContentScreen(modifier = Modifier.padding(innerPadding), selectedIndex)
    }
}

@Composable
fun ContentScreen(modifier: Modifier = Modifier, selectedIndex: Int){
    when(selectedIndex){
        0-> ExplorerPage()
        1-> SchedulePage()
        2-> StartPage()
        3-> HistoryPage()
        4-> AccountPage()
    }
}