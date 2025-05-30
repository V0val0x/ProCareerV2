package com.example.procareerv2.presentation.vacancy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.procareerv2.domain.model.Vacancy
import com.example.procareerv2.presentation.common.components.ProCareerBottomBar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VacancyListScreen(
    onNavigateToVacancyDetail: (Int) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToTests: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: VacancyListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(1) } // Вакансии - вкладка 1

    LaunchedEffect(key1 = true) {
        viewModel.loadVacancies()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
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
                onNavigateToVacancies = { /* Already on vacancies */ },
                onNavigateToTests = onNavigateToTests,
                onNavigateToProfile = onNavigateToProfile
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.searchVacancies(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                placeholder = { Text("Поиск вакансий") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            if (uiState.isLoading) {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Загрузка...")
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(uiState.filteredVacancies) { vacancy ->
                        VacancyItem(
                            vacancy = vacancy,
                            onClick = { onNavigateToVacancyDetail(vacancy.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VacancyItem(
    vacancy: Vacancy,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Заголовок вакансии с выделением
            Text(
                text = vacancy.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Карточка с информацией о компании и грейдом
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Информация о компании
                Column(modifier = Modifier.weight(1f)) {
                    // Отображаем название работодателя, если оно есть
                    vacancy.employer_name?.let { employer ->
                        Text(
                            text = employer,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Индикатор уровня/грейда
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
            }
            
            // Добавляем информацию о технологиях, если они есть
            vacancy.technologies?.let { techList ->
                if (techList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Показываем первые 3 технологии
                    val visibleTechs = techList.take(3)
                    
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Отображаем первые 3 технологии
                        for (tech in visibleTechs) {
                            TechItem(tech = tech.trim())
                        }
                        
                        // Если технологий больше 3, показываем индикатор
                        if (techList.size > 3) {
                            MoreTechIndicator(count = techList.size - 3)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TechItem(tech: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = tech,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun MoreTechIndicator(count: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "+$count",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
