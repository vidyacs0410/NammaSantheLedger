package com.namma.santhe.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.namma.santhe.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeNumberScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val oldNumber = viewModel.sessionManager.getUserPhone()
    var newNumber by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var showOtpDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.change_number)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Visual transition: Old Phone -> New Phone
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                PhoneVisual(label = stringResource(R.string.old_number))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                PhoneVisual(label = stringResource(R.string.new_number), isNew = true)
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Migration Info
            Text(
                text = stringResource(R.string.migration_info),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.sms_info),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.otp_send_info, oldNumber),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = newNumber,
                onValueChange = { if (it.length <= 10) newNumber = it },
                label = { Text(stringResource(R.string.enter_new_number)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    if (newNumber.length == 10) {
                        showOtpDialog = true
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please enter a valid 10-digit number")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1B5E20) // Green as in screenshot
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.verify_mobile), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showOtpDialog) {
        AlertDialog(
            onDismissRequest = { showOtpDialog = false },
            title = { Text("Verify OTP") },
            text = {
                Column {
                    Text("Enter the 4-digit OTP sent to $oldNumber")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { if (it.length <= 4) otp = it },
                        label = { Text("OTP (Hint: 1234)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (otp == "1234") {
                            showOtpDialog = false
                            isLoading = true
                            scope.launch {
                                delay(1500) // Simulate network
                                viewModel.sessionManager.saveLoginSession(
                                    userId = 1, // Assume same user
                                    name = viewModel.sessionManager.getUserName(),
                                    phone = newNumber
                                )
                                isLoading = false
                                snackbarHostState.showSnackbar(context.getString(R.string.number_updated))
                                delay(1000)
                                onNavigateBack()
                            }
                        }
                    }
                ) {
                    Text("Verify")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOtpDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PhoneVisual(label: String, isNew: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(width = 80.dp, height = 140.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(2.dp, if (isNew) Color(0xFF1B5E20) else Color.Gray, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.SimCard,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = if (isNew) Color(0xFF1B5E20) else Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color.LightGray, CircleShape)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
