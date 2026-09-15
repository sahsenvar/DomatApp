package com.domatapp.feature.auth.presentation.screen.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import com.domatapp.core.design.theme.DomatColors
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.generated.resources.Res
import com.domatapp.core.resource.generated.resources.cd_google_icon
import org.jetbrains.compose.resources.stringResource

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    iconPainter: Painter,
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(12.dp),
        color = DomatColors.White,
        border = BorderStroke(1.dp, DomatColors.Slate200),
        shadowElevation = 1.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                painter = iconPainter,
                contentDescription = stringResource(Res.string.cd_google_icon),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 20.dp)
                    .size(24.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = DomatColors.Slate900,
            )
        }
    }
}

@Preview
@Composable
private fun GoogleSignInButtonPreview() {
    DomatTheme {
        GoogleSignInButton(
            onClick = {},
            iconPainter = ColorPainter(Color.Gray),
            text = "Google ile Devam Et",
        )
    }
}
