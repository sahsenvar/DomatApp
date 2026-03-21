package com.domatapp.core.mapping

import com.domatapp.core.mapping.converter.MapTypeConverter
import com.domatapp.core.mapping.converter.TypeConverterRegistry

/**
 * Configuration builder for global type converters.
 *
 * Use with [startKMapper] to register custom converters with explicit priority order.
 */
class KMapperConfiguration {
    internal val converters = mutableListOf<MapTypeConverter<*, *>>()

    /**
     * Registers a global type converter.
     *
     * **Order matters:** First registered converter has highest priority.
     * If multiple converters handle the same type pair, the first one will be used.
     *
     * @param converter The converter instance (typically an `object`)
     */
    fun registerGlobalTypeConverter(converter: MapTypeConverter<*, *>) {
        converters.add(converter)
    }
}

/**
 * Starts KMapper configuration with a DSL block (similar to Koin's `startKoin`).
 *
 * **Usage:**
 * ```kotlin
 * // In Application or top-level
 * startKMapper {
 *     registerGlobalTypeConverter(MyCustomIntToStringConverter)
 *     registerGlobalTypeConverter(AnotherConverter)
 * }
 * ```
 *
 * **Priority:**
 * - First registered converter has highest priority
 * - Custom converters override built-in converters
 * - Order is preserved in both compile-time (KSP) and runtime
 *
 * **KSP Integration:**
 * - Annotate the call with `@KMapperConfiguration` for compile-time detection
 * - KSP will parse this block and use converters in generated mapping code
 *
 * @param block Configuration DSL block
 * @return Configuration instance (can be ignored)
 */
fun startKMapper(block: KMapperConfiguration.() -> Unit): KMapperConfiguration {
    val config = KMapperConfiguration().apply(block)

    // Runtime registration (order preserved)
    config.converters.forEach { converter ->
        @Suppress("UNCHECKED_CAST")
        TypeConverterRegistry.register(converter as MapTypeConverter<Any, Any>)
    }

    return config
}
