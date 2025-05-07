package com.example.procareerv2.presentation.roadmap

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.procareerv2.domain.model.NodeStatus
import com.example.procareerv2.domain.model.RoadmapTheme
import com.example.procareerv2.presentation.common.components.ProCareerBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadmapThemesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToThemeDetails: (Int) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToVacancies: () -> Unit,
    onNavigateToTests: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: RoadmapThemesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableStateOf(0) } // Roadmap - tab 0 (custom)

    LaunchedEffect(key1 = true) {
        viewModel.loadRoadmap()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (uiState.roadmap != null) uiState.roadmap!!.title else "Карта развития", 
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.White
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
                onNavigateToProfile = onNavigateToProfile
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                // Error state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ошибка: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                // Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Roadmap title
                    if (uiState.roadmap != null) {
                        Text(
                            text = uiState.roadmap!!.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                    }

                    // Modern roadmap with themes
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Vertical connecting line
                        if (uiState.themes.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(((uiState.themes.size * 210) - 60).dp)
                                    .align(Alignment.TopCenter)
                                    .offset { IntOffset(0, 50) }
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    )
                            )
                        }

                        // Themes
                        Column(
                            verticalArrangement = Arrangement.spacedBy(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            uiState.themes.forEachIndexed { index, theme ->
                                RoadmapThemeItem(
                                    theme = theme,
                                    position = index,
                                    onThemeClick = { onNavigateToThemeDetails(theme.id) }
                                )
                            }
                        }
                    }
                    
                    // Add some space at the bottom
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun RoadmapThemeItem(
    theme: RoadmapTheme,
    position: Int,
    onThemeClick: () -> Unit
) {
    // Calculate completion stats - excluding skill with the same name as theme
    val shouldExcludeThemeSkill = theme.skills.any { it.name == theme.title }
    val adjustSkillCount = if (shouldExcludeThemeSkill) 1 else 0
    
    val totalSkills = theme.skills.size - adjustSkillCount
    val completedSkills = theme.skills.count { 
        // Если навык имеет такое же название как тема, не считаем его
        if (shouldExcludeThemeSkill && it.name == theme.title) false
        else it.status == NodeStatus.LEARNED 
    }
    val progressPercentage = if (totalSkills > 0) (completedSkills * 100) / totalSkills else 0
    
    // Check if theme is fully completed (all relevant skills learned)
    val isCompleted = totalSkills > 0 && completedSkills == totalSkills
    
    val progressColor = when {
        isCompleted || progressPercentage == 100 -> Color(0xFF4CAF50) // Green for completion
        progressPercentage >= 40 -> Color(0xFFFF9800) // Orange for medium completion
        else -> MaterialTheme.colorScheme.primary    // Primary color for low completion
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Theme node (circle with number)
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(elevation = 4.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(progressColor)
                .border(width = 2.dp, color = Color.White, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${position + 1}",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Theme content card with elevation and better shadow
        Card(
            modifier = Modifier
                .weight(1f)
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp))
                .clickable { onThemeClick() },
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = theme.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (theme.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = theme.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress indicator with gradient background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.LightGray.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressPercentage / 100f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .then(
                                if (isCompleted) {
                                    // Для завершенных тем используем градиент
                                    Modifier.background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF43A047),  // Темно-зеленый
                                                Color(0xFF66BB6A),  // Зеленый
                                                Color(0xFF81C784)   // Светло-зеленый
                                            )
                                        ),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                } else {
                                    // Для не завершенных тем используем сплошной цвет
                                    Modifier.background(
                                        color = progressColor,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                }
                            )
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Stats and detail button in a row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Progress stats
                    Column {
                        Text(
                            text = "$completedSkills из $totalSkills",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isCompleted || progressPercentage == 100) "Завершено" 
                                  else if (progressPercentage > 0) "Изучается" 
                                  else "Не начато",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCompleted) progressColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Detail button with improved design
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Подробнее",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
