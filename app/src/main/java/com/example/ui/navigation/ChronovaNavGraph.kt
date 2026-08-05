package com.example.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.home.HomeTimelineScreen
import com.example.ui.screens.home.ReviewDraftsScreen
import com.example.ui.screens.insights.InsightsScreen
import com.example.ui.screens.onboarding.OnboardingScreen1
import com.example.ui.screens.onboarding.OnboardingScreen2
import com.example.ui.screens.search.AISearchScreen
import com.example.ui.screens.capture.CaptureMemoryScreen
import com.example.ui.screens.details.MemoryDetailsScreen
import com.example.ui.screens.memories.MemoriesScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.auth.AuthLandingScreen
import com.example.ui.screens.auth.EmailAuthScreen
import com.example.ui.screens.auth.PhoneAuthScreen
import androidx.compose.ui.Modifier

@Composable
fun ChronovaNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "onboarding1",
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) },
        popEnterTransition = { fadeIn(animationSpec = tween(300)) },
        popExitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        composable("onboarding1") {
            OnboardingScreen1(onNext = { navController.navigate("onboarding2") })
        }
        composable("onboarding2") {
            OnboardingScreen2(onStart = {
                navController.navigate("auth_landing") {
                    popUpTo("onboarding1") { inclusive = true }
                }
            })
        }
        composable("auth_landing") {
            AuthLandingScreen(
                onEmailAuth = { navController.navigate("auth_email") },
                onPhoneAuth = { navController.navigate("auth_phone") }
            )
        }
        composable("auth_email") {
            EmailAuthScreen(
                onAuthSuccess = {
                    navController.navigate("home") {
                        popUpTo("auth_landing") { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("auth_phone") {
            PhoneAuthScreen(
                onAuthSuccess = {
                    navController.navigate("home") {
                        popUpTo("auth_landing") { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("home") {
            HomeTimelineScreen(
                onNavigateToCapture = { navController.navigate("capture") },
                onNavigateToDetails = { memoryId -> navController.navigate("details/$memoryId") },
                onNavigateToReviewDrafts = { navController.navigate("review_drafts") }
            )
        }
        composable("details/{memoryId}") { backStackEntry ->
            val memoryId = backStackEntry.arguments?.getString("memoryId")?.toLongOrNull() ?: return@composable
            MemoryDetailsScreen(
                memoryId = memoryId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("review_drafts") {
            ReviewDraftsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("capture") {
            CaptureMemoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("search") {
            AISearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetails = { memoryId ->
                    navController.navigate("details/$memoryId")
                }
            )
        }
        composable("insights") {
            InsightsScreen()
        }
        composable("memories") {
            MemoriesScreen(
                onNavigateToDetails = { memoryId ->
                    navController.navigate("details/$memoryId")
                }
            )
        }
        composable("profile") {
            ProfileScreen(
                onNavigateToAuth = {
                    navController.navigate("auth_landing") {
                        popUpTo("home") { inclusive = false }
                    }
                }
            )
        }
    }
}
