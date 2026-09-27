package com.domatapp.feature.checkout.presentation.screen.confirmation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.design.theme.spacing
import com.domatapp.core.navigation.CheckoutGraph
import com.domatapp.core.presentation.component.bar.BottomActionBar
import com.domatapp.core.presentation.component.button.GhostButton
import com.domatapp.core.presentation.component.button.SecondaryButton
import com.domatapp.core.presentation.component.community.CommunityProgressCard
import com.domatapp.core.presentation.component.data.InfoRow
import com.domatapp.core.presentation.component.data.SummaryRow
import com.domatapp.core.presentation.component.feedback.InlineNote
import com.domatapp.core.presentation.component.feedback.NoteTone
import com.domatapp.core.presentation.component.feedback.StatusHero
import com.domatapp.core.presentation.component.feedback.StatusTone
import com.domatapp.core.presentation.component.layout.FormSection
import com.domatapp.core.resource.generated.resources.Res
import com.domatapp.core.resource.generated.resources.c5_back_to_market
import com.domatapp.core.resource.generated.resources.c5_blocked_amount
import com.domatapp.core.resource.generated.resources.c5_community_title
import com.domatapp.core.resource.generated.resources.c5_delivery_code
import com.domatapp.core.resource.generated.resources.c5_delivery_day
import com.domatapp.core.resource.generated.resources.c5_delivery_title
import com.domatapp.core.resource.generated.resources.c5_payment_title
import com.domatapp.core.resource.generated.resources.c5_provision_reminder
import com.domatapp.core.resource.generated.resources.c5_share_whatsapp
import com.domatapp.core.resource.generated.resources.c5_subtitle
import com.domatapp.core.resource.generated.resources.c5_title_addition
import com.domatapp.core.resource.generated.resources.c5_title_first
import com.domatapp.core.resource.generated.resources.ic_share
import com.domatapp.feature.checkout.domain.model.Money
import com.domatapp.feature.checkout.presentation.confirmation.OrderConfirmationIntent
import com.domatapp.feature.checkout.presentation.confirmation.OrderConfirmationUiState
import com.domatapp.feature.checkout.presentation.format.formatTry
import dev.gezgin.core.annotation.Screen
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * C5 - Sipariş Onay (design/screens/C5). No header and no back (`noBack`). Scrolling body
 * (`StatusHero` → delivery → payment → community) + fixed [BottomActionBar].
 */
@Screen(CheckoutGraph.OrderConfirmationRoute::class)
@Composable
fun ColumnScope.OrderConfirmationScreen(
    uiState: OrderConfirmationUiState,
    onIntent: (OrderConfirmationIntent) -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(MaterialTheme.spacing.sp4),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp4),
    ) {
        StatusHero(
            tone = StatusTone.Success,
            title = stringResource(if (uiState.isAddition) Res.string.c5_title_addition else Res.string.c5_title_first),
            subtitle = stringResource(Res.string.c5_subtitle),
        )
        FormSection(title = stringResource(Res.string.c5_delivery_title)) {
            InfoRow(label = stringResource(Res.string.c5_delivery_code), value = uiState.deliveryCode)
            InfoRow(label = stringResource(Res.string.c5_delivery_day), value = uiState.deliveryDay)
        }
        FormSection(title = stringResource(Res.string.c5_payment_title)) {
            SummaryRow(
                label = stringResource(Res.string.c5_blocked_amount),
                amount = uiState.blockedAmount.formatTry(),
                emphasis = true,
            )
            InlineNote(text = stringResource(Res.string.c5_provision_reminder), tone = NoteTone.Neutral)
        }
        CommunityProgressCard(
            title = stringResource(Res.string.c5_community_title),
            progress = uiState.communityProgress,
            caption = uiState.communityCaption,
        )
    }
    BottomActionBar {
        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp3)) {
            if (uiState.showShare) {
                SecondaryButton(
                    text = stringResource(Res.string.c5_share_whatsapp),
                    onClick = { onIntent(OrderConfirmationIntent.ShareWhatsAppClicked) },
                    // Generic share icon - no brand logo is drawn (design/components.yaml → SecondaryButton).
                    leadingIcon = painterResource(Res.drawable.ic_share),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            GhostButton(
                text = stringResource(Res.string.c5_back_to_market),
                onClick = { onIntent(OrderConfirmationIntent.BackToMarketClicked) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ── Previews: one per design state (design/screens/C5/states/*.png) ─────────────────────────────

/** Fake states built from `card.yaml → sampleData` / `structure.json`. Preview-only. */
private object C5PreviewStates {
    val firstOrder = OrderConfirmationUiState(
        deliveryCode = "#DAL1",
        deliveryDay = "Cumartesi, 3 Ekim",
        blockedAmount = Money.ofLira(188),
        communityProgress = 0.5f,
        communityCaption = "Bir sonraki kademeye 12 sipariş kaldı — kg fiyatı ₺2 daha düşecek.",
    )

    /** Second order in the window: combined community effect (75 %), no share button. */
    val addition = firstOrder.copy(isAddition = true, communityProgress = 0.75f)
}

@Composable
private fun C5Preview(state: OrderConfirmationUiState) {
    DomatTheme(darkTheme = false) {
        Column {
            OrderConfirmationScreen(uiState = state, onIntent = {})
        }
    }
}

/** Height = content (annotations: the body scrolls; the frame hugs its content). */
@Preview(name = "C5@first-order", widthDp = 390, heightDp = 911)
@Composable
private fun C5FirstOrderPreview() = C5Preview(C5PreviewStates.firstOrder)

@Preview(name = "C5@addition", widthDp = 390, heightDp = 851)
@Composable
private fun C5AdditionPreview() = C5Preview(C5PreviewStates.addition)
