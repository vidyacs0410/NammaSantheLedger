package com.namma.santhe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.namma.santhe.data.model.Transaction
import com.namma.santhe.ui.theme.DueRedText
import com.namma.santhe.ui.theme.SettledGreenText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionItem(
    transaction: Transaction,
    modifier: Modifier = Modifier,
    onLongPress: (() -> Unit)? = null
) {
    val isUdari = transaction.type == "UDARI"
    val barColor = if (isUdari) DueRedText else SettledGreenText
    val amountColor = if (isUdari) DueRedText else SettledGreenText
    val prefix = if (isUdari) "+" else "-"
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val timeStr = timeFormat.format(Date(transaction.date))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Colored vertical bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(44.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(barColor)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Note and time
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.note.ifEmpty {
                    if (isUdari) "Udari" else "Payment"
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = timeStr,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Amount
        Text(
            text = "$prefix₹${String.format(Locale.getDefault(), "%.2f", transaction.amount)}",
            color = amountColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
