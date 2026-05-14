package com.namma.santhe.ui.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.namma.santhe.ui.screens.customer.CustomerLedgerScreen
import com.namma.santhe.ui.screens.entry.QuickEntryScreen
import com.namma.santhe.ui.screens.home.HomeScreen
import com.namma.santhe.ui.screens.summary.DailySummaryScreen

import androidx.hilt.navigation.compose.hiltViewModel
import com.namma.santhe.ui.screens.auth.*
import com.namma.santhe.ui.screens.profile.ProfileScreen
import com.namma.santhe.ui.screens.profile.ProfileViewModel
import com.namma.santhe.ui.screens.settings.SettingsScreen
import com.namma.santhe.ui.screens.settings.SettingsViewModel
import com.namma.santhe.ui.screens.settings.HelpSupportScreen
import com.namma.santhe.ui.screens.settings.DataSecurityScreen
import com.namma.santhe.ui.screens.settings.AppLockScreen
import com.namma.santhe.ui.screens.settings.AppLockViewModel
import com.namma.santhe.ui.screens.settings.PaymentPinScreen

sealed class Screen(val route: String) {
    object Landing : Screen("landing")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Otp : Screen("otp")
    object ForgotPassword : Screen("forgot_password")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Home : Screen("home")
    object QuickEntry : Screen("entry?customerId={customerId}") {
        fun withCustomer(id: Int?): String = "entry?customerId=${id ?: -1}"
    }
    object CustomerLedger : Screen("customer/{customerId}") {
        fun create(id: Int): String = "customer/$id"
    }
    object DailySummary : Screen("summary")
    object HelpSupport : Screen("help_support")
    object DataSecurity : Screen("data_security/{type}") {
        fun create(type: String): String = "data_security/$type"
    }
    object AppLock : Screen("app_lock")
    object PaymentPin : Screen("payment_pin")
    object ChangeNumber : Screen("change_number")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    val authViewModel: AuthViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { fadeOut() }
    ) {
        composable(route = Screen.Landing.route) {
            LandingScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToSignup = { navController.navigate(Screen.Signup.route) }
            )
        }

        composable(route = Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Landing.route) { inclusive = true }
                    }
                },
                onNavigateToSignup = { navController.navigate(Screen.Signup.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Signup.route) {
            SignupScreen(
                viewModel = authViewModel,
                onNavigateToOtp = { navController.navigate(Screen.Otp.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Otp.route) {
            OtpScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Landing.route) { inclusive = false }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Profile.route) {
            val viewModel: ProfileViewModel = hiltViewModel()
            ProfileScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChangeNumber = { navController.navigate(Screen.ChangeNumber.route) }
            )
        }

        composable(route = Screen.ChangeNumber.route) {
            val viewModel: ProfileViewModel = hiltViewModel()
            com.namma.santhe.ui.screens.profile.ChangeNumberScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToEntry = { customerId ->
                    navController.navigate(Screen.QuickEntry.withCustomer(customerId))
                },
                onNavigateToCustomer = { customerId ->
                    navController.navigate(Screen.CustomerLedger.create(customerId))
                },
                onNavigateToSummary = {
                    navController.navigate(Screen.DailySummary.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(route = Screen.Settings.route) {
            val viewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHelp = { navController.navigate(Screen.HelpSupport.route) },
                onNavigateToDataSecurity = { type -> navController.navigate(Screen.DataSecurity.create(type)) }
            )
        }

        composable(route = Screen.HelpSupport.route) {
            HelpSupportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.DataSecurity.route,
            arguments = listOf(
                navArgument("type") {
                    type = NavType.StringType
                    defaultValue = "all"
                }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "all"
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            DataSecurityScreen(
                type = type,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChangePassword = { navController.navigate(Screen.ForgotPassword.route) },
                onNavigateToAppLock = { navController.navigate(Screen.AppLock.route) },
                onNavigateToPaymentPin = { navController.navigate(Screen.PaymentPin.route) },
                onLogout = {
                    settingsViewModel.clearSession()
                    navController.navigate(Screen.Landing.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.AppLock.route) {
            val viewModel: AppLockViewModel = hiltViewModel()
            AppLockScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.PaymentPin.route) {
            PaymentPinScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.QuickEntry.route,
            arguments = listOf(
                navArgument("customerId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) {
            QuickEntryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CustomerLedger.route,
            arguments = listOf(
                navArgument("customerId") {
                    type = NavType.IntType
                }
            )
        ) {
            CustomerLedgerScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEntry = { customerId ->
                    navController.navigate(Screen.QuickEntry.withCustomer(customerId))
                }
            )
        }

        composable(route = Screen.DailySummary.route) {
            DailySummaryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
