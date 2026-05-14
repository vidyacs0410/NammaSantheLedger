package com.namma.santhe

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.namma.santhe.ui.navigation.NavGraph
import com.namma.santhe.ui.theme.NammaSantheTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.namma.santhe.data.SessionManager
import javax.inject.Inject
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.namma.santhe.utils.BiometricUtils
import java.util.Locale

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val startDestination = if (sessionManager.isLoggedIn()) {
            com.namma.santhe.ui.navigation.Screen.Home.route
        } else {
            com.namma.santhe.ui.navigation.Screen.Landing.route
        }

        setContent {
            val language by sessionManager.languageFlow.collectAsStateWithLifecycle()
            val isDarkMode by sessionManager.darkModeFlow.collectAsStateWithLifecycle()
            
            var isAuthenticated by remember { mutableStateOf(!sessionManager.isAppLockEnabled()) }
            
            val locale = remember(language) { Locale(language) }
            
            // Update configuration in a LaunchedEffect to avoid complex ContextWrappers
            LaunchedEffect(locale) {
                Locale.setDefault(locale)
                val config = resources.configuration
                config.setLocale(locale)
                resources.updateConfiguration(config, resources.displayMetrics)
            }
            
            LaunchedEffect(Unit) {
                if (sessionManager.isAppLockEnabled()) {
                    BiometricUtils.showBiometricPrompt(
                        activity = this@MainActivity,
                        onSuccess = { isAuthenticated = true },
                        onError = { /* Handle error or exit app */ }
                    )
                }
            }

            NammaSantheTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isAuthenticated) {
                        val navController = rememberNavController()
                        NavGraph(navController = navController, startDestination = startDestination)
                    } else {
                        // Show a blank screen while authenticating
                        Box(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}
