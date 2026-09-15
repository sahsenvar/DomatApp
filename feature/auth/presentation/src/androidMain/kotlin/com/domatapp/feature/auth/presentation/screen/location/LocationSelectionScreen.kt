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
import com.domatapp.core.navigation.AuthGraph
import dev.gezgin.core.annotation.Screen
import com.domatapp.core.presentation.component.bar.BottomActionBar
import com.domatapp.core.presentation.component.button.PrimaryButton
import com.domatapp.core.resource.generated.resources.Res
import com.domatapp.core.resource.generated.resources.button_continue
import com.domatapp.core.resource.generated.resources.ic_arrow_forward_white
import com.domatapp.core.resource.generated.resources.ic_building
import com.domatapp.core.resource.generated.resources.ic_checkmark
import com.domatapp.core.resource.generated.resources.ic_chevron_down
import com.domatapp.core.resource.generated.resources.ic_door
import com.domatapp.core.resource.generated.resources.ic_lock
import com.domatapp.core.resource.generated.resources.location_apartment_label
import com.domatapp.core.resource.generated.resources.location_block_label
import com.domatapp.core.resource.generated.resources.location_building_label
import com.domatapp.core.resource.generated.resources.location_district_label
import com.domatapp.core.resource.generated.resources.location_mock_building
import com.domatapp.core.resource.generated.resources.location_mock_district
import com.domatapp.core.resource.generated.resources.location_mock_neighborhood
import com.domatapp.core.resource.generated.resources.location_neighborhood_label
import com.domatapp.core.resource.generated.resources.placeholder_select
import com.domatapp.feature.auth.presentation.location.LocationSelectionIntent
import com.domatapp.feature.auth.presentation.location.LocationSelectionUiState
import com.domatapp.feature.auth.presentation.screen.component.InputDropdown
import com.domatapp.feature.auth.presentation.screen.component.LocationCard
import com.domatapp.feature.auth.presentation.screen.component.LocationCardConnector
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Screen(AuthGraph.LocationSelectionRoute::class)
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
                    label = stringResource(Res.string.location_district_label),
                    value = stringResource(Res.string.location_mock_district),
                    checkmarkPainter = painterResource(Res.drawable.ic_checkmark),
                    isLocked = true,
                    lockPainter = painterResource(Res.drawable.ic_lock),
                    cardAlpha = 0.6f,
                )
                LocationCardConnector()
                LocationCard(
                    label = stringResource(Res.string.location_neighborhood_label),
                    value = stringResource(Res.string.location_mock_neighborhood),
                    checkmarkPainter = painterResource(Res.drawable.ic_checkmark),
                    isLocked = true,
                    lockPainter = painterResource(Res.drawable.ic_lock),
                    cardAlpha = 0.8f,
                )
                LocationCardConnector()
                LocationCard(
                    label = stringResource(Res.string.location_building_label),
                    value = stringResource(Res.string.location_mock_building),
                    checkmarkPainter = painterResource(Res.drawable.ic_checkmark),
                    isLocked = true,
                    lockPainter = painterResource(Res.drawable.ic_lock),
                )
                LocationCardConnector()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    InputDropdown(
                        label = stringResource(Res.string.location_block_label),
                        value = uiState.selectedBlock ?: stringResource(Res.string.placeholder_select),
                        iconPainter = painterResource(Res.drawable.ic_building),
                        chevronPainter = painterResource(Res.drawable.ic_chevron_down),
                        checkmarkPainter = painterResource(Res.drawable.ic_checkmark),
                        items = uiState.blockItems,
                        selectedItem = uiState.selectedBlock,
                        onItemSelected = { item -> onIntent(LocationSelectionIntent.SelectBlock(item)) },
                        modifier = Modifier.weight(1f),
                    )
                    InputDropdown(
                        label = stringResource(Res.string.location_apartment_label),
                        value = uiState.selectedApartment ?: stringResource(Res.string.placeholder_select),
                        iconPainter = painterResource(Res.drawable.ic_door),
                        chevronPainter = painterResource(Res.drawable.ic_chevron_down),
                        checkmarkPainter = painterResource(Res.drawable.ic_checkmark),
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
                    text = stringResource(Res.string.button_continue),
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
