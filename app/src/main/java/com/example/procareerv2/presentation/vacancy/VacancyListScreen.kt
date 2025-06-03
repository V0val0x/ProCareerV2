package com.example.procareerv2.presentation.vacancy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.procareerv2.domain.model.Vacancy
import com.example.procareerv2.presentation.common.components.ProCareerBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VacancyListScreen(
    onNavigateToVacancyDetail: (Int) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToTests: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: VacancyListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val searchState = remember { mutableStateOf("") }
    
    // Запускаем загрузку вакансий при первом запуске
    LaunchedEffect(key1 = Unit) {
        viewModel.loadVacancies()
    }
    
    // Логирование списка вакансий
    LogVacancies(vacancies = uiState.filteredVacancies)
    
    // Проверка, нужно ли подгружать больше вакансий
    val loadMoreItems = remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
            
            // Логирование для диагностики пагинации
            android.util.Log.d(
                "VacancyListScreen", 
                "Pagination check: lastVisibleItemIndex=$lastVisibleItemIndex, totalItems=$totalItemsNumber, threshold=${totalItemsNumber - 3}"
            )
            
            !uiState.isLoading && !uiState.isLoadingMore && !uiState.isLastPage &&
            lastVisibleItemIndex > totalItemsNumber - 3 && totalItemsNumber > 0
        }
    }
    
    LaunchedEffect(loadMoreItems.value) {
        if (loadMoreItems.value) {
            android.util.Log.d("VacancyListScreen", "Загрузка следующей страницы вакансий...")
            viewModel.loadMoreVacancies()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Рекомендуемые вакансии") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            ProCareerBottomBar(
                selectedTab = 1,
                onTabSelected = { },
                onNavigateToHome = onNavigateToHome,
                onNavigateToVacancies = { /* Already on vacancies */ },
                onNavigateToTests = onNavigateToTests,
                onNavigateToProfile = onNavigateToProfile
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Поисковая строка (всегда видимая)
            OutlinedTextField(
                value = searchState.value,
                onValueChange = { newValue ->
                    searchState.value = newValue
                    viewModel.searchVacancies(newValue)
                },
                placeholder = { Text("Поиск вакансий") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Поиск"
                    )
                },
                shape = RoundedCornerShape(50),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                )
            )
            
            // Основной контент
            Box(modifier = Modifier.fillMaxSize()) {
                // Список вакансий
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.filteredVacancies) { vacancy ->
                        VacancyItem(
                            vacancy = vacancy,
                            onClick = {
                                onNavigateToVacancyDetail(vacancy.id)
                            }
                        )
                    }
                    
                    // Показываем индикатор загрузки при подгрузке новых данных
                    if (uiState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                    
                    // Показываем сообщение о конце списка
                    if (uiState.isLastPage && uiState.vacancies.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Больше нет вакансий",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
                
                // Индикатор загрузки при первоначальной загрузке
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                // Сообщение об ошибке
                if (uiState.error != null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Ошибка",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = uiState.error ?: "Неизвестная ошибка",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadVacancies() }
                            ) {
                                Text("Попробовать снова")
                            }
                        }
                    }
                }
                
                // Сообщение при пустом списке вакансий
                if (!uiState.isLoading && uiState.error == null && uiState.filteredVacancies.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Вакансий не найдено",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// Логирование вакансий
@Composable
private fun LogVacancies(vacancies: List<Vacancy>) {
    LaunchedEffect(vacancies) {
        android.util.Log.d("VacancyListScreen", "===== СПИСОК ВАКАНСИЙ =====")
        vacancies.forEachIndexed { index, vacancy ->
            android.util.Log.d("VacancyListScreen", "[$index] ID: ${vacancy.id}, title: ${vacancy.title}")
        }
        android.util.Log.d("VacancyListScreen", "=========================")
    }
}

// Карточка вакансии
@Composable
fun VacancyItem(
    vacancy: Vacancy,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F9FC) // Светло-голубой фон как на скриншоте
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Заголовок вакансии
            Text(
                text = vacancy.title,
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF3F51B5), // Синий цвет как на скриншоте
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Нижняя часть с компанией и уровнем
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Компания с иконкой
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = vacancy.employer_name ?: "Компания",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Уровень (grade) с цветным фоном
                val gradeColor = when(vacancy.grade.lowercase()) {
                    "junior" -> Color(0xFF4CAF50)  // Зеленый
                    "middle" -> Color(0xFFE91E63)  // Розовый
                    "intern" -> Color(0xFF00BCD4)  // Голубой
                    else -> Color(0xFF9E9E9E)      // Серый по умолчанию
                }
                
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = gradeColor
                    )
                ) {
                    Text(
                        text = vacancy.grade,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
