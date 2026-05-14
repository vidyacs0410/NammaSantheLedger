package com.namma.santhe.ui.screens.summary

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.namma.santhe.R
import com.namma.santhe.ui.theme.*
import com.namma.santhe.utils.PdfUtils
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailySummaryScreen(
    onNavigateBack: () -> Unit,
    viewModel: SummaryViewModel = hiltViewModel()
) {
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val isMonthly by viewModel.isMonthly.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val dateStr = remember(isMonthly, selectedDate, selectedMonth) {
        if (isMonthly) {
            val sdf = SimpleDateFormat("MMMM yyyy", Locale("kn", "IN"))
            val date = Date.from(selectedMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant())
            sdf.format(date)
        } else {
            val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("kn", "IN"))
            val date = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
            sdf.format(date)
        }
    }

    val proverbs = listOf(
        stringResource(R.string.proverb_1),
        stringResource(R.string.proverb_2),
        stringResource(R.string.proverb_3),
        stringResource(R.string.proverb_4),
        stringResource(R.string.proverb_5)
    )
    val randomProverb = remember { proverbs.random() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.daily_summary),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Toggle Tabs
            TabRow(
                selectedTabIndex = if (isMonthly) 1 else 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = !isMonthly,
                    onClick = { viewModel.setMonthly(false) },
                    text = { Text("Daily") }
                )
                Tab(
                    selected = isMonthly,
                    onClick = { viewModel.setMonthly(true) },
                    text = { Text("Monthly") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { if (isMonthly) viewModel.previousMonth() else viewModel.previousDate() }) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous")
                    }
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { if (isMonthly) viewModel.nextMonth() else viewModel.nextDate() }) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Next")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Summary Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Today's Sales
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.todays_sales),
                    amount = summary.totalSales,
                    bgColor = SettledGreenBg,
                    textColor = SettledGreenText
                )

                // Collected
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.collected),
                    amount = summary.totalCollected,
                    bgColor = CollectedBlueBg,
                    textColor = CollectedBlue
                )

                // Pending
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.pending),
                    amount = summary.totalPending,
                    bgColor = DueRedBg,
                    textColor = DueRedText
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Motivational proverb
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = randomProverb,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 28.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Share Text Button
                OutlinedButton(
                    onClick = {
                        shareSummary(context, sales = summary.totalSales, collected = summary.totalCollected, pending = summary.totalPending, isMonthly = isMonthly)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Text", fontWeight = FontWeight.Bold)
                }

                // Share PDF Button
                Button(
                    onClick = {
                        PdfUtils.generateAndSharePdf(
                            context = context,
                            title = if (isMonthly) "Monthly Summary" else "Daily Summary",
                            dateString = dateStr,
                            sales = summary.totalSales,
                            collected = summary.totalCollected,
                            pending = summary.totalPending
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share PDF", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: Double,
    bgColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "₹${String.format(Locale.getDefault(), "%.2f", amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

private fun shareSummary(
    context: Context,
    sales: Double,
    collected: Double,
    pending: Double,
    isMonthly: Boolean
) {
    val title = if (isMonthly) "ಈ ತಿಂಗಳ ಸಂತೆ ವರದಿ 📊" else "ಇಂದಿನ ಸಂತೆ ವರದಿ 📊"
    val text = """
        |$title
        |ಮಾರಾಟ: ₹${String.format(Locale.getDefault(), "%.2f", sales)}
        |ಸ್ವೀಕರಿಸಿದ: ₹${String.format(Locale.getDefault(), "%.2f", collected)}
        |ಬಾಕಿ: ₹${String.format(Locale.getDefault(), "%.2f", pending)}
    """.trimMargin()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share Summary"))
}
