package com.domatapp.shared.di

/**
 * Koin initialization is now handled in platform-specific code.
 *
 * For Android: See MainActivity.kt
 * For iOS: Will be in App.swift
 *
 * This approach allows each platform to access KSP-generated code
 * without compile-time visibility issues.
 */
