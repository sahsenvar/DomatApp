package com.domatapp.core.mapping.converter.platform

import com.domatapp.core.mapping.converter.MapTypeConverter
import platform.Foundation.NSURL

/**
 * iOS-specific converter: NSURL to String.
 */
object NSURLToStringConverter : MapTypeConverter<NSURL, String>(NSURL::class, String::class) {
    override fun convertToNonNull(value: NSURL): String {
        return value.absoluteString ?: error("NSURL has no absoluteString: $value")
    }

    override fun convertFromNonNull(value: String): NSURL {
        return NSURL(string = value) ?: error("Invalid URL: $value")
    }
}
