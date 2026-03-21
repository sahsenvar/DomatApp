package com.domatapp.feature.auth.data.converter

import com.domatapp.core.mapping.converter.MapTypeConverter

/**
 * Custom Int to String converter that overrides the built-in converter.
 * Adds a "CUSTOM-" prefix to demonstrate override behavior.
 */
object CustomIntToStringConverter : MapTypeConverter<Int, String>(Int::class, String::class) {
    override fun convertToNonNull(value: Int): String {
        return "CUSTOM-$value"
    }

    override fun convertFromNonNull(value: String): Int {
        return value.removePrefix("CUSTOM-").toIntOrNull()
            ?: error("Invalid custom format: $value")
    }
}
