package com.example.procareerv2.presentation.profile

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.procareerv2.presentation.profile.components.EditProfileDialog
import java.io.File
import com.example.procareerv2.R
import com.example.procareerv2.presentation.common.components.ProCareerBottomBar
import com.example.procareerv2.presentation.profile.components.EditProfileSheet
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToVacancies: () -> Unit,
    onNavigateToTests: () -> Unit,
    onNavigateToRoadmap: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableStateOf(3) } // Профиль - вкладка 3
    var showSubscriptionDialog by remember { mutableStateOf(false) }
    var isSubscribed by remember { mutableStateOf(true) }
    var showSnackbar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // При первом открытии экрана обновляем данные с сервера
    LaunchedEffect(key1 = Unit) {
        Log.d("ProfileScreen", "Screen launched, refreshing user profile")
        viewModel.refreshUserProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Редактировать профиль") },
                            onClick = {
                                viewModel.showEditProfileDialog()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Управление рассылкой") },
                            onClick = {
                                showSubscriptionDialog = true
                                showMenu = false
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            ProCareerBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onNavigateToHome = onNavigateToHome,
                onNavigateToVacancies = onNavigateToVacancies,
                onNavigateToTests = onNavigateToTests,
                onNavigateToProfile = { /* Already on profile */ }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile header with image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                val profileImage = uiState.user?.profileImage
                val imageModel = if (profileImage != null) {
                    Uri.parse(profileImage)
                } else {
                    R.drawable.default_avatar
                }
                
                // Background blur effect
                Image(
                    painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(context)
                            .data(imageModel)
                            .crossfade(true)
                            .build()
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                    contentScale = ContentScale.Crop,
                    alpha = 0.3f // Make it semi-transparent
                )
                
                // Profile image
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { viewModel.showEditProfileDialog() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = ImageRequest.Builder(context)
                                .data(imageModel)
                                .crossfade(true)
                                .build()
                        ),
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Edit overlay (only show on hover/press)
                    var isHovered by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { 
                                viewModel.showEditProfileDialog()
                                isHovered = false 
                            }
                            .background(color = if (isHovered) Color.Black.copy(alpha = 0.3f) else Color.Transparent)
                    ) {
                        if (isHovered) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(32.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name
            Text(
                text = uiState.user?.name ?: "Антон Петухов",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Грейд (отображаем с большой буквы)
            Text(
                text = (uiState.user?.position ?: "Front-end разработчик").replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Specialization (if available, отображаем с большой буквы)
            if (!uiState.user?.specialization.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = (uiState.user?.specialization ?: "").replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Interests Section
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Интересы",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = { viewModel.showEditSheet(false) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Редактировать интересы",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    FlowRow(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.interests.forEach { interest ->
                            SuggestionChip(
                                onClick = { },
                                label = { Text(interest.name) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    labelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Закомментировано: раздел с резюме
            /*
            // Resume card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_document),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )

                    Text(
                        text = "Резюме.pdf",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { /* Download resume */ }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_download),
                                contentDescription = "Download",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(onClick = { /* Share resume */ }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_share),
                                contentDescription = "Share",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            */



            Spacer(modifier = Modifier.height(24.dp))

            // Roadmap button
            Button(
                onClick = onNavigateToRoadmap,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Мой роудмап развития",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tests button
            OutlinedButton(
                onClick = onNavigateToTests,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Тесты",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout button
            OutlinedButton(
                onClick = {
                    viewModel.logout()
                    onLogout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = "Выйти",
                    style = MaterialTheme.typography.titleMedium
                )
            }

        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Bottom Sheet for editing interests
    if (uiState.showEditInterestsSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { viewModel.hideEditSheet() },
            sheetState = sheetState
        ) {
            EditProfileSheet(
                interests = uiState.user?.interests ?: emptyList(),
                onAddInterest = viewModel::addInterest,
                onDeleteInterest = viewModel::deleteInterest,
                onStartEditingInterest = viewModel::startEditingInterest,
                editingInterest = uiState.editingInterest,
                onDismiss = { viewModel.hideEditSheet() }
            )
        }
    }

    if (uiState.showEditProfileDialog) {
        val user = uiState.user
        if (user != null) {
            EditProfileDialog(
                user = user,
                onDismiss = { viewModel.hideEditProfileDialog() },
                onSave = { name: String, position: String, specialization: String, imageUri: Uri? ->
                    viewModel.updateUserProfile(name, position, specialization, imageUri)
                },
                error = uiState.error
            )
        }
    }

    if (showSubscriptionDialog) {
        AlertDialog(
            onDismissRequest = { showSubscriptionDialog = false },
            title = { Text("Управление рассылкой") },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Получать уведомления")
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(onClick = {
                        isSubscribed = !isSubscribed
                        showSnackbar = true
                    }) {
                        val icon = if (isSubscribed) Icons.Default.Check else Icons.Default.Close
                        Icon(
                            imageVector = icon,
                            contentDescription = if (isSubscribed) "Включено" else "Выключено",
                            tint = if (isSubscribed) Color(0xFF4CAF50) else Color(0xFFF44336),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showSubscriptionDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
    if (showSnackbar) {
        LaunchedEffect(isSubscribed) {
            snackbarHostState.showSnackbar(
                if (isSubscribed) "Уведомления включены" else "Уведомления отключены"
            )
            showSnackbar = false
        }
    }
}