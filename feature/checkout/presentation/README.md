# Module: :feature:checkout:presentation

## Purpose
Checkout screens of Akış C. Currently: **C4 - Ödeme** (`CheckoutGraph.PaymentRoute`), built from
`design/screens/C4/`.

## Layout
- `commonMain/.../payment/` - `PaymentUiState` / `PaymentIntent` / `PaymentEffect` / `PaymentViewModel`
  (shared MVI, no Android APIs).
- `androidMain/.../screen/payment/` - `@Screen` composable, Gezgin bindings, one `@Preview` per
  design state (`C4FirstOrderPreview`, ...).

## Status
No data layer yet: the ViewModel starts from `PaymentStubData` and `PayClicked` only simulates the
request. The C5 / WindowClosed / cart edges are not in the graph yet (see `CheckoutGraph`).
