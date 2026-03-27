package com.domatapp.feature.auth.presentation.screen.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.R

@Composable
fun InputDropdown(
    label: String,
    value: String,
    iconPainter: Painter,
    chevronPainter: Painter,
    checkmarkPainter: Painter,
    modifier: Modifier = Modifier,
    items: List<String> = emptyList(),
    selectedItem: String? = null,
    onItemSelected: (String) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    var triggerWidthDp by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "chevronRotation",
    )

    val borderColor = if (expanded) colorResource(R.color.malachite) else colorResource(R.color.slate_200)
    val borderWidth = if (expanded) 2.dp else 1.dp

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { size ->
                    triggerWidthDp = with(density) { size.width.toDp() }
                }
                .shadow(1.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(colorResource(R.color.white))
                .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
                .clickable { expanded = !expanded }
                .padding(if (expanded) 18.dp else 17.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = iconPainter,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    colorFilter = ColorFilter.tint(
                        if (expanded) colorResource(R.color.malachite) else colorResource(R.color.slate_500),
                    ),
                )
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = colorResource(R.color.slate_500),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = colorResource(R.color.slate_900),
                    modifier = Modifier.weight(1f),
                )
                Image(
                    painter = chevronPainter,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp).rotate(chevronRotation),
                    colorFilter = ColorFilter.tint(
                        if (expanded) colorResource(R.color.malachite) else colorResource(R.color.slate_400),
                    ),
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(triggerWidthDp),
            shape = RoundedCornerShape(12.dp),
            containerColor = colorResource(R.color.white),
            shadowElevation = 8.dp,
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = item == selectedItem
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyLarge,
                            color = colorResource(R.color.slate_900),
                        )
                    },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    },
                    trailingIcon = if (isSelected) {
                        {
                            Image(
                                painter = checkmarkPainter,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                colorFilter = ColorFilter.tint(colorResource(R.color.malachite)),
                            )
                        }
                    } else null,
                    modifier = Modifier.background(
                        if (isSelected) colorResource(R.color.malachite_10)
                        else Color.Transparent,
                    ),
                    colors = MenuDefaults.itemColors(),
                )
                if (index < items.lastIndex) {
                    HorizontalDivider(color = colorResource(R.color.slate_100), thickness = 1.dp)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InputDropdownPreview() {
    DomatTheme {
        InputDropdown(
            label = "Blok No",
            value = "A1",
            iconPainter = ColorPainter(Color.Gray),
            chevronPainter = ColorPainter(Color.Gray),
            checkmarkPainter = ColorPainter(Color.Green),
        )
    }
}
