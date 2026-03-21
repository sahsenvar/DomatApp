package com.domatapp.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.domatapp.app.navigation.homeEntries
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.presentation.compose.LocalNavigator
import com.domatapp.core.presentation.compose.LocalSnackbarHostState
import com.domatapp.app.navigation.authPresentationEntries
import com.domatapp.app.navigation.onboardingPresentationEntries

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DomatTheme {
                DomatApp()
            }
        }
    }
}

@Composable
fun DomatApp() {
    val mainViewModel: MainViewModel = viewModel()
    val state by mainViewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    CompositionLocalProvider(
        LocalNavigator provides mainViewModel,
        LocalSnackbarHostState provides snackbarHostState
    ) {
        Scaffold(
            bottomBar = {},
            floatingActionButton = {},
            topBar = {

            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        ) { innerPadding ->
            NavDisplay(
                backStack = state.backStack,
                modifier = Modifier.padding(innerPadding),
                entryProvider = entryProvider {
                    authPresentationEntries()
                    onboardingPresentationEntries()
                    homeEntries()
                },
                onBack = { mainViewModel.popBack() },
                transitionSpec = {
                    slideInHorizontally(animationSpec = tween(300, easing = LinearEasing)) { it } togetherWith
                            slideOutHorizontally(animationSpec = tween(300, easing = LinearEasing)) { -it }
                },
                popTransitionSpec = {
                    slideInHorizontally(animationSpec = tween(300, easing = LinearEasing)) { -it } togetherWith
                            slideOutHorizontally(animationSpec = tween(300, easing = LinearEasing)) { it }
                },
            )
        }
    }
}
