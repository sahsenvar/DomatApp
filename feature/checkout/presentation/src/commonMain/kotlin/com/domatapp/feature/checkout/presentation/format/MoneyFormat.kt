package com.domatapp.feature.checkout.presentation.format

import com.domatapp.feature.checkout.domain.model.Money
import kotlin.math.abs

/**
 * Turkish lira display format: `₺1.234,50` (dot thousands separator, comma decimals).
 *
 * Hand-rolled rather than `java.text.NumberFormat` so it stays in commonMain for iOS.
 */
fun Money.formatTry(): String {
    val sign = if (minorUnits < 0) "-" else ""
    val absolute = abs(minorUnits)
    val lira = (absolute / 100).toString()
    val kurus = (absolute % 100).toString().padStart(2, '0')
    val grouped = lira.reversed().chunked(3).joinToString(".").reversed()
    return "$sign₺$grouped,$kurus"
}
