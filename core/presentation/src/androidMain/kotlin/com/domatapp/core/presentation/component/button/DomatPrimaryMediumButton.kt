package com.domatapp.core.presentation.component.button

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.domatapp.core.design.theme.DomatTheme
import com.domatapp.core.resource.R

@Composable
fun DomatPrimaryMediumButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.malachite),
            contentColor = colorResource(R.color.slate_900),
            disabledContainerColor = colorResource(R.color.slate_100),
            disabledContentColor = colorResource(R.color.cool_gray_400),
        ),
    ) {
        Text(text = text, style = MaterialTheme.typography.titleLarge)
    }
}

@Preview(showBackground = true)
@Composable
private fun DomatPrimaryMediumButtonPreview() {
    DomatTheme {
        DomatPrimaryMediumButton(text = "Devam Et", onClick = {})
    }
}
