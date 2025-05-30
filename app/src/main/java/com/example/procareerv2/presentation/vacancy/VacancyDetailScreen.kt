package com.example.procareerv2.presentation.vacancy

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.procareerv2.presentation.common.components.ProCareerBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VacancyDetailScreen(
    vacancyId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToVacancies: () -> Unit,
    onNavigateToTests: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: VacancyDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableStateOf(1) } // Вакансии - вкладка 1

    LaunchedEffect(key1 = vacancyId) {
        viewModel.loadVacancy(vacancyId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
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
                    Text(text = "Загрузка...")
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
                uiState.vacancy?.let { vacancy ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(scrollState)
                    ) {
                        // Заголовок вакансии
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                // Название вакансии
                                Text(
                                    text = vacancy.title,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Уровень вакансии
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            when (vacancy.grade) {
                                                "Intern" -> MaterialTheme.colorScheme.secondary
                                                "Junior" -> MaterialTheme.colorScheme.tertiary
                                                "Middle" -> Color(0xFF4CAF50) // Green
                                                "Senior" -> Color(0xFFE53935) // Red
                                                else -> MaterialTheme.colorScheme.primary
                                            }
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = vacancy.grade,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White
                                    )
                                }
                                
                                // Работодатель если есть
                                if (!vacancy.employer_name.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Business,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = vacancy.employer_name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                // Опыт работы согласно грейду
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AttachMoney,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Опыт: ${vacancy.grade}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // URL вакансии если есть
                        if (!vacancy.url.isNullOrBlank()) {
                            val context = LocalContext.current
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(vacancy.url))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Открыть вакансию на сайте",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Описание вакансии
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Описание вакансии",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                Divider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                                
                                if (!vacancy.description.isNullOrBlank()) {
                                    Text(
                                        text = vacancy.description,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        text = "Описание отсутствует",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Дополнительные секции если есть
                        if (vacancy.responsibilities != null && vacancy.responsibilities.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        text = "Обязанности",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    // Выводим список обязанностей
                                    vacancy.responsibilities.forEach { responsibility ->
                                        Text(
                                            text = "\u2022 $responsibility",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        if (vacancy.requirements != null && vacancy.requirements.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Text(
                                        text = "Требования",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    // Выводим список требований
                                    vacancy.requirements.forEach { requirement ->
                                        Text(
                                            text = "\u2022 $requirement",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}
