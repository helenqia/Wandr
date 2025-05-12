package hu.ait.wandr.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import hu.ait.wandr.ui.screen.*

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Map : Screen("map", "Map", Icons.Default.AddLocation)
    object Rankings : Screen("rankings", "Rankings", Icons.Default.FormatListNumbered)
    object Splash : Screen("splash", "Splash", Icons.Default.AddLocation)
    object PinDetail : Screen("pin_detail", "Pin Detail", Icons.Default.AddLocation)
}

@Composable
fun WandrNavigation() {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val rankingViewModel: RankingViewModel = hiltViewModel() // ✅ Inject here

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
                RankedListScreen(navController = navController) // ✅ Pass navController
            }
            composable(
                "pin_detail/{index}",
                arguments = listOf(navArgument("index") { type = NavType.IntType })
            ) { backStackEntry ->
                val index = backStackEntry.arguments?.getInt("index") ?: -1
                val pin = rankingViewModel.rankedPlaces.collectAsState().value.getOrNull(index)
                pin?.let { PinDetailScreen(it) }
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
