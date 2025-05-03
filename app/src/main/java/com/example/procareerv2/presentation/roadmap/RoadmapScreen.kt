package com.example.procareerv2.presentation.roadmap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.procareerv2.domain.model.Roadmap
import com.example.procareerv2.domain.model.RoadmapSkill
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadmapScreen(
    onNavigateBack: () -> Unit,
    viewModel: RoadmapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val textMeasurer = rememberTextMeasurer()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            uiState.roadmap?.let { roadmap ->
                Text(
                    text = roadmap.description,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )

                SkillsGraph(
                    skills = roadmap.skills,
                    textMeasurer = textMeasurer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp)
                        .padding(16.dp)
                )

                // Список скилов с детальной информацией
                roadmap.skills.forEach { skill ->
                    SkillCard(
                        skill = skill,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SkillsGraph(
    skills: List<RoadmapSkill>,
    textMeasurer: TextMeasurer,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .background(colorScheme.surface)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val maxRadius = min(centerX, centerY) * 0.8f

            // Рисуем связи между скилами
            skills.forEach { skill ->
                skill.relatedSkills.forEach { relatedId ->
                    val relatedSkill = skills.find { it.id == relatedId } ?: return@forEach
                    drawSkillConnection(
                        skill1 = skill,
                        skill2 = relatedSkill,
                        totalSkills = skills.size,
                        maxRadius = maxRadius,
                        skills = skills,
                        color = colorScheme.primary
                    )
                }
            }

            // Рисуем круги скилов
            skills.forEachIndexed { index, skill ->
                val angle = (2 * PI * index / skills.size).toFloat()
                val radius = maxRadius * 0.8f
                val x = centerX + radius * cos(angle)
                val y = centerY + radius * sin(angle)

                // Размер круга зависит от важности скила
                val circleRadius = (30 + skill.importance * 0.3f).dp.toPx()

                drawCircle(
                    color = colorScheme.primary,
                    radius = circleRadius,
                    center = Offset(x, y)
                )

                // Рисуем текст скила
                val textLayoutResult = textMeasurer.measure(
                    text = skill.name
                )
                with(this) {
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            x - textLayoutResult.size.width / 2,
                            y - textLayoutResult.size.height / 2
                        ),
                        color = colorScheme.onSurface
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawSkillConnection(
    skill1: RoadmapSkill,
    skill2: RoadmapSkill,
    totalSkills: Int,
    maxRadius: Float,
    skills: List<RoadmapSkill>,
    color: Color
) {
    val index1 = skills.indexOf(skill1)
    val index2 = skills.indexOf(skill2)
    if (index1 == -1 || index2 == -1) return

    val angle1 = (2 * PI * index1 / totalSkills).toFloat()
    val angle2 = (2 * PI * index2 / totalSkills).toFloat()

    val centerX = size.width / 2
    val centerY = size.height / 2
    val radius = maxRadius * 0.8f

    val x1 = centerX + radius * cos(angle1)
    val y1 = centerY + radius * sin(angle1)
    val x2 = centerX + radius * cos(angle2)
    val y2 = centerY + radius * sin(angle2)

    drawLine(
        color = color.copy(alpha = 0.3f),
        start = Offset(x1, y1),
        end = Offset(x2, y2),
        strokeWidth = 2.dp.toPx()
    )
}

@Composable
private fun SkillCard(
    skill: RoadmapSkill,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = skill.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = skill.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = skill.importance / 100f,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Важность: ${skill.importance}%",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
