package com.domatapp.core.design.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.design.theme.spacing

@Preview(showBackground = true, name = "Static Light")
@Composable
private fun DomatThemeStaticLightPreview() {
    DomatTheme(darkTheme = false, dynamicColor = false) {
        ThemeShowcase()
    }
}

@Preview(showBackground = true, name = "Static Dark")
@Composable
private fun DomatThemeStaticDarkPreview() {
    DomatTheme(darkTheme = true, dynamicColor = false) {
        ThemeShowcase()
    }
}

@Preview(showBackground = true, name = "Dynamic Light")
@Composable
private fun DomatThemeDynamicLightPreview() {
    DomatTheme(darkTheme = false, dynamicColor = true) {
        ThemeShowcase()
    }
}

@Preview(showBackground = true, name = "Dynamic Dark")
@Composable
private fun DomatThemeDynamicDarkPreview() {
    DomatTheme(darkTheme = true, dynamicColor = true) {
        ThemeShowcase()
    }
}

@Composable
private fun ThemeShowcase() {
    Surface {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.sp4),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp2),
        ) {
            Text("Display Small", style = MaterialTheme.typography.displaySmall)
            Text("Headline Medium", style = MaterialTheme.typography.headlineMedium)
            Text("Title Large", style = MaterialTheme.typography.titleLarge)
            Text("Body Medium", style = MaterialTheme.typography.bodyMedium)
            Text("Label Small", style = MaterialTheme.typography.labelSmall)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sp1),
            ) {
                ColorSwatch("Primary", MaterialTheme.colorScheme.primary)
                ColorSwatch("Secondary", MaterialTheme.colorScheme.secondary)
                ColorSwatch("Tertiary", MaterialTheme.colorScheme.tertiary)
                ColorSwatch("Error", MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun ColorSwatch(label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color, MaterialTheme.shapes.small),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
