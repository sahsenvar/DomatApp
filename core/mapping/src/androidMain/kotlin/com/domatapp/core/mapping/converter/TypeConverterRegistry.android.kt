package com.domatapp.core.mapping.converter

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

actual object TypeConverterRegistry {
    private val converters = ConcurrentHashMap<Pair<String, String>, MapTypeConverter<*, *>>()

    actual fun <S : Any, T : Any> register(converter: MapTypeConverter<S, T>) {
        val forwardKey =
            converter.sourceType.qualifiedName!! to converter.targetType.qualifiedName!!
        val reverseKey =
            converter.targetType.qualifiedName!! to converter.sourceType.qualifiedName!!

        when {
            // Exact duplicate: Same converter registered twice
            converters.containsKey(forwardKey) -> {
                println(
                    """
                    ⚠️ WARNING: Duplicate TypeConverter registration
                    Key: ${forwardKey.first} → ${forwardKey.second}
                    Existing: ${converters[forwardKey]!!::class.simpleName}
                    New: ${converter::class.simpleName}
                    → Existing converter will be overridden.
                    """.trimIndent()
                )
                converters[forwardKey] = converter
            }

            // Bilateral conflict: Reverse converter already exists
            converters.containsKey(reverseKey) -> {
                println(
                    """
                    ⚠️ WARNING: Bilateral TypeConverter conflict detected!

                    Existing converter: ${converters[reverseKey]!!::class.simpleName}
                      Handles: ${reverseKey.first} ↔ ${reverseKey.second} (bilateral)

                    New converter: ${converter::class.simpleName}
                      Would handle: ${forwardKey.first} ↔ ${forwardKey.second} (bilateral)

                    MapTypeConverter is BILATERAL - it already converts both directions!
                    → New converter will NOT be registered (redundant).
                    """.trimIndent()
                )
                // Don't register redundant converter
            }

            else -> {
                converters[forwardKey] = converter
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    actual fun <S : Any, T : Any> get(
        sourceType: KClass<S>,
        targetType: KClass<T>
    ): MapTypeConverter<S, T>? {
        val key = sourceType.qualifiedName!! to targetType.qualifiedName!!
        return converters[key] as? MapTypeConverter<S, T>
    }

    actual fun <S : Any, T : Any> has(
        sourceType: KClass<S>,
        targetType: KClass<T>
    ): Boolean {
        val key = sourceType.qualifiedName!! to targetType.qualifiedName!!
        return converters.containsKey(key)
    }
}
