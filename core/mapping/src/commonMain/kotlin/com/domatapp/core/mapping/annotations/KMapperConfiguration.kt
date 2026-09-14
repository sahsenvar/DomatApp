package com.domatapp.core.mapping.annotations

/**
 * Marks a property or function that contains KMapper configuration.
 *
 * This annotation is used by KSP to detect and parse `startKMapper { }` blocks
 * at compile-time, enabling custom converters to be used in generated mapping code.
 *
 * **Usage:**
 * ```kotlin
 * // Top-level property
 * @KMapperConfiguration
 * val mappers = startKMapper {
 *     registerGlobalTypeConverter(MyCustomConverter::class)
 * }
 *
 * // Or in Application.onCreate()
 * class MyApplication : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         startKMapper { ... }  // No annotation needed if not using generated code
 *     }
 * }
 * ```
 *
 * **Note:**
 * - Only needed if you want custom converters to be used in generated mapping code
 * - Without this annotation, converters are only registered at runtime
 * - KSP will parse the block to extract converter registration order
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION, AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
annotation class KMapperConfiguration
