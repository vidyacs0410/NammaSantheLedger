package com.namma.santhe.ui.screens.entry

import androidx.compose.animation.*
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.namma.santhe.R
import com.namma.santhe.ui.components.CustomerCard
import com.namma.santhe.ui.components.NumericKeypad
import com.namma.santhe.ui.theme.*
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import android.speech.RecognizerIntent
import android.app.Activity
import com.namma.santhe.utils.VoiceUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickEntryScreen(
    onNavigateBack: () -> Unit,
    viewModel: EntryViewModel = hiltViewModel()
) {
    val selectedCustomer by viewModel.selectedCustomer.collectAsStateWithLifecycle()
    val entryAmount by viewModel.entryAmount.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val customerDues by viewModel.customerDues.collectAsStateWithLifecycle()
    val appLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var noteText by remember { mutableStateOf("") }
    var showNewCustomerSheet by remember { mutableStateOf(false) }

    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = data?.firstOrNull() ?: ""
            if (text.isNotBlank()) {
                processVoiceCommand(text, viewModel) { parsedNote ->
                    noteText = parsedNote
                }
            }
        }
    }

    val launchVoice = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, getSpeechLanguageCode(appLanguage))
        }
        try {
            voiceLauncher.launch(intent)
        } catch (e: Exception) {
            // Ignore if no speech recognizer available
        }
    }

    // Collect UI events
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Success -> {
                    noteText = ""
                    snackbarHostState.showSnackbar("Saved! / ಉಳಿಸಲಾಗಿದೆ")
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
                    Text(
                        text = if (selectedCustomer == null)
                            stringResource(R.string.select_customer)
                        else
                            stringResource(R.string.step_amount_entry),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = selectedCustomer,
                transitionSpec = {
                    fadeIn() + slideInHorizontally() togetherWith
                            fadeOut() + slideOutHorizontally()
                },
                label = "entry_step"
            ) { customer ->
                if (customer == null) {
                    // Step 1: Customer Selection
                    CustomerSelectionStep(
                        customers = customers,
                        customerDues = customerDues,
                        searchQuery = searchQuery,
                        onSearchChange = viewModel::onSearchQueryChange,
                        onSelectCustomer = viewModel::selectCustomer,
                        onNewCustomer = { showNewCustomerSheet = true }
                    )
                } else {
                    // Step 2: Amount Entry
                    AmountEntryStep(
                        customer = customer,
                        entryAmount = entryAmount,
                        noteText = noteText,
                        isLoading = isLoading,
                        onNoteChange = { noteText = it },
                        onClearCustomer = viewModel::clearCustomer,
                        onDigit = viewModel::appendDigit,
                        onBackspace = viewModel::clearLast,
                        onDot = { viewModel.appendDigit(".") },
                        onSubmitUdari = { viewModel.submitUdari(noteText) },
                        onSubmitPayment = { viewModel.submitPayment(noteText) },
                        onVoiceClick = launchVoice
                    )

                }
            }
        }
    }

    // New Customer Bottom Sheet
    if (showNewCustomerSheet) {
        NewCustomerBottomSheet(
            appLanguage = appLanguage,
            onDismiss = { showNewCustomerSheet = false },
            onAdd = { name, phone ->
                scope.launch {
                    viewModel.addNewCustomer(name, phone)
                    showNewCustomerSheet = false
                }
            }
        )
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

@Composable
private fun CustomerSelectionStep(
    customers: List<com.namma.santhe.data.model.Customer>,
    customerDues: Map<Int, Double>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSelectCustomer: (com.namma.santhe.data.model.Customer) -> Unit,
    onNewCustomer: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // New Customer Button
        TextButton(
            onClick = onNewCustomer,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(Icons.Filled.PersonAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.new_customer),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text(stringResource(R.string.search_customers)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Customer list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(customers, key = { it.id }) { customer ->
                CustomerCard(
                    customer = customer,
                    dueAmount = customerDues[customer.id] ?: 0.0,
                    onClick = { onSelectCustomer(customer) },
                    compact = true
                )
            }
        }
    }
}

@Composable
private fun AmountEntryStep(
    customer: com.namma.santhe.data.model.Customer,
    entryAmount: String,
    noteText: String,
    isLoading: Boolean,
    onNoteChange: (String) -> Unit,
    onClearCustomer: () -> Unit,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onDot: () -> Unit,
    onSubmitUdari: () -> Unit,
    onSubmitPayment: () -> Unit,
    onVoiceClick: () -> Unit
) {
    val amountValid = entryAmount.toDoubleOrNull()?.let { it > 0 } ?: false

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Selected customer chip
        AssistChip(
            onClick = onClearCustomer,
            label = {
                Text(customer.name, fontWeight = FontWeight.SemiBold)
            },
            trailingIcon = {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Clear",
                    modifier = Modifier.size(16.dp)
                )
            },
            shape = RoundedCornerShape(20.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Amount display
        Text(
            text = "₹${entryAmount.ifEmpty { "0" }}",
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        if (!amountValid && entryAmount.isNotEmpty()) {
            Text(
                text = stringResource(R.string.error_empty_amount),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Numeric Keypad
        NumericKeypad(
            onDigit = onDigit,
            onBackspace = onBackspace,
            onDot = onDot
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = noteText,
            onValueChange = onNoteChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.what_did_you_sell)) },
            trailingIcon = {
                IconButton(onClick = onVoiceClick) {
                    Icon(Icons.Filled.Mic, contentDescription = "Voice Input")
                }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Action Buttons
        Button(
            onClick = onSubmitUdari,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = amountValid && !isLoading,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ErrorRed,
                contentColor = TextOnPrimary
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = TextOnPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = stringResource(R.string.add_udari),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onSubmitPayment,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = amountValid && !isLoading,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = ForestGreen
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(ForestGreen)
            )
        ) {
            Text(
                text = stringResource(R.string.record_payment),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewCustomerBottomSheet(
    appLanguage: String,
    onDismiss: () -> Unit,
    onAdd: (name: String, phone: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    val nameSpeechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = data?.firstOrNull() ?: ""
            if (text.isNotBlank()) {
                name = text
                nameError = false
            }
        }
    }

    val phoneSpeechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = data?.firstOrNull() ?: ""
            if (text.isNotBlank()) {
                phone = VoiceUtils.normalizePhoneNumber(text)
            }
        }
    }

    val launchSpeech = { launcher: androidx.activity.result.ActivityResultLauncher<Intent> ->
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, getSpeechLanguageCode(appLanguage))
        }
        try {
            launcher.launch(intent)
        } catch (e: Exception) {
            // Ignore if no speech recognizer available
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.add_customer),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                label = { Text(stringResource(R.string.customer_name)) },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Name") },
                trailingIcon = {
                    IconButton(onClick = { launchSpeech(nameSpeechLauncher) }) {
                        Icon(Icons.Filled.Mic, contentDescription = "Speak Name")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = nameError,
                supportingText = if (nameError) {
                    { Text(stringResource(R.string.error_empty_name)) }
                } else null
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(stringResource(R.string.phone_number)) },
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = "Phone") },
                trailingIcon = {
                    IconButton(onClick = { launchSpeech(phoneSpeechLauncher) }) {
                        Icon(Icons.Filled.Mic, contentDescription = "Speak Phone")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    } else {
                        onAdd(name.trim(), phone.trim())
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.add_customer),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun processVoiceCommand(
    text: String,
    viewModel: EntryViewModel,
    onNoteExtracted: (String) -> Unit
) {
    val normalizedText = VoiceUtils.normalizeDigits(text)
    val lowerText = normalizedText.lowercase()
    
    // Pattern for "Add [amount] for [note]" or "Add [amount]"
    // English: add, plus, sent, put
    // Kannada: ಸೇರಿಸು (serisu), ಹಾಕು (haku)
    // Hindi: जोड़ो (jodo), डालो (dalo)
    val addRegex = Regex("(?:add|plus|sent|put|ಸೇರಿಸು|ಹಾಕು|ಜೋಡೋ|ಡಾಲೋ|ಸೇರಿಸಿ|ಜೋಡಿಸಿ|ಹಲವು)\\s+(\\d+(?:\\.\\d+)?)(?:\\s+(?:for|ಗಾಗಿ|ಕ್ಕೆ|ಕೆ|ಕೋ|ಕೆ|ಲಿ|ಗೆ)\\s+(.+))?")
    
    // Pattern for "Receive [amount]" or "Paid [amount]" or "Got [amount]"
    // English: receive, paid, got, received, collected
    // Kannada: ತಗೋ (tago), ಪಾವತಿ (pavati), ಬಂತು (bantu)
    val receiveRegex = Regex("(?:receive|paid|got|received|collected|ತಗೋ|ಪಾವತಿ|ಬಂತು|ಸ್ವೀಕರಿಸು|ಖಾತೆ|ಮಿಲನ|ಮಿಲನ್)\\s+(\\d+(?:\\.\\d+)?)")

    val addMatch = addRegex.find(lowerText)
    val receiveMatch = receiveRegex.find(lowerText)

    when {
        addMatch != null -> {
            val amount = addMatch.groupValues[1]
            val note = addMatch.groupValues.getOrNull(2)?.trim() ?: ""
            viewModel.entryAmount.value = amount
            onNoteExtracted(note)
            viewModel.submitUdari(note)
        }
        receiveMatch != null -> {
            val amount = receiveMatch.groupValues[1]
            viewModel.entryAmount.value = amount
            onNoteExtracted("Payment")
            viewModel.submitPayment("Payment")
        }
        else -> {
            // Just treat as text for the note field
            onNoteExtracted(text)
        }
    }
}
