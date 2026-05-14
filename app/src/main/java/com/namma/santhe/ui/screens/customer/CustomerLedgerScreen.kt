package com.namma.santhe.ui.screens.customer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.namma.santhe.data.model.Transaction
import com.namma.santhe.ui.components.DueBadge
import com.namma.santhe.ui.components.TransactionItem
import com.namma.santhe.ui.screens.entry.UiEvent
import com.namma.santhe.ui.theme.*
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerLedgerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEntry: (Int) -> Unit,
    viewModel: CustomerViewModel = hiltViewModel()
) {
    val customer by viewModel.customer.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val balance by viewModel.balance.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showDeleteTxnDialog by remember { mutableStateOf<Transaction?>(null) }
    var showDeleteCustomerDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Success -> {
                    // If customer was deleted, navigate back
                    if (customer == null) {
                        onNavigateBack()
                    }
                }
                is UiEvent.Error -> {
                    snackbarHostState.showSnackbar(event.msg)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = customer?.name ?: "",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        if (customer != null) {
                            DueBadge(amount = balance)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.send_whatsapp_reminder)) },
                                onClick = {
                                    showMenu = false
                                    customer?.let {
                                        sendWhatsAppReminder(context, it.name, it.phone, balance)
                                    }
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Share, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(R.string.delete_customer),
                                        color = ErrorRed
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showDeleteCustomerDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = null,
                                        tint = ErrorRed
                                    )
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            customer?.let {
                FloatingActionButton(
                    onClick = { onNavigateToEntry(it.id) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add entry")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Balance Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (balance > 0) DueRedBg else SettledGreenBg
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.net_balance),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${String.format(Locale.getDefault(), "%.2f", balance)}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (balance > 0) DueRedText else SettledGreenText
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sub labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val totalUdari = transactions
                            .filter { it.type == "UDARI" }
                            .sumOf { it.amount }
                        val totalPaid = transactions
                            .filter { it.type == "PAYMENT" }
                            .sumOf { it.amount }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.total_udari),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₹${String.format(Locale.getDefault(), "%.2f", totalUdari)}",
                                fontWeight = FontWeight.SemiBold,
                                color = DueRedText
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.total_paid),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₹${String.format(Locale.getDefault(), "%.2f", totalPaid)}",
                                fontWeight = FontWeight.SemiBold,
                                color = SettledGreenText
                            )
                        }
                    }
                }
            }

            // Transaction Timeline grouped by date
            val groupedTransactions = transactions.groupBy { txn ->
                getDateLabel(txn.date)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                groupedTransactions.forEach { (dateLabel, txns) ->
                    item(key = "header_$dateLabel") {
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(txns, key = { it.id }) { txn ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            onClick = { showDeleteTxnDialog = txn }
                        ) {
                            TransactionItem(
                                transaction = txn,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete Transaction Dialog
    showDeleteTxnDialog?.let { txn ->
        AlertDialog(
            onDismissRequest = { showDeleteTxnDialog = null },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(txn)
                        showDeleteTxnDialog = null
                    }
                ) {
                    Text(stringResource(R.string.delete), color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteTxnDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Delete Customer Dialog
    if (showDeleteCustomerDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteCustomerDialog = false },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCustomer()
                        showDeleteCustomerDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text(stringResource(R.string.delete), color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCustomerDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

private fun getDateLabel(epochMillis: Long): String {
    val cal = Calendar.getInstance()
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val yesterdayStart = (todayStart.clone() as Calendar).apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }

    cal.timeInMillis = epochMillis

    return when {
        cal.timeInMillis >= todayStart.timeInMillis -> "Today"
        cal.timeInMillis >= yesterdayStart.timeInMillis -> "Yesterday"
        else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(epochMillis))
    }
}

private fun sendWhatsAppReminder(context: Context, name: String, phone: String, balance: Double) {
    val message = "ನಮಸ್ಕಾರ $name, ನಿಮ್ಮ ಉಧಾರಿ ₹${String.format(Locale.getDefault(), "%.2f", balance)} ಇದೆ. ದಯವಿಟ್ಟು ಪಾವತಿಸಿ."
    val encodedMsg = URLEncoder.encode(message, "UTF-8")
    val url = "https://wa.me/91${phone}?text=$encodedMsg"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.whatsapp_not_found), Toast.LENGTH_SHORT).show()
    }
}
