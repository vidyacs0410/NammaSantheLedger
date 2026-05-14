package com.namma.santhe.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.res.stringResource
import com.namma.santhe.R
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataSecurityScreen(
    type: String,
    onNavigateBack: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToAppLock: () -> Unit,
    onNavigateToPaymentPin: () -> Unit,
    onLogout: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var showCloudBackups by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var isBackingUp by remember { mutableStateOf(false) }
    var backupStatus by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                snackbarHostState.showSnackbar("Importing from: ${it.lastPathSegment}")
                isRestoring = true
                delay(2000)
                isRestoring = false
                snackbarHostState.showSnackbar("Data imported successfully!")
            }
        }
    }
    
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                isBackingUp = true
                backupStatus = "Preparing data for export..."
                delay(1500)
                isBackingUp = false
                snackbarHostState.showSnackbar("Data exported to ${it.lastPathSegment}")
            }
        }
    }

    if (showCloudBackups) {
        RestoreCloudDialog(
            onDismiss = { showCloudBackups = false },
            onRestore = { date ->
                showCloudBackups = false
                scope.launch {
                    isRestoring = true
                    delay(2500)
                    isRestoring = false
                    snackbarHostState.showSnackbar("Restored backup from $date")
                }
            }
        )
    }

    if (isRestoring || isBackingUp) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            title = { Text(if (isRestoring) stringResource(R.string.restoring) else backupStatus) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.data_security), fontWeight = FontWeight.Bold) },
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            InfoSection(
                title = stringResource(R.string.backup_restore),
                description = stringResource(R.string.backup_restore_desc)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (type == "all" || type == "backup") {
                // Backup Section
                SecurityCard(title = stringResource(R.string.backup_data)) {
                    SecurityActionItem(
                        icon = Icons.Filled.CloudUpload,
                        title = stringResource(R.string.backup_to_cloud),
                        subtitle = "Last backup: Never",
                        onClick = {
                            scope.launch {
                                isBackingUp = true
                                backupStatus = "Preparing backup..."
                                delay(1500)
                                backupStatus = "Uploading to cloud..."
                                delay(2000)
                                isBackingUp = false
                                snackbarHostState.showSnackbar("Backup completed successfully!")
                            }
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    SecurityActionItem(
                        icon = Icons.Filled.SdStorage,
                        title = stringResource(R.string.export_local_backup),
                        subtitle = stringResource(R.string.save_file_phone),
                        onClick = {
                            exportLauncher.launch("NammaSanthe_Backup_${System.currentTimeMillis()}.json")
                        }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (type == "all" || type == "restore") {
                // Restore Section
                SecurityCard(title = stringResource(R.string.restore_data)) {
                    SecurityActionItem(
                        icon = Icons.Filled.CloudDownload,
                        title = stringResource(R.string.restore_from_cloud),
                        subtitle = stringResource(R.string.download_saved_records),
                        onClick = {
                            showCloudBackups = true
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    SecurityActionItem(
                        icon = Icons.Filled.FileOpen,
                        title = stringResource(R.string.import_from_file),
                        subtitle = stringResource(R.string.select_backup_file),
                        onClick = {
                            filePickerLauncher.launch("*/*")
                        }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (type == "all" || type == "security") {
                // Account Security Section
                SecurityCard(title = stringResource(R.string.account_security)) {
                    SecurityActionItem(
                        icon = Icons.Filled.Fingerprint,
                        title = "App Lock",
                        subtitle = "Require biometric to open app",
                        onClick = onNavigateToAppLock
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    
                    SecurityActionItem(
                        icon = Icons.Filled.Password,
                        title = "Set Payment PIN",
                        subtitle = "Require PIN for transactions",
                        onClick = onNavigateToPaymentPin
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    SecurityActionItem(
                        icon = Icons.Filled.Lock,
                        title = stringResource(R.string.change_password),
                        subtitle = stringResource(R.string.update_credentials),
                        onClick = onNavigateToChangePassword
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    
                    SecurityActionItem(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        title = "Sign Out",
                        subtitle = "Log out of your account",
                        onClick = onLogout
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun InfoSection(title: String, description: String) {
    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SecurityCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@Composable
private fun RestoreCloudDialog(
    onDismiss: () -> Unit,
    onRestore: (String) -> Unit
) {
    val backups = listOf("May 12, 2024", "May 10, 2024", "May 05, 2024")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.available_backups), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.select_to_restore),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                backups.forEach { date ->
                    Surface(
                        onClick = { onRestore(date) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.CloudDone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(date, fontWeight = FontWeight.Bold)
                                Text(
                                    stringResource(R.string.cloud_backup_found, date),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun SecurityActionItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}
