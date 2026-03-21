package com.domatapp.feature.auth.presentation.screen.location

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.navigation.Route
import com.domatapp.core.navigation.annotations.NavigationScreen
import com.domatapp.core.presentation.component.bar.DomatBottomActionBar
import com.domatapp.core.presentation.component.button.DomatPrimaryButton
import com.domatapp.core.presentation.component.card.DomatLocationCard
import com.domatapp.core.presentation.component.card.DomatLocationCardConnector
import com.domatapp.core.presentation.component.input.DomatInputDropdown
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.domatapp.core.resource.R
import com.domatapp.feature.auth.presentation.location.LocationSelectionIntent
import com.domatapp.feature.auth.presentation.location.LocationSelectionUiState

@NavigationScreen(Route.AuthRoute.LocationSelection::class)
@Composable
fun ColumnScope.LocationSelectionScreen(
    uiState: LocationSelectionUiState,
    onIntent: (LocationSelectionIntent) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            LocationSelectionTopBar(
                onBackClick = { onIntent(LocationSelectionIntent.GoBack) },
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                DomatLocationCard(
                    label = stringResource(R.string.location_district_label),
                    value = stringResource(R.string.location_mock_district),
                    checkmarkPainter = painterResource(R.drawable.ic_checkmark),
                    isLocked = true,
                    lockPainter = painterResource(R.drawable.ic_lock),
                    cardAlpha = 0.6f,
                )
                DomatLocationCardConnector()
                DomatLocationCard(
                    label = stringResource(R.string.location_neighborhood_label),
                    value = stringResource(R.string.location_mock_neighborhood),
                    checkmarkPainter = painterResource(R.drawable.ic_checkmark),
                    isLocked = true,
                    lockPainter = painterResource(R.drawable.ic_lock),
                    cardAlpha = 0.8f,
                )
                DomatLocationCardConnector()
                DomatLocationCard(
                    label = stringResource(R.string.location_building_label),
                    value = stringResource(R.string.location_mock_building),
                    checkmarkPainter = painterResource(R.drawable.ic_checkmark),
                    isLocked = true,
                    lockPainter = painterResource(R.drawable.ic_lock),
                )
                DomatLocationCardConnector()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    DomatInputDropdown(
                        label = stringResource(R.string.location_block_label),
                        value = uiState.selectedBlock ?: stringResource(R.string.placeholder_select),
                        iconPainter = painterResource(R.drawable.ic_building),
                        chevronPainter = painterResource(R.drawable.ic_chevron_down),
                        checkmarkPainter = painterResource(R.drawable.ic_checkmark),
                        onClick = { onIntent(LocationSelectionIntent.ToggleBlokDropdown) },
                        isActive = uiState.isBlokDropdownOpen || uiState.selectedBlock != null,
                        isOpen = uiState.isBlokDropdownOpen,
                        items = uiState.blockItems,
                        selectedItem = uiState.selectedBlock,
                        onItemSelected = { item -> onIntent(LocationSelectionIntent.SelectBlock(item)) },
                        modifier = Modifier.weight(1f),
                    )
                    DomatInputDropdown(
                        label = stringResource(R.string.location_apartment_label),
                        value = uiState.selectedApartment ?: stringResource(R.string.placeholder_select),
                        iconPainter = painterResource(R.drawable.ic_door),
                        chevronPainter = painterResource(R.drawable.ic_chevron_down),
                        checkmarkPainter = painterResource(R.drawable.ic_checkmark),
                        onClick = { onIntent(LocationSelectionIntent.ToggleDaireDropdown) },
                        isActive = uiState.isDaireDropdownOpen || uiState.selectedApartment != null,
                        isOpen = uiState.isDaireDropdownOpen,
                        items = uiState.apartmentItems,
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
                    text = stringResource(R.string.button_continue),
                    onClick = { onIntent(LocationSelectionIntent.Confirm) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.isConfirmEnabled,
                    trailingContent = {
                        Image(
                            painter = painterResource(R.drawable.ic_arrow_forward_white),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                )
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
