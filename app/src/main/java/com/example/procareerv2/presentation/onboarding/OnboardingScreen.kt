package com.example.procareerv2.presentation.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.procareerv2.R
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.ui.zIndex
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

data class OnboardingPage(
    val image: Int,
    val title: String,
    val description: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onNavigateToLogin: () -> Unit,
    onSkip: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            image = R.drawable.onboarding_1,
            title = "Начни свой понятный карьерный путь вместе с нами",
            description = ""
        ),
        OnboardingPage(
            image = R.drawable.onboarding_2,
            title = "Приложение сформирует твой персонализированный роадмап развития",
            description = ""
        ),
        OnboardingPage(
            image = R.drawable.onboarding_3,
            title = "Поможет понять, какие есть пробелы",
            description = ""
        ),
        OnboardingPage(
            image = R.drawable.onboarding_4,
            title = "Выдаст всевозможные вакансии, чтобы тебе было удобно",
            description = ""
        ),
        OnboardingPage(
            image = R.drawable.onboarding_5,
            title = "Отслеживай свои успехи",
            description = ""
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    var showProfessionDialog = remember { mutableStateOf(false) }
    var selectedProfession = remember { mutableStateOf(IT_PROFESSIONS[0]) }
    var onboardingFinished = remember { mutableStateOf(false) }
    var customProfession by rememberSaveable { mutableStateOf("") }

    fun finishOnboarding() {
        showProfessionDialog.value = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        // Skip button (поверх всего)
        TextButton(
            onClick = { finishOnboarding() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 32.dp, end = 16.dp)
                .zIndex(2f)
        ) {
            Text(
                text = "Пропустить",
                color = Color.White
            )
        }

        // Pager content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingPageContent(
                page = pages[page],
                modifier = Modifier.fillMaxSize()
            )
        }

        // Bottom section with indicators and button
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page indicators
            PageIndicator(
                pageCount = pages.size,
                currentPage = pagerState.currentPage,
                modifier = Modifier.padding(16.dp)
            )

            // Next button
            Button(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        finishOnboarding()
                    }
                },
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.next),
                    contentDescription = "Далее"
                )
            }
        }

        if (showProfessionDialog.value) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Выберите желаемую профессию") },
                text = {
                    LazyColumn {
                        items(IT_PROFESSIONS.size) { index ->
                            val profession = IT_PROFESSIONS[index]
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedProfession.value = profession
                                        if (profession != "Другое") {
                                            customProfession = ""
                                        }
                                    }
                                    .padding(vertical = 8.dp)
                            ) {
                                RadioButton(
                                    selected = selectedProfession.value == profession,
                                    onClick = {
                                        selectedProfession.value = profession
                                        if (profession != "Другое") {
                                            customProfession = ""
                                        }
                                    }
                                )
                                Text(profession)
                            }
                            if (profession == "Другое" && selectedProfession.value == "Другое") {
                                OutlinedTextField(
                                    value = customProfession,
                                    onValueChange = { customProfession = it },
                                    label = { Text("Ваша профессия") },
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    singleLine = true,
                                    isError = false
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        showProfessionDialog.value = false
                        onNavigateToHome()
                    }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = page.image),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .padding(bottom = 32.dp)
        )

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (page.description.isNotEmpty()) {
            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp, start = 32.dp, end = 32.dp)
            )
        }
    }
}

@Composable
fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        repeat(pageCount) { page ->
            val color = if (page == currentPage) {
                MaterialTheme.colorScheme.secondary
            } else {
                Color.White.copy(alpha = 0.5f)
            }

            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

val IT_PROFESSIONS = listOf(
    "Android-разработчик",
    "iOS-разработчик",
    "QA-инженер",
    "DevOps-инженер",
    "Data Scientist",
    "Frontend-разработчик",
    "Backend-разработчик",
    "Fullstack-разработчик",
    "Product Manager",
    "UI/UX Designer",
    "Другое"
)