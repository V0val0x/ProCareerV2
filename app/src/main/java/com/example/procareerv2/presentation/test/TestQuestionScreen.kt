package com.example.procareerv2.presentation.test

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.procareerv2.domain.model.Answer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestQuestionScreen(
    testId: Int,
    onFinishTest: () -> Unit,
    viewModel: TestQuestionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = testId) {
        viewModel.startTest(testId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Вопрос ${uiState.currentQuestionIndex + 1} из ${uiState.totalQuestions}") },
                navigationIcon = {
                    IconButton(onClick = { /* Handle back navigation */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
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
            } else if (uiState.isTestCompleted) {
                // Test completed state
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Тест завершен!",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Правильных ответов: ${uiState.correctAnswers} из ${uiState.totalQuestions}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onFinishTest,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Завершить")
                    }
                }
            } else {
                // Question state
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Progress indicator
                    LinearProgressIndicator(
                        progress = (uiState.currentQuestionIndex + 1).toFloat() / uiState.totalQuestions,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Question
                    uiState.currentQuestion?.let { question ->
                        Text(
                            text = question.question,
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Answers
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            /*items(question.answers) { answer ->
                                AnswerItem(
                                    answer = answer,
                                    isSelected = uiState.selectedAnswerId == answer.id,
                                    onClick = { viewModel.selectAnswer(answer.id) }
                                )
                            }*/
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Next button
                        Button(
                            onClick = { viewModel.nextQuestion() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = uiState.selectedAnswerId != null
                        ) {
                            Text(
                                text = if (uiState.currentQuestionIndex == uiState.totalQuestions - 1)
                                    "Завершить" else "Следующий вопрос"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnswerItem(
    answer: Answer,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )

            Text(
                text = answer.answer,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}