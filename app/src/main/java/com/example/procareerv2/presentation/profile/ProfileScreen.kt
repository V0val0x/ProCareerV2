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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToVacancies: () -> Unit,
    onNavigateToTests: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableStateOf(3) } // Профиль - вкладка 3

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мой профиль") },
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
                            text = { Text("Редактировать навыки") },
                            onClick = {
                                viewModel.showEditSheet(true)
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Редактировать интересы") },
                            onClick = {
                                viewModel.showEditSheet(false)
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
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { viewModel.showEditProfileDialog() },
                    contentAlignment = Alignment.Center
                ) {
                    val context = LocalContext.current
                    val profileImage = uiState.user?.profileImage
                    val imageRequest = if (profileImage != null) {
                        ImageRequest.Builder(context)
                            .data(File(profileImage))
                            .crossfade(true)
                            .build()
                    } else {
                        ImageRequest.Builder(context)
                            .data(R.drawable.default_avatar)
                            .crossfade(true)
                            .build()
                    }

                    // Display user name and position
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        Image(
                            painter = rememberAsyncImagePainter(imageRequest),
                            contentDescription = "Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    // Edit overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = Color.Black.copy(alpha = 0.3f))
                    ) {
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

            // Position
            Text(
                text = uiState.user?.position ?: "Front-end разработчик",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Skills Section
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Навыки",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = { viewModel.showEditSheet(true) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Редактировать навыки",
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
                    Column(modifier = Modifier.padding(16.dp)) {
                        uiState.skills.forEach { skill ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = skill.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${skill.proficiencyLevel}%",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = skill.proficiencyLevel / 100f,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                            if (skill != uiState.skills.last()) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
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



            Spacer(modifier = Modifier.height(24.dp))

            // Roadmap button
            Button(
                onClick = { /* Navigate to roadmap */ },
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
                onClick = onLogout,
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

    // Bottom Sheet for editing skills and interests
    if (uiState.showEditSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { viewModel.hideEditSheet() },
            sheetState = sheetState
        ) {
            EditProfileSheet(
                isSkillsMode = uiState.isSkillsMode,
                skills = uiState.user?.skills ?: emptyList(),
                interests = uiState.user?.interests ?: emptyList(),
                onAddSkill = viewModel::addSkill,
                onAddInterest = viewModel::addInterest,
                onDeleteSkill = viewModel::deleteSkill,
                onDeleteInterest = viewModel::deleteInterest,
                onStartEditingSkill = viewModel::startEditingSkill,
                onStartEditingInterest = viewModel::startEditingInterest,
                editingSkill = uiState.editingSkill,
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
                onSave = { name: String, position: String, imageUri: Uri? ->
                    viewModel.updateUserProfile(name, position, imageUri?.toString())
                },
                error = uiState.error
            )
        }
    }
}