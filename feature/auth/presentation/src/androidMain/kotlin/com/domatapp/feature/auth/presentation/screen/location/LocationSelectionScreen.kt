package com.domatapp.feature.auth.presentation.screen.location

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.design.theme.spacing
import com.domatapp.core.navigation.AuthGraph
import com.domatapp.core.presentation.component.bar.BottomActionBar
import com.domatapp.core.presentation.component.bar.ScreenHeader
import com.domatapp.core.presentation.component.button.ButtonSize
import com.domatapp.core.presentation.component.button.PrimaryButton
import com.domatapp.core.presentation.component.input.DomatDropdown
import com.domatapp.core.resource.generated.resources.Res
import com.domatapp.core.resource.generated.resources.button_continue
import com.domatapp.core.resource.generated.resources.ic_arrow_forward
import com.domatapp.core.resource.generated.resources.location_apartment_label
import com.domatapp.core.resource.generated.resources.location_block_label
import com.domatapp.core.resource.generated.resources.location_building_label
import com.domatapp.core.resource.generated.resources.location_district_label
import com.domatapp.core.resource.generated.resources.location_mock_building
import com.domatapp.core.resource.generated.resources.location_mock_district
import com.domatapp.core.resource.generated.resources.location_mock_neighborhood
import com.domatapp.core.resource.generated.resources.location_neighborhood_label
import com.domatapp.core.resource.generated.resources.location_selection_title
import com.domatapp.feature.auth.presentation.location.LocationSelectionIntent
import com.domatapp.feature.auth.presentation.location.LocationSelectionUiState
import com.domatapp.feature.auth.presentation.screen.component.LocationCard
import com.domatapp.feature.auth.presentation.screen.component.LocationCardConnector
import dev.gezgin.core.annotation.Screen
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Konum seçimi. Core bileşenlerle: ScreenHeader, DomatDropdown, BottomActionBar/PrimaryButton.
 * LocationCard + LocationCardConnector feature-yereldir (katalogda değil; Akış A kartıyla karara bağlanacak).
 */
@Screen(AuthGraph.LocationSelectionRoute::class)
@Composable
fun ColumnScope.LocationSelectionScreen(
    uiState: LocationSelectionUiState,
    onIntent: (LocationSelectionIntent) -> Unit,
) {
    ScreenHeader(
        title = stringResource(Res.string.location_selection_title),
        onBackClick = { onIntent(LocationSelectionIntent.GoBack) },
    )
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(MaterialTheme.spacing.sp4),
    ) {
        LocationCard(
            label = stringResource(Res.string.location_district_label),
            value = stringResource(Res.string.location_mock_district),
            isLocked = true,
            cardAlpha = 0.6f,
        )
        LocationCardConnector()
        LocationCard(
            label = stringResource(Res.string.location_neighborhood_label),
            value = stringResource(Res.string.location_mock_neighborhood),
            isLocked = true,
            cardAlpha = 0.8f,
        )
        LocationCardConnector()
        LocationCard(
            label = stringResource(Res.string.location_building_label),
            value = stringResource(Res.string.location_mock_building),
            isLocked = true,
        )
        LocationCardConnector()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp4),
        ) {
            DomatDropdown(
                label = stringResource(Res.string.location_block_label),
                items = uiState.blockItems,
                selectedItem = uiState.selectedBlock,
                onItemSelected = { onIntent(LocationSelectionIntent.SelectBlock(it)) },
                modifier = Modifier.weight(1f),
            )
            DomatDropdown(
                label = stringResource(Res.string.location_apartment_label),
                items = uiState.apartmentItems,
                selectedItem = uiState.selectedApartment,
                onItemSelected = { onIntent(LocationSelectionIntent.SelectApartment(it)) },
                modifier = Modifier.weight(1f),
            )
        }
    }
    BottomActionBar(modifier = Modifier.imePadding()) {
        PrimaryButton(
            text = stringResource(Res.string.button_continue),
            onClick = { onIntent(LocationSelectionIntent.Confirm) },
            modifier = Modifier.fillMaxWidth(),
            size = ButtonSize.Large,
            enabled = uiState.isConfirmEnabled,
            trailingContent = {
                // Renk butonun içerik renginden (LocalContentColor) gelir; pasifte %38'e iner.
                Icon(painter = painterResource(Res.drawable.ic_arrow_forward), contentDescription = null, modifier = Modifier.size(20.dp))
            },
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LocationSelectionScreenPreview() {
    DomatTheme {
        Column {
            LocationSelectionScreen(uiState = LocationSelectionUiState(), onIntent = {})
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LocationSelectionScreenFilledPreview() {
    DomatTheme {
        Column {
            LocationSelectionScreen(
                uiState = LocationSelectionUiState(selectedBlock = "A2", selectedApartment = "5", isConfirmEnabled = true),
                onIntent = {},
            )
        }
    }
}
