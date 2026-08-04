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
import com.example.ui.screens.insights.InsightsScreen
import com.example.ui.screens.onboarding.OnboardingScreen1
import com.example.ui.screens.onboarding.OnboardingScreen2
import com.example.ui.screens.search.AISearchScreen

@Composable
fun ChronovaNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "onboarding1"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
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
                navController.navigate("home") {
                    popUpTo("onboarding1") { inclusive = true }
                }
            })
        }
        composable("home") {
            HomeTimelineScreen(
                onNavigateToSearch = { navController.navigate("search") },
                onNavigateToInsights = { navController.navigate("insights") }
            )
        }
        composable("search") {
            AISearchScreen(
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onNavigateToInsights = { navController.navigate("insights") }
            )
        }
        composable("insights") {
            InsightsScreen(
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onNavigateToSearch = { navController.navigate("search") }
            )
        }
    }
}
