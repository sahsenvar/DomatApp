package com.domatapp.core.local.annotations

/**
 * Marks an interface as a Local Data Source to be processed by KSP.
 * The processor will generate an implementation that uses LocalApi.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class LocalDataSource(val name: String)
