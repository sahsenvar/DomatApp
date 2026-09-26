package com.domatapp.feature.checkout.domain.model

/** One block of the user's site, in `sortOrder` (`GET /v1/sites/{siteId}/blocks`). */
data class SiteBlock(
    val id: String,
    val name: String,
)
