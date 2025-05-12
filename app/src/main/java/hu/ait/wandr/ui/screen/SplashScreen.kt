package hu.ait.wandr.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavHostController
import hu.ait.wandr.R
import hu.ait.wandr.ui.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val offsetX = remember { Animatable(-200f) } // Start far off the left side

    LaunchedEffect(true) {
        // Animate to beyond the right edge of the screen
        offsetX.animateTo(
            targetValue = screenWidth + 200f, // Move across and off the right
            animationSpec = tween(durationMillis = 5000)
        )

        delay(500) // short pause after animation
        navController.navigate(Screen.Map.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
        Image(
            painter = painterResource(id = R.drawable.travel_icon),
            contentDescription = "Logo",
            modifier = Modifier
                .offset { IntOffset(offsetX.value.dp.roundToPx(), 0) }
        )
    }
}
