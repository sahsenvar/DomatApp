package com.domatapp.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.domatapp.android.ui.auth.AuthScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}

@Composable
fun App() {
    AuthScreen(
        onGoogleSignInRequested = {
            // TODO: Implement Google Sign-In integration
            "dummy-google-id-token-for-testing"
        },
        onNavigateToHome = {
            // TODO: Navigate to home screen
            println("Navigate to home screen")
        }
    )
}

@Preview
@Composable
fun AppPreview() {
    App()
}
