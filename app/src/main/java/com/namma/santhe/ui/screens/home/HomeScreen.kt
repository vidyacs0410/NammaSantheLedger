package com.namma.santhe.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.namma.santhe.R
import com.namma.santhe.ui.components.CustomerCard
import com.namma.santhe.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToEntry: (Int?) -> Unit,
    onNavigateToCustomer: (Int) -> Unit,
    onNavigateToSummary: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val customers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val totalOutstanding by viewModel.totalOutstanding.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val customerDues by viewModel.customerDues.collectAsStateWithLifecycle()
    val appLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val matches = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                viewModel.onSearchQueryChange(matches[0])
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.home_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Profile"
                        )
                    }
                    IconButton(onClick = onNavigateToSummary) {
                        Icon(
                            Icons.Filled.BarChart,
                            contentDescription = stringResource(R.string.daily_summary)
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToEntry(null) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add entry")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Summary Cards Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = stringResource(R.string.total_outstanding),
                    value = "₹${String.format(Locale.getDefault(), "%.2f", totalOutstanding)}",
                    icon = Icons.Filled.AccountBalanceWallet,
                    gradient = Brush.horizontalGradient(
                        colors = if (totalOutstanding > 0) listOf(DueRedBg, Color.White) else listOf(SettledGreenBg, Color.White)
                    ),
                    valueColor = if (totalOutstanding > 0) DueRedText else SettledGreenText,
                    modifier = Modifier.weight(1.1f)
                )

                SummaryCard(
                    title = stringResource(R.string.customers_label),
                    value = "${customers.size}",
                    icon = Icons.Filled.People,
                    gradient = Brush.horizontalGradient(
                        colors = listOf(CollectedBlueBg, Color.White)
                    ),
                    valueColor = CollectedBlue,
                    modifier = Modifier.weight(0.9f)
                )
            }

            // Floating Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.search_customers)) },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, getSpeechLanguageCode(appLanguage))
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to search...")
                            }
                            try {
                                speechRecognizerLauncher.launch(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Voice search not supported on this device", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Filled.Mic, contentDescription = "Voice Search", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Customer List or Empty State
            if (customers.isEmpty()) {
                if (searchQuery.isBlank()) {
                    // Initial empty state (no customers in DB)
                    EmptyStateContent(
                        onAddCustomer = { onNavigateToEntry(null) }
                    )
                } else {
                    // Search returned no results
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.customer_not_found),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(customers, key = { it.id }) { customer ->
                        CustomerCard(
                            customer = customer,
                            dueAmount = customerDues[customer.id] ?: 0.0,
                            onClick = { onNavigateToCustomer(customer.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    gradient: Brush,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = valueColor.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = valueColor
                )
            }
        }
    }
}

@Composable
private fun EmptyStateContent(
    onAddCustomer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Simple market stall icon drawn with Canvas
        Canvas(
            modifier = Modifier.size(120.dp)
        ) {
            val w = size.width
            val h = size.height
            val stallColor = Color(0xFFE65100)
            val roofColor = Color(0xFFFF833A)

            // Roof (triangle)
            val roofPath = Path().apply {
                moveTo(w * 0.1f, h * 0.4f)
                lineTo(w * 0.5f, h * 0.1f)
                lineTo(w * 0.9f, h * 0.4f)
                close()
            }
            drawPath(roofPath, roofColor)

            // Counter (rectangle)
            drawRect(
                color = stallColor,
                topLeft = Offset(w * 0.15f, h * 0.4f),
                size = androidx.compose.ui.geometry.Size(w * 0.7f, h * 0.25f)
            )

            // Legs
            drawLine(
                color = stallColor,
                start = Offset(w * 0.2f, h * 0.65f),
                end = Offset(w * 0.2f, h * 0.9f),
                strokeWidth = 6f
            )
            drawLine(
                color = stallColor,
                start = Offset(w * 0.8f, h * 0.65f),
                end = Offset(w * 0.8f, h * 0.9f),
                strokeWidth = 6f
            )

            // Items on counter (small circles)
            drawCircle(color = Color(0xFF4CAF50), radius = w * 0.06f, center = Offset(w * 0.35f, h * 0.48f))
            drawCircle(color = Color(0xFFFFC107), radius = w * 0.05f, center = Offset(w * 0.5f, h * 0.5f))
            drawCircle(color = Color(0xFFF44336), radius = w * 0.055f, center = Offset(w * 0.65f, h * 0.47f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.no_customers_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.no_customers_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAddCustomer,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.add_first_customer))
        }
    }
}

private fun getSpeechLanguageCode(appLang: String): String {
    return when (appLang) {
        "kn" -> "kn-IN"
        "te" -> "te-IN"
        "ta" -> "ta-IN"
        "hi" -> "hi-IN"
        else -> "en-IN"
    }
}
