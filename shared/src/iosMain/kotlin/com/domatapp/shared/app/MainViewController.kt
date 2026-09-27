package com.domatapp.shared.app

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * The entry point the Xcode project instantiates, wrapped by `ContentView` in `iosApp`.
 *
 * Kotlin/Native exports a file's top-level functions on a class named after the file, so Swift
 * calls this as `MainViewControllerKt.MainViewController()`.
 *
 * Root back is a no-op: iOS forbids an application from terminating itself, so back simply stops at
 * the start destination rather than doing Android's `finish()`.
 */
fun MainViewController(): UIViewController = ComposeUIViewController { DomatApp(onRootBack = {}) }
