//package hu.ait.wandr.ui.navigation
//
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.AddLocation
//import androidx.compose.material.icons.filled.FormatListNumbered
//import androidx.compose.material3.Icon
//import androidx.compose.material3.NavigationBar
//import androidx.compose.material3.NavigationBarItem
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.navigation.NavController
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.currentBackStackEntryAsState
//import androidx.navigation.compose.rememberNavController
//import hu.ait.wandr.ui.screen.MapsScreen
//import hu.ait.wandr.ui.screen.RankedListScreen
//
//sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
//    object Map : Screen("map", "Map", Icons.Default.AddLocation)
//    object Rankings : Screen("rankings", "Rankings", Icons.Default.FormatListNumbered)
//}
//
//@Composable
//fun WandrNavigation() {
//    val navController = rememberNavController()
//
//    Scaffold(
//        bottomBar = { WandrBottomNavigation(navController) }
//    ) { innerPadding ->
//        NavHost(
//            navController = navController,
//            startDestination = Screen.Map.route,
//            modifier = Modifier.padding(innerPadding)
//        ) {
//            composable(Screen.Map.route) {
//                MapsScreen(modifier = Modifier)
//            }
//            composable(Screen.Rankings.route) {
//                RankedListScreen()
//            }
//        }
//    }
//}
//
//@Composable
//fun WandrBottomNavigation(navController: NavController) {
//    val items = listOf(
//        Screen.Map,
//        Screen.Rankings
//    )
//
//    NavigationBar {
//        val navBackStackEntry by navController.currentBackStackEntryAsState()
//        val currentRoute = navBackStackEntry?.destination?.route
//
//        items.forEach { screen ->
//            NavigationBarItem(
//                icon = { Icon(screen.icon, contentDescription = screen.title) },
//                label = { Text(screen.title) },
//                selected = currentRoute == screen.route,
//                onClick = {
//                    if (currentRoute != screen.route) {
//                        navController.navigate(screen.route) {
//                            popUpTo(navController.graph.startDestinationId) {
//                                saveState = true
//                            }
//                            launchSingleTop = true
//                            restoreState = true
//                        }
//                    }
//                }
//            )
//        }
//    }
//}

package hu.ait.wandr.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import hu.ait.wandr.ui.screen.MapsScreen
import hu.ait.wandr.ui.screen.RankedListScreen
import hu.ait.wandr.ui.screen.SplashScreen


sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Map : Screen("map", "Map", Icons.Default.AddLocation)
    object Rankings : Screen("rankings", "Rankings", Icons.Default.FormatListNumbered)
    object Splash : Screen("splash", "Splash", Icons.Default.AddLocation)
}

@Composable
fun WandrNavigation() {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute != Screen.Splash.route) {
                WandrBottomNavigation(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(navController)
            }
            composable(Screen.Map.route) {
                MapsScreen(modifier = Modifier)
            }
            composable(Screen.Rankings.route) {
                RankedListScreen()
            }
        }
    }
}

@Composable
fun WandrBottomNavigation(navController: NavController) {
    val items = listOf(
        Screen.Map,
        Screen.Rankings
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}