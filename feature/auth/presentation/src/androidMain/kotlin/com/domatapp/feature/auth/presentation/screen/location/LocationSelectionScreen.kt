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
import com.domatapp.core.presentation.component.bar.BottomActionBar
import com.domatapp.core.presentation.component.button.PrimaryButton
import com.domatapp.feature.auth.presentation.screen.component.LocationCard
import com.domatapp.feature.auth.presentation.screen.component.LocationCardConnector
import com.domatapp.feature.auth.presentation.screen.component.InputDropdown
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
                LocationCard(
                    label = stringResource(R.string.location_district_label),
                    value = stringResource(R.string.location_mock_district),
                    checkmarkPainter = painterResource(R.drawable.ic_checkmark),
                    isLocked = true,
                    lockPainter = painterResource(R.drawable.ic_lock),
                    cardAlpha = 0.6f,
                )
                LocationCardConnector()
                LocationCard(
                    label = stringResource(R.string.location_neighborhood_label),
                    value = stringResource(R.string.location_mock_neighborhood),
                    checkmarkPainter = painterResource(R.drawable.ic_checkmark),
                    isLocked = true,
                    lockPainter = painterResource(R.drawable.ic_lock),
                    cardAlpha = 0.8f,
                )
                LocationCardConnector()
                LocationCard(
                    label = stringResource(R.string.location_building_label),
                    value = stringResource(R.string.location_mock_building),
                    checkmarkPainter = painterResource(R.drawable.ic_checkmark),
                    isLocked = true,
                    lockPainter = painterResource(R.drawable.ic_lock),
                )
                LocationCardConnector()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    InputDropdown(
                        label = stringResource(R.string.location_block_label),
                        value = uiState.selectedBlock ?: stringResource(R.string.placeholder_select),
                        iconPainter = painterResource(R.drawable.ic_building),
                        chevronPainter = painterResource(R.drawable.ic_chevron_down),
                        checkmarkPainter = painterResource(R.drawable.ic_checkmark),
                        items = uiState.blockItems,
                        selectedItem = uiState.selectedBlock,
                        onItemSelected = { item -> onIntent(LocationSelectionIntent.SelectBlock(item)) },
                        modifier = Modifier.weight(1f),
                    )
                    InputDropdown(
                        label = stringResource(R.string.location_apartment_label),
                        value = uiState.selectedApartment ?: stringResource(R.string.placeholder_select),
                        iconPainter = painterResource(R.drawable.ic_door),
                        chevronPainter = painterResource(R.drawable.ic_chevron_down),
                        checkmarkPainter = painterResource(R.drawable.ic_checkmark),
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
            BottomActionBar {
                PrimaryButton(
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
