package com.domatapp.feature.onboarding.presentation.model.welcome

enum class OnboardingPage(val index: Int) {
    WELCOME(0),
    PRICING(1),
    COMMUNITY(2),
    TRUST(3),
    EFFORTLESS(4);

    fun next(): OnboardingPage? = entries.find { it.index == index + 1 }

    companion object {
        fun fromIndex(index: Int): OnboardingPage =
            entries.find { it.index == index } ?: WELCOME
    }
}
