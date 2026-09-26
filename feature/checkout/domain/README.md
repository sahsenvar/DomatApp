# Module: :feature:checkout:domain

## Purpose
Checkout (Akış C, C4 Ödeme) business models: money, order lines, saved payment cards.
Pure Kotlin - no framework dependencies.

## Status
Models only. Use cases and the repository interface (`GET /v1/deliveries/current`,
`GET /v1/payments/cards`, `POST /v1/orders`, ...) arrive with `:feature:checkout:data`, which does
not exist yet - the C4 ViewModel runs on a stubbed state until then.
