package com.lurenjia534.httprequest.NavItem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(val label: String, val icon: ImageVector, val route: String)

val navItems = listOf(
    NavItem("Send", Icons.AutoMirrored.Filled.Send, "send"),
    NavItem("result", Icons.Default.Info, "records"),
)
