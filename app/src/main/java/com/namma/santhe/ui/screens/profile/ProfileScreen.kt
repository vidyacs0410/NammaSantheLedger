package com.namma.santhe.ui.screens.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.Manifest
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.namma.santhe.R
import com.namma.santhe.utils.ImageUtils
import kotlinx.coroutines.launch
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToChangeNumber: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val userName = viewModel.sessionManager.getUserName()
    val userPhone = viewModel.sessionManager.getUserPhone()
    val profileImage by viewModel.profileImage.collectAsStateWithLifecycle()
    val businessType by viewModel.businessType.collectAsStateWithLifecycle()
    val address by viewModel.address.collectAsStateWithLifecycle()

    var editedName by remember(userName) { mutableStateOf(userName) }
    var editedAddress by remember(address) { mutableStateOf(address) }
    var isEditingName by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    
    var expanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val businessTypes = listOf(
        stringResource(R.string.retail_shop), 
        stringResource(R.string.wholesale_distributor), 
        stringResource(R.string.personal_use), 
        stringResource(R.string.small_business), 
        stringResource(R.string.online_services)
    )
    
    var showLocationPicker by remember { mutableStateOf(false) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
            showLocationPicker = true
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.location_permission_denied))
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            // Save to internal storage for persistence
            val savedPath = ImageUtils.copyUriToInternalStorage(context, uri)
            if (savedPath != null) {
                viewModel.updateProfile(savedPath, businessType, editedAddress)
                scope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.photo_updated))
                }
            }
        }
    }

    val onSaveProfile = {
        val nameChanged = editedName != userName
        val addressChanged = editedAddress != address
        
        viewModel.sessionManager.updateName(editedName)
        viewModel.updateProfile(profileImage, businessType, editedAddress)
        isEditingName = false
        
        scope.launch {
            val msg = if (nameChanged) {
                context.getString(R.string.name_updated)
            } else {
                context.getString(R.string.profile_saved)
            }
            snackbarHostState.showSnackbar(msg)
        }
        Unit
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Section with Overlapping Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                // Curved background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primaryContainer
                                )
                            ),
                            shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                        )
                ) {
                    // Top App Bar inside header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        Text(
                            text = stringResource(R.string.profile),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        // Removed save icon as per user request
                        Spacer(modifier = Modifier.width(48.dp)) // Maintain centering for the title
                    }
                }

                // Profile Image overlapping the bottom
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .shadow(8.dp, CircleShape)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .border(4.dp, MaterialTheme.colorScheme.background, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileImage != null) {
                            AsyncImage(
                                model = profileImage,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        }
                    }
                    
                    // Edit Button
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 4.dp, bottom = 4.dp)
                            .size(40.dp)
                            .shadow(4.dp, CircleShape)
                            .background(MaterialTheme.colorScheme.secondary, CircleShape)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.CameraAlt,
                            contentDescription = stringResource(R.string.edit_photo),
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name Field (Clean and centered with pencil icon)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    readOnly = !isEditingName,
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.Center) {
                            if (editedName.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.your_name),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                                    )
                                )
                            }
                            innerTextField()
                        }
                    },
                    modifier = Modifier
                        .widthIn(min = 40.dp)
                        .focusRequester(focusRequester)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = { 
                        isEditingName = true
                        focusRequester.requestFocus()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.edit_name),
                        tint = if (isEditingName) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.business_information).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Phone Field
                    ProfileInfoField(
                        icon = Icons.Filled.Phone,
                        label = stringResource(R.string.phone_number),
                        value = userPhone,
                        readOnly = true,
                        onValueChange = {},
                        trailingIcon = Icons.Filled.Edit,
                        onTrailingIconClick = onNavigateToChangeNumber
                    )
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    // Business Type Field
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        Box(modifier = Modifier.menuAnchor()) {
                            ProfileInfoField(
                                icon = Icons.Filled.Storefront,
                                label = stringResource(R.string.business_type),
                                value = businessType,
                                readOnly = true,
                                onValueChange = {},
                                showDropdownIcon = true
                            )
                        }
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            businessTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type, style = MaterialTheme.typography.bodyLarge) },
                                    onClick = {
                                        viewModel.updateProfile(profileImage, type, editedAddress)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                    // Address Field
                    ProfileInfoField(
                        icon = Icons.Filled.LocationOn,
                        label = stringResource(R.string.business_address),
                        value = editedAddress,
                        readOnly = false,
                        onValueChange = { editedAddress = it },
                        trailingIcon = Icons.Filled.MyLocation,
                        onTrailingIconClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                            
                            if (hasPermission) {
                                showLocationPicker = true
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        }
                    )
                }
            }

            if (showLocationPicker) {
                AlertDialog(
                    onDismissRequest = { showLocationPicker = false },
                    title = { Text(stringResource(R.string.select_location)) },
                    text = { Text(stringResource(R.string.location_picker_msg)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showLocationPicker = false
                                scope.launch {
                                    try {
                                        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                                            .addOnSuccessListener { location ->
                                                location?.let {
                                                    val geocoder = Geocoder(context, Locale.getDefault())
                                                    val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                                                    if (!addresses.isNullOrEmpty()) {
                                                        val addr = addresses[0]
                                                        val fullAddress = buildString {
                                                            append(addr.getAddressLine(0))
                                                        }
                                                        editedAddress = fullAddress
                                                    }
                                                }
                                            }
                                    } catch (e: SecurityException) {
                                        snackbarHostState.showSnackbar(context.getString(R.string.permission_error))
                                    }
                                }
                            }
                        ) {
                            Text(stringResource(R.string.current_location))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLocationPicker = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Save Button
            Button(
                onClick = onSaveProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.update_profile),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileInfoField(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    readOnly: Boolean,
    onValueChange: (String) -> Unit,
    showDropdownIcon: Boolean = false,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(4.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                readOnly = readOnly,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = stringResource(R.string.not_set),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        )
                    }
                    innerTextField()
                }
            )
        }
        
        if (showDropdownIcon) {
            Icon(
                Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        } else if (trailingIcon != null) {
            IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
