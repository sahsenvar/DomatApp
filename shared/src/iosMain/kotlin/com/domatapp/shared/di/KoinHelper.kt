package com.domatapp.shared.di

/**
 * Starts Koin from Swift, in `iOSApp.init()`, before any screen composes.
 *
 * Kotlin/Native exports a file's top-level functions on a class named after the file, so Swift
 * calls this as `KoinHelperKt.doInitKoin()`.
 *
 * The typed `KoinHelper` accessor class that used to live here is gone. It existed so SwiftUI views
 * could pull a ViewModel out of Koin by hand - Swift cannot call Koin's reified `get<T>()`. With the
 * UI drawn by Compose, `@ViewModelOf` providers resolve their own ViewModels through
 * `koinViewModel()` against the per-entry ViewModelStore, and Swift never touches the graph.
 */
fun doInitKoin() = initKoin {}
