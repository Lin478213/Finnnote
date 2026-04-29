package com.example.y.ui.component.glass

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

val LiquidGlassTextFieldShape = RoundedCornerShape(18.dp)

@Composable
fun liquidGlassOutlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedBorderColor = Color.White.copy(alpha = 0.92f),
    unfocusedBorderColor = Color.White.copy(alpha = 0.58f),
    disabledBorderColor = Color.White.copy(alpha = 0.32f),
    focusedContainerColor = Color.White.copy(alpha = 0.22f),
    unfocusedContainerColor = Color.White.copy(alpha = 0.14f),
    disabledContainerColor = Color.White.copy(alpha = 0.08f),
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = MaterialTheme.colorScheme.primary,
)
