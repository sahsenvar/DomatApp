package com.domatapp.core.presentation.component.input

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.R

@Composable
fun DomatInputDropdown(
    label: String,
    value: String,
    iconPainter: Painter,
    chevronPainter: Painter,
    checkmarkPainter: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    isOpen: Boolean = false,
    items: List<String> = emptyList(),
    selectedItem: String? = null,
    onItemSelected: (String) -> Unit = {},
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (isOpen) 180f else 0f,
        label = "chevronRotation",
    )

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            color = colorResource(R.color.white),
            border = BorderStroke(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) colorResource(R.color.malachite) else colorResource(R.color.slate_200),
            ),
            shadowElevation = 1.dp,
        ) {
            Column(
                modifier = Modifier.padding(if (isActive) 18.dp else 17.dp),
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
                            if (isActive) colorResource(R.color.malachite) else colorResource(R.color.slate_500),
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
                            if (isActive) colorResource(R.color.malachite) else colorResource(R.color.slate_400),
                        ),
                    )
                }
            }
        }

        if (isOpen && items.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(10f)
                    .padding(top = 88.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = Color.Black.copy(alpha = 0.1f),
                        spotColor = Color.Black.copy(alpha = 0.1f),
                    ),
                shape = RoundedCornerShape(12.dp),
                color = colorResource(R.color.white),
                border = BorderStroke(2.dp, colorResource(R.color.malachite)),
            ) {
                Column(modifier = Modifier.padding(2.dp)) {
                    items.forEachIndexed { index, item ->
                        val isSelected = item == selectedItem
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) colorResource(R.color.malachite_10)
                                    else colorResource(R.color.white),
                                )
                                .clickable { onItemSelected(item) }
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyLarge,
                                color = colorResource(R.color.slate_900),
                            )
                            if (isSelected) {
                                Image(
                                    painter = checkmarkPainter,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    colorFilter = ColorFilter.tint(colorResource(R.color.malachite)),
                                )
                            }
                        }
                        if (index < items.lastIndex) {
                            HorizontalDivider(color = colorResource(R.color.slate_100), thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DomatInputDropdownPreview() {
    DomatTheme {
        DomatInputDropdown(
            label = "Blok No",
            value = "A1",
            iconPainter = ColorPainter(Color.Gray),
            chevronPainter = ColorPainter(Color.Gray),
            checkmarkPainter = ColorPainter(Color.Green),
            onClick = {},
        )
    }
}
