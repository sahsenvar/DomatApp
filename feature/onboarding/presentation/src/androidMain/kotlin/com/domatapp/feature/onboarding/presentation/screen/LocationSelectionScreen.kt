package com.domatapp.feature.onboarding.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationEffectHandler
import com.domatapp.core.navigation.annotations.NavigationScreen
import com.domatapp.core.presentation.component.bar.DomatBottomActionBar
import com.domatapp.core.presentation.component.button.DomatPrimaryButton
import com.domatapp.core.presentation.component.card.DomatLocationCard
import com.domatapp.core.presentation.component.card.DomatLocationCardConnector
import com.domatapp.core.presentation.component.header.DomatScreenHeader
import com.domatapp.core.presentation.component.indicator.DomatProgressSteps
import com.domatapp.core.presentation.component.input.DomatInputDropdown
import com.domatapp.core.presentation.compose.LocalNavigator
import com.domatapp.feature.onboarding.presentation.model.location.LocationSelectionEffect
import com.domatapp.feature.onboarding.presentation.model.location.LocationSelectionIntent
import com.domatapp.feature.onboarding.presentation.model.location.LocationSelectionUiState
import domatapp.feature.onboarding.presentation.generated.resources.Res
import domatapp.feature.onboarding.presentation.generated.resources.ic_arrow_back
import domatapp.feature.onboarding.presentation.generated.resources.ic_arrow_forward_white
import domatapp.feature.onboarding.presentation.generated.resources.ic_building
import domatapp.feature.onboarding.presentation.generated.resources.ic_checkmark
import domatapp.feature.onboarding.presentation.generated.resources.ic_chevron_down
import domatapp.feature.onboarding.presentation.generated.resources.ic_door
import domatapp.feature.onboarding.presentation.generated.resources.ic_lock
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.painterResource
import kotlinx.coroutines.flow.collectLatest

private val blokItems = listOf("A1", "A2", "A3")
private val daireItems = listOf("1", "2", "3", "4", "5", "6", "7", "8")

@NavigationScreen(Route.OnboardingRoute.LocationSelection::class)
@Composable
fun ColumnScope.LocationSelectionScreen(
    uiState: LocationSelectionUiState,
    onIntent: (LocationSelectionIntent) -> Unit,
) {
    var blokDropdownOpen by remember { mutableStateOf(false) }
    var daireDropdownOpen by remember { mutableStateOf(false) }

    val checkmarkPainter = painterResource(Res.drawable.ic_checkmark)
    val lockPainter = painterResource(Res.drawable.ic_lock)
    val backPainter = painterResource(Res.drawable.ic_arrow_back)
    val chevronPainter = painterResource(Res.drawable.ic_chevron_down)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DomatScreenHeader(
                title = "Teslimat Bölgesi",
                onBackClick = { onIntent(LocationSelectionIntent.GoBack) },
                backIconPainter = backPainter,
                bottomContent = {
                    DomatProgressSteps(totalSteps = 4, currentStep = 1)
                },
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                DomatLocationCard(
                    label = "İlçe / İl",
                    value = "Tuzla/İstanbul",
                    checkmarkPainter = checkmarkPainter,
                    isLocked = true,
                    lockPainter = lockPainter,
                    cardAlpha = 0.6f,
                )
                DomatLocationCardConnector()
                DomatLocationCard(
                    label = "Mahalle",
                    value = "Aydınlı Mh.",
                    checkmarkPainter = checkmarkPainter,
                    isLocked = true,
                    lockPainter = lockPainter,
                    cardAlpha = 0.8f,
                )
                DomatLocationCardConnector()
                DomatLocationCard(
                    label = "Site / Apartman",
                    value = "Dumankaya Adres Lobi.",
                    checkmarkPainter = checkmarkPainter,
                    isLocked = true,
                    lockPainter = lockPainter,
                )
                DomatLocationCardConnector()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    DomatInputDropdown(
                        label = "Blok No",
                        value = uiState.selectedBlock ?: "Seçiniz",
                        iconPainter = painterResource(Res.drawable.ic_building),
                        chevronPainter = chevronPainter,
                        checkmarkPainter = checkmarkPainter,
                        onClick = {
                            blokDropdownOpen = !blokDropdownOpen
                            if (blokDropdownOpen) daireDropdownOpen = false
                        },
                        isActive = blokDropdownOpen || uiState.selectedBlock != null,
                        isOpen = blokDropdownOpen,
                        items = blokItems,
                        selectedItem = uiState.selectedBlock,
                        onItemSelected = { item ->
                            onIntent(LocationSelectionIntent.SelectBlock(item))
                            blokDropdownOpen = false
                        },
                        modifier = Modifier.weight(1f),
                    )
                    DomatInputDropdown(
                        label = "Daire No",
                        value = uiState.selectedApartment ?: "Seçiniz",
                        iconPainter = painterResource(Res.drawable.ic_door),
                        chevronPainter = chevronPainter,
                        checkmarkPainter = checkmarkPainter,
                        onClick = {
                            daireDropdownOpen = !daireDropdownOpen
                            if (daireDropdownOpen) blokDropdownOpen = false
                        },
                        isActive = daireDropdownOpen || uiState.selectedApartment != null,
                        isOpen = daireDropdownOpen,
                        items = daireItems,
                        selectedItem = uiState.selectedApartment,
                        onItemSelected = { item ->
                            onIntent(LocationSelectionIntent.SelectApartment(item))
                            daireDropdownOpen = false
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
        ) {
            DomatBottomActionBar {
                DomatPrimaryButton(
                    text = "Devam Et",
                    onClick = { onIntent(LocationSelectionIntent.Confirm) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.isConfirmEnabled,
                    trailingContent = {
                        Image(
                            painter = painterResource(Res.drawable.ic_arrow_forward_white),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                )
            }
        }
    }
}

@NavigationEffectHandler(Route.OnboardingRoute.LocationSelection::class)
@Composable
fun LocationSelectionEffectHandler(effectFlow: Flow<LocationSelectionEffect>) {
    val navigator = LocalNavigator.current
    LaunchedEffect(effectFlow) {
        effectFlow.collectLatest { effect ->
            when (effect) {
                LocationSelectionEffect.NavigateToHome -> navigator.replaceAll(Route.Main.Home)
                LocationSelectionEffect.NavigateBack -> navigator.popBack()
            }
        }
    }
}
