package com.domatapp.feature.checkout.domain.model

/**
 * An amount in Turkish lira, held in kuruş (1/100 TRY) so arithmetic is exact.
 */
data class Money(val minorUnits: Long) : Comparable<Money> {

    operator fun plus(other: Money): Money = Money(minorUnits + other.minorUnits)

    override fun compareTo(other: Money): Int = minorUnits.compareTo(other.minorUnits)

    companion object {
        val Zero = Money(0)

        fun ofLira(lira: Long): Money = Money(lira * 100)
    }
}
