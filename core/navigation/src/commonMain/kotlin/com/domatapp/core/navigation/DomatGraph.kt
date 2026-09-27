package com.domatapp.core.navigation

import dev.gezgin.core.Route
import dev.gezgin.core.annotation.GoTo
import dev.gezgin.core.annotation.NavGraph
import dev.gezgin.core.annotation.NoBack
import dev.gezgin.core.annotation.ReplaceTo

/**
 * The application's navigation graph.
 *
 * Every edge below is declared on the route it starts from, and Gezgin's KSP processor turns each
 * one into a single method on that route's generated `<X>Navigator`. A screen can therefore only
 * navigate along the edges its own route declares - `nav.goToLogin()` exists on
 * `OnboardingWelcomeNavigator` because [OnboardingGraph.OnboardingWelcomeRoute] declares
 * `@GoTo(LoginRoute)`, and there is no method at all for anywhere it did not declare.
 *
 * Route types carry no `@Serializable`: Gezgin generates and registers their serializers itself
 * (`gezginSerializersModule` / `gezginJson`). A route that gains a parameter of a project-defined
 * type would need `@Serializable` on that parameter type, not on the route.
 *
 * Naming: the generated names strip a trailing `Route` and then a trailing `Screen`/`Flow`, so
 * `LoginRoute` yields `LoginNavigator`, `provideLoginEntry()` and `goToLogin()`.
 */
@NavGraph
sealed interface OnboardingGraph : Route {

    /**
     * The app's start destination - see `rememberNavigator(start = ...)` in `MainActivity`.
     *
     * Because it is always the bottom of the stack, it is also the `clearUpTo` anchor the auth
     * routes use to wipe the funnel on a successful sign-in.
     */
    @GoTo(AuthGraph.LoginRoute::class)
    data object OnboardingWelcomeRoute : OnboardingGraph
}

@NavGraph
sealed interface AuthGraph : Route {

    /**
     * `goToLocationSelection()` for a brand-new user, `replaceToHome()` for a returning one.
     *
     * The replace clears up to and including the start destination, which is what the old
     * hand-rolled `Navigator.replaceAll(Route.Main.Home)` did: after a successful login the
     * onboarding/auth funnel must not be reachable by system or predictive back.
     */
    @GoTo(LocationSelectionRoute::class)
    @ReplaceTo(
        target = MainGraph.HomeRoute::class,
        clearUpTo = OnboardingGraph.OnboardingWelcomeRoute::class,
        inclusive = true,
    )
    data object LoginRoute : AuthGraph

    /**
     * Confirming the address finishes the funnel the same way [LoginRoute] does. Cancelling is the
     * implicit single-step `back()` Gezgin generates for every route that is not `@NoBack`, so no
     * annotation is needed for it.
     */
    @ReplaceTo(
        target = MainGraph.HomeRoute::class,
        clearUpTo = OnboardingGraph.OnboardingWelcomeRoute::class,
        inclusive = true,
    )
    data object LocationSelectionRoute : AuthGraph
}

@NavGraph
sealed interface MainGraph : Route {

    /**
     * Terminal for now: the signed-in area declares no outgoing edges yet, so `HomeNavigator`
     * carries only the implicit `back()`. Since Home is the stack root once the funnel is cleared,
     * that back reaches `onRootBack` and finishes the Activity.
     */
    data object HomeRoute : MainGraph
}

@NavGraph
sealed interface CheckoutGraph : Route {

    /**
     * C1 - Telefon Numarası Girişi (design/screens/C1). A successful OTP request pushes C2 with the
     * number; back is the implicit single-step `back()` to the cart. Nothing navigates *to* this
     * route yet (the cart, B3, does not exist).
     */
    @GoTo(OtpVerifyRoute::class)
    data object PhoneEntryRoute : CheckoutGraph

    /**
     * C2 - OTP Doğrulama (design/screens/C2). "Numarayı Değiştir" is the implicit `back()` to C1.
     *
     * After a successful verify + cart merge the sign-in funnel (C1 and C2) must not be reachable
     * by back any more, so every forward edge replaces up to and including [PhoneEntryRoute] -
     * which is always on the stack under C2. Back from the next screen returns to the cart.
     */
    @ReplaceTo(target = AddressRoute::class, clearUpTo = PhoneEntryRoute::class, inclusive = true)
    @ReplaceTo(target = PaymentRoute::class, clearUpTo = PhoneEntryRoute::class, inclusive = true)
    @ReplaceTo(target = WindowClosedRoute::class, clearUpTo = PhoneEntryRoute::class, inclusive = true)
    data class OtpVerifyRoute(val phoneNumber: String) : CheckoutGraph

    /**
     * C3 - Adres Ekleme (design/screens/C3). Both "Devam Et" (after the save) and the invoice row's
     * "Düzenle" go to C4.
     */
    @GoTo(PaymentRoute::class)
    data object AddressRoute : CheckoutGraph

    /**
     * C4 - Ödeme (design/screens/C4). `POST /v1/orders` 2xx replaces this screen with C5 (back
     * must not return to the payment form); `409 window_closed` replaces it with [WindowClosedRoute].
     *
     * Still missing: "Sepete Dön ve Ürün Ekle" is designed as `@BackTo(B3 / cart)`, and the cart
     * route does not exist yet - the implicit `back()` stands in for it.
     */
    @ReplaceTo(target = OrderConfirmationRoute::class, clearUpTo = PaymentRoute::class, inclusive = true)
    @ReplaceTo(target = WindowClosedRoute::class, clearUpTo = PaymentRoute::class, inclusive = true)
    data object PaymentRoute : CheckoutGraph

    /**
     * C5 - Sipariş Onay (design/screens/C5). No back (`noBack`); "Pazar'a Dön" clears the whole
     * stack onto Home (Pazar), anchoring on the start destination like the auth routes do.
     */
    @NoBack
    @ReplaceTo(
        target = MainGraph.HomeRoute::class,
        clearUpTo = OnboardingGraph.OnboardingWelcomeRoute::class,
        inclusive = true,
    )
    data class OrderConfirmationRoute(val orderId: String) : CheckoutGraph

    /**
     * Pencere Kapandı (design/screens/WindowClosed), reached from C2 (cart merge) or C4 (order).
     * No back; "Pazar'a Dön" clears the stack onto Home exactly like C5.
     */
    @NoBack
    @ReplaceTo(
        target = MainGraph.HomeRoute::class,
        clearUpTo = OnboardingGraph.OnboardingWelcomeRoute::class,
        inclusive = true,
    )
    data object WindowClosedRoute : CheckoutGraph
}
