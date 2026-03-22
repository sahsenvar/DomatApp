package com.domatapp.core.mapping.converter.builtin

import com.domatapp.core.mapping.converter.MapTypeConverter
import kotlinx.datetime.Instant

/**
 * Converts ISO-8601 String to Instant.
 */
object StringToInstantConverter : MapTypeConverter<String, Instant>(String::class, Instant::class) {
    override fun convertToNonNull(value: String): Instant = Instant.parse(value)
    override fun convertFromNonNull(value: Instant): String = value.toString()
}

/**
 * Converts epoch milliseconds (Long) to Instant.
 */
object LongToInstantConverter : MapTypeConverter<Long, Instant>(Long::class, Instant::class) {
    override fun convertToNonNull(value: Long): Instant = Instant.fromEpochMilliseconds(value)
    override fun convertFromNonNull(value: Instant): Long = value.toEpochMilliseconds()
}
