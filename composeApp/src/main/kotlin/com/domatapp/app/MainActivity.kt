package com.domatapp.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.domatapp.app.navigation.authEntries
import com.domatapp.app.navigation.homeEntries
import com.domatapp.app.scaffold.DomatBottomBar
import com.domatapp.app.scaffold.DomatFab
import com.domatapp.app.scaffold.scaffoldConfig
import com.domatapp.core.navigation.Route
import com.domatapp.core.presentation.compose.LocalNavigator
import com.domatapp.core.presentation.compose.LocalSnackbarHostState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DomatApp()
        }
    }
}

@Composable
fun DomatApp() {
    val mainViewModel: MainViewModel = viewModel()
    val state by mainViewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentRoute = state.currentRoute
    val scaffoldConfig = currentRoute.scaffoldConfig()

    CompositionLocalProvider(
        LocalNavigator provides mainViewModel,
        LocalSnackbarHostState provides snackbarHostState
    ) {
        Scaffold(
            bottomBar = {
                if (scaffoldConfig.showBottomBar) {
                    DomatBottomBar(currentRoute = currentRoute)
                }
            },
            floatingActionButton = {
                if (scaffoldConfig.showFab) {
                    DomatFab(onClick = { /* TODO */ })
                }
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
                    authEntries()
                    homeEntries()
                },
                onBack = { mainViewModel.popBack() }
            )
        }
    }
}
