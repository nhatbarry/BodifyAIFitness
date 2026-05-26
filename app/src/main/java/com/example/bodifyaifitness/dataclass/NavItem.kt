package com.example.bodifyaifitness.dataclass

import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(
    var label: String,
    var icon: ImageVector,
    val route: String

)