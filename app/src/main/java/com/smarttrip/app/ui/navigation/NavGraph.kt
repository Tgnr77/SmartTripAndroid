package com.smarttrip.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.smarttrip.app.ui.screens.auth.*
import com.smarttrip.app.ui.screens.favorites.FavoritesScreen
import com.smarttrip.app.ui.screens.history.SearchHistoryScreen
import com.smarttrip.app.ui.screens.home.HomeScreen
import com.smarttrip.app.ui.screens.inspiration.InspirationScreen
import com.smarttrip.app.ui.screens.landing.LandingScreen
import com.smarttrip.app.ui.screens.profile.ProfileScreen
import com.smarttrip.app.ui.screens.search.SearchResultsScreen
import com.smarttrip.app.ui.language.AppStrings
import com.smarttrip.app.ui.language.LanguageManager
import com.smarttrip.app.ui.viewmodel.AuthUiState
import com.smarttrip.app.ui.viewmodel.AuthViewModel

// ─── Routes ────────────────────────────────────────────────────────────────
object Routes {
    const val LANDING       = "landing"
    const val HOME          = "home"
    const val LOGIN         = "login"
    const val REGISTER      = "register"
    const val VERIFY_EMAIL  = "verify_email/{email}"
    const val FORGOT_PASS   = "forgot_password"
    const val RESET_PASS    = "reset_password/{token}"
    const val SEARCH_RESULTS = "search_results"
    const val FAVORITES     = "favorites"
    const val HISTORY       = "history"
    const val PROFILE       = "profile"
    const val INSPIRATION   = "inspiration"

    fun verifyEmail(email: String) = "verify_email/$email"
    fun resetPassword(token: String) = "reset_password/$token"
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

val routesWithBottomBar = setOf(
    Routes.HOME,
    Routes.FAVORITES,
    Routes.HISTORY,
    Routes.PROFILE,
    Routes.INSPIRATION,
    Routes.SEARCH_RESULTS
)

@Composable
fun NavGraph(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val language by LanguageManager.language.collectAsState()
    val isGuest = authState is AuthUiState.Guest
    val bottomNavItems = remember(language, isGuest) {
        val s = AppStrings.forLanguage(language)
        buildList {
            add(BottomNavItem(Routes.HOME,        s.navHome,        Icons.Default.Home))
            if (!isGuest) add(BottomNavItem(Routes.FAVORITES,   s.navFavorites,   Icons.Default.Favorite))
            if (!isGuest) add(BottomNavItem(Routes.HISTORY,     s.navHistory,     Icons.Default.History))
            add(BottomNavItem(Routes.INSPIRATION, s.navInspiration, Icons.Default.Explore))
            add(BottomNavItem(Routes.PROFILE,     s.navProfile,     Icons.Default.Person))
        }
    }

    val showBottomBar = routesWithBottomBar.any { route ->
        currentRoute == route || currentRoute?.startsWith("$route?") == true
    }

    // Redirection automatique selon l'état d'auth
    LaunchedEffect(authState) {
        when (authState) {
            is AuthUiState.Unauthenticated -> {
                if (currentRoute !in listOf(
                        Routes.LANDING, Routes.LOGIN, Routes.REGISTER,
                        Routes.FORGOT_PASS, "verify_email/{email}", "reset_password/{token}"
                    )
                ) {
                    navController.navigate(Routes.LANDING) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is AuthUiState.Authenticated -> {
                if (currentRoute == Routes.LANDING ||
                    currentRoute == Routes.LOGIN ||
                    currentRoute == Routes.REGISTER
                ) {
                    navController.navigate(Routes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            else -> Unit
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val currentDestination = navBackStackEntry?.destination
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any {
                                it.route == item.route || it.route?.startsWith("${item.route}?") == true
                            } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LANDING,
            modifier = Modifier.padding(innerPadding)
        ) {
            // ─── Auth ─────────────────────────────────────────────────────
            composable(Routes.LANDING) {
                LandingScreen(
                    onNavigateAsGuest = {
                        authViewModel.continueAsGuest()
                        navController.navigate(Routes.HOME)
                    },
                    onNavigateToLogin = { navController.navigate(Routes.LOGIN) }
                )
            }
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                    onNavigateToForgotPassword = { navController.navigate(Routes.FORGOT_PASS) },
                    onRequiresVerification = { email ->
                        navController.navigate(Routes.verifyEmail(email))
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    onRegisterSuccess = { email ->
                        navController.navigate(Routes.verifyEmail(email))
                    },
                    onNavigateToLogin = { navController.navigate(Routes.LOGIN) }
                )
            }
            composable(
                route = Routes.VERIFY_EMAIL,
                arguments = listOf(navArgument("email") { type = NavType.StringType })
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                VerifyEmailScreen(
                    email = email,
                    onVerified = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.LANDING)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.FORGOT_PASS) {
                ForgotPasswordScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Routes.RESET_PASS,
                arguments = listOf(navArgument("token") { type = NavType.StringType })
            ) { backStackEntry ->
                val token = backStackEntry.arguments?.getString("token") ?: ""
                ResetPasswordScreen(
                    token = token,
                    onReset = { navController.navigate(Routes.LOGIN) },
                    onBack = { navController.popBackStack() }
                )
            }

            // ─── Écrans principaux ────────────────────────────────────────
            composable(
                route = "${Routes.HOME}?destCode={destCode}&destName={destName}",
                arguments = listOf(
                    navArgument("destCode") { type = NavType.StringType; defaultValue = "" },
                    navArgument("destName") { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStackEntry ->
                val destCode = backStackEntry.arguments?.getString("destCode") ?: ""
                val destName = backStackEntry.arguments?.getString("destName") ?: ""
                HomeScreen(
                    prefillDestCode = destCode,
                    prefillDestName = destName,
                    onSearch = { params ->
                        navController.navigate("${Routes.SEARCH_RESULTS}?$params")
                    }
                )
            }
            composable(
                route = "${Routes.SEARCH_RESULTS}?origin={origin}&destination={destination}&departureDate={departureDate}&returnDate={returnDate}&passengers={passengers}&class={class}&nonStop={nonStop}&tripType={tripType}",
                arguments = listOf(
                    navArgument("origin") { type = NavType.StringType; defaultValue = "" },
                    navArgument("destination") { type = NavType.StringType; defaultValue = "" },
                    navArgument("departureDate") { type = NavType.StringType; defaultValue = "" },
                    navArgument("returnDate") { type = NavType.StringType; defaultValue = "" },
                    navArgument("passengers") { type = NavType.IntType; defaultValue = 1 },
                    navArgument("class") { type = NavType.StringType; defaultValue = "economy" },
                    navArgument("nonStop") { type = NavType.BoolType; defaultValue = false },
                    navArgument("tripType") { type = NavType.StringType; defaultValue = "roundtrip" }
                )
            ) { backStackEntry ->
                SearchResultsScreen(
                    args = backStackEntry.arguments,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.FAVORITES) {
                FavoritesScreen(
                    onLoginRequired = { navController.navigate(Routes.LOGIN) }
                )
            }
            composable(Routes.HISTORY) {
                SearchHistoryScreen(
                    onLoginRequired = { navController.navigate(Routes.LOGIN) },
                    onSearchAgain = { params ->
                        navController.navigate("${Routes.SEARCH_RESULTS}?$params")
                    }
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(Routes.LANDING) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onLoginRequired = { navController.navigate(Routes.LOGIN) }
                )
            }
            composable(Routes.INSPIRATION) {
                InspirationScreen(
                    onNavigateToHome = { destCode, destName ->
                        navController.navigate("${Routes.HOME}?destCode=$destCode&destName=$destName") {
                            popUpTo(Routes.INSPIRATION) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
