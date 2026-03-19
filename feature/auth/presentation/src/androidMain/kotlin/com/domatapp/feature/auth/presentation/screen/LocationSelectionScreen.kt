package com.domatapp.feature.auth.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
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
import com.domatapp.core.resource.MR
import com.domatapp.feature.auth.presentation.model.location.LocationSelectionEffect
import com.domatapp.feature.auth.presentation.model.location.LocationSelectionIntent
import com.domatapp.feature.auth.presentation.model.location.LocationSelectionUiState
import dev.icerock.moko.resources.compose.painterResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow

private val blokItems = listOf("A1", "A2", "A3")
private val daireItems = listOf("1", "2", "3", "4", "5", "6", "7", "8")

@NavigationScreen(Route.AuthRoute.LocationSelection::class)
@Composable
fun ColumnScope.LocationSelectionScreen(
    uiState: LocationSelectionUiState,
    onIntent: (LocationSelectionIntent) -> Unit,
) {
    val checkmarkPainter = painterResource(MR.images.ic_checkmark)
    val lockPainter = painterResource(MR.images.ic_lock)
    val backPainter = painterResource(MR.images.ic_arrow_back)
    val chevronPainter = painterResource(MR.images.ic_chevron_down)

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
                        iconPainter = painterResource(MR.images.ic_building),
                        chevronPainter = chevronPainter,
                        checkmarkPainter = checkmarkPainter,
                        onClick = { onIntent(LocationSelectionIntent.ToggleBlokDropdown) },
                        isActive = uiState.isBlokDropdownOpen || uiState.selectedBlock != null,
                        isOpen = uiState.isBlokDropdownOpen,
                        items = blokItems,
                        selectedItem = uiState.selectedBlock,
                        onItemSelected = { item -> onIntent(LocationSelectionIntent.SelectBlock(item)) },
                        modifier = Modifier.weight(1f),
                    )
                    DomatInputDropdown(
                        label = "Daire No",
                        value = uiState.selectedApartment ?: "Seçiniz",
                        iconPainter = painterResource(MR.images.ic_door),
                        chevronPainter = chevronPainter,
                        checkmarkPainter = checkmarkPainter,
                        onClick = { onIntent(LocationSelectionIntent.ToggleDaireDropdown) },
                        isActive = uiState.isDaireDropdownOpen || uiState.selectedApartment != null,
                        isOpen = uiState.isDaireDropdownOpen,
                        items = daireItems,
                        selectedItem = uiState.selectedApartment,
                        onItemSelected = { item -> onIntent(LocationSelectionIntent.SelectApartment(item)) },
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
                            painter = painterResource(MR.images.ic_arrow_forward_white),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                )
            }
        }
    }
}

@NavigationEffectHandler(Route.AuthRoute.LocationSelection::class)
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

@Preview(showBackground = true)
@Composable
private fun LocationSelectionScreenPreview() {
    DomatTheme {
        Column {
            LocationSelectionScreen(
                uiState = LocationSelectionUiState(),
                onIntent = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationSelectionEffectHandlerPreview() {
    DomatTheme {
        LocationSelectionEffectHandler(effectFlow = emptyFlow())
    }
}
