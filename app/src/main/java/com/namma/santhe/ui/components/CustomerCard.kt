package com.namma.santhe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.namma.santhe.data.model.Customer
import kotlin.math.absoluteValue

@Composable
fun CustomerCard(
    customer: Customer,
    dueAmount: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val avatarColor = getAvatarColor(customer.name)
    val initial = customer.name.firstOrNull()?.uppercase() ?: "?"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (compact) 12.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar circle
            Box(
                modifier = Modifier
                    .size(if (compact) 40.dp else 48.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    color = Color.White,
                    fontSize = if (compact) 16.sp else 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name and phone
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 16.sp else 18.sp
                )
                if (!compact) {
                    Text(
                        text = customer.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }

            // Due badge
            DueBadge(amount = dueAmount)
        }
    }
}

private fun getAvatarColor(name: String): Color {
    val colors = listOf(
        Color(0xFFE65100), // Deep Orange
        Color(0xFF1B5E20), // Green
        Color(0xFF1565C0), // Blue
        Color(0xFF6A1B9A), // Purple
        Color(0xFFC62828), // Red
        Color(0xFF00838F), // Teal
        Color(0xFFEF6C00), // Orange
        Color(0xFF283593), // Indigo
    )
    val index = name.hashCode().absoluteValue % colors.size
    return colors[index]
}
