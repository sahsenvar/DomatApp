package com.domatapp.feature.checkout.domain.model

/**
 * One product line of the order being paid for.
 *
 * @property quantityLabel display quantity as the backend formats it ("3 kg", "1 demet").
 * @property lineTotal the maximum amount for this line (provision is taken on the maximum).
 */
data class OrderLine(
    val productName: String,
    val quantityLabel: String,
    val lineTotal: Money,
)
