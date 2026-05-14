package com.namma.santhe.ui.screens.settings

import kotlinx.coroutines.launch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.namma.santhe.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToDataSecurity: (String) -> Unit
) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
            SettingsSection(title = stringResource(R.string.app_preferences)) {
                // Language
                SettingsRow(
                    icon = Icons.Filled.Language,
                    title = stringResource(R.string.language),
                    subtitle = when (language) {
                        "kn" -> "ಕನ್ನಡ"
                        "te" -> "తెలుగు"
                        "ta" -> "தமிழ்"
                        "hi" -> "हिन्दी"
                        else -> "English"
                    }
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Text(stringResource(R.string.change))
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(
                                text = { Text("ಕನ್ನಡ") },
                                onClick = {
                                    viewModel.setLanguage("kn")
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("తెలుగు") },
                                onClick = {
                                    viewModel.setLanguage("te")
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("தமிழ்") },
                                onClick = {
                                    viewModel.setLanguage("ta")
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("हिन्दी") },
                                onClick = {
                                    viewModel.setLanguage("hi")
                                    expanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("English") },
                                onClick = {
                                    viewModel.setLanguage("en")
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )

                // Dark Mode
                SettingsRow(
                    icon = Icons.Filled.DarkMode,
                    title = stringResource(R.string.dark_mode),
                    subtitle = if (isDarkMode) stringResource(R.string.enabled) else stringResource(R.string.disabled)
                ) {
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.setDarkMode(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSection(title = stringResource(R.string.notifications)) {
                SettingsRow(
                    icon = Icons.Filled.Notifications,
                    title = stringResource(R.string.push_notifications),
                    subtitle = stringResource(R.string.notifications_desc)
                ) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val msg = stringResource(R.string.notifications_enabled_msg)
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.toggleNotifications()
                            if (enabled) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(msg)
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSection(title = stringResource(R.string.data_security)) {
                SettingsActionRow(
                    icon = Icons.Filled.Backup,
                    title = stringResource(R.string.backup_data),
                    subtitle = stringResource(R.string.backup_data_desc)
                ) {
                    onNavigateToDataSecurity("backup")
                }
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )

                SettingsActionRow(
                    icon = Icons.Filled.Restore,
                    title = stringResource(R.string.restore_data),
                    subtitle = stringResource(R.string.restore_data_desc)
                ) {
                    onNavigateToDataSecurity("restore")
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )

                SettingsActionRow(
                    icon = Icons.Filled.Security,
                    title = stringResource(R.string.account_security),
                    subtitle = stringResource(R.string.update_credentials)
                ) {
                    onNavigateToDataSecurity("security")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSection(title = stringResource(R.string.about)) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val appVersionString = stringResource(R.string.app_version)
                val versionLabel = "${packageInfo.versionName} (${packageInfo.versionCode})"
                SettingsActionRow(
                    icon = Icons.Filled.Info,
                    title = appVersionString,
                    subtitle = versionLabel
                ) {
                    scope.launch {
                        snackbarHostState.showSnackbar("$appVersionString: $versionLabel")
                    }
                }
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )

                SettingsActionRow(
                    icon = Icons.AutoMirrored.Filled.Help,
                    title = stringResource(R.string.help_support),
                    subtitle = stringResource(R.string.help_support_desc)
                ) {
                    onNavigateToHelp()
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = stringResource(R.string.made_in_india),
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    action: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
        action()
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}
