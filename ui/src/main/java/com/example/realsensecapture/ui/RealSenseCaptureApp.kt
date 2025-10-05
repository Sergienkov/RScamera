package com.example.realsensecapture.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.realsensecapture.data.SessionRepository

@Composable
fun RealSenseCaptureApp(
    sessionRepository: SessionRepository,
    voiceNoteController: VoiceNoteController,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navigateToGallery: () -> Unit = {
        navController.navigate(Screen.Gallery.route) {
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Preview.route,
        modifier = modifier
    ) {
        composable(Screen.Preview.route) {
            PreviewScreen(
                sessionRepository = sessionRepository,
                onNavigateToGallery = navigateToGallery,
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onCaptureSuccess = navigateToGallery
            )
        }
        composable(Screen.Gallery.route) {
            GalleryScreen(
                sessionRepository = sessionRepository,
                onSessionClick = { session ->
                    navController.navigate(Screen.SessionDetails.createRoute(session.id))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.SessionDetails.route,
            arguments = listOf(
                navArgument(Screen.SessionDetails.ARG_ID) { type = NavType.LongType }
            )
        ) { entry ->
            val sessionId = entry.arguments?.getLong(Screen.SessionDetails.ARG_ID)
                ?: return@composable
            SessionDetailsScreen(
                sessionId = sessionId,
                controller = voiceNoteController,
                sessionRepository = sessionRepository,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

private sealed class Screen(val route: String) {
    data object Preview : Screen("preview")
    data object Gallery : Screen("gallery")
    data object Settings : Screen("settings")
    data object SessionDetails : Screen("details/{sessionId}") {
        const val ARG_ID = "sessionId"
        fun createRoute(id: Long) = "details/$id"
    }
}
