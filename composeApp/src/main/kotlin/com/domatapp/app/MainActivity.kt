package com.domatapp.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.domatapp.shared.app.DomatApp

/**
 * The Android host. Its iOS counterpart is `MainViewController()` in `:shared`; everything below
 * `DomatApp` - the theme, the snackbar host, the navigator and every screen - is shared.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DomatApp(onRootBack = { finish() })
        }
    }
}
