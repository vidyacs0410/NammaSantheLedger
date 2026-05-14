package com.namma.santhe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.namma.santhe.ui.theme.DueRedBg
import com.namma.santhe.ui.theme.DueRedText
import com.namma.santhe.ui.theme.SettledGreenBg
import com.namma.santhe.ui.theme.SettledGreenText
import java.util.Locale

@Composable
fun DueBadge(
    amount: Double,
    modifier: Modifier = Modifier
) {
    val isDue = amount > 0.0
    val bgColor = if (isDue) DueRedBg else SettledGreenBg
    val textColor = if (isDue) DueRedText else SettledGreenText
    val label = if (isDue) {
        "₹${String.format(Locale.getDefault(), "%.2f", amount)}"
    } else {
        "Settled ✓"
    }

    Text(
        text = label,
        color = textColor,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    )
}
