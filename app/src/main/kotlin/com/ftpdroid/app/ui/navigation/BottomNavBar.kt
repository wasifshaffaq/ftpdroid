package com.ftpdroid.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.Computer
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(
            route = Routes.Home.route,
            label = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        BottomNavItem(
            route = Routes.ServerDashboard.route,
            label = "Server",
            selectedIcon = Icons.Filled.Dns,
            unselectedIcon = Icons.Outlined.Dns
        ),
        BottomNavItem(
            route = Routes.ProfileList.route,
            label = "Client",
            selectedIcon = Icons.Filled.Computer,
            unselectedIcon = Icons.Outlined.Computer
        ),
        BottomNavItem(
            route = Routes.TransferQueue.route,
            label = "Transfers",
            selectedIcon = Icons.Filled.SwapVert,
            unselectedIcon = Icons.Outlined.SwapVert
        )
    )

    NavigationBar(modifier = modifier) {
        items.forEach { item ->
            val selected = currentRoute.startsWith(item.route)
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) }
            )
        }
    }
}
