package com.domatapp.feature.checkout.presentation.address

import com.domatapp.feature.checkout.domain.model.SiteBlock

/**
 * Stand-in for what C3 loads on entry (the user's site, `GET /v1/sites/{siteId}/blocks`) until
 * `:feature:checkout:data` exists. Values are the design package's `sampleData`
 * (design/screens/C3/card.yaml). Delete with the stub.
 */
internal object AddressStubData {

    val blocks = listOf(
        SiteBlock(id = "block-a", name = "A Blok"),
        SiteBlock(id = "block-b", name = "B Blok"),
        SiteBlock(id = "block-c", name = "C Blok"),
    )

    val initialState = AddressUiState(
        siteName = "Dalyan Sitesi",
        blocks = blocks,
        isFirstOrder = true,
    )
}
