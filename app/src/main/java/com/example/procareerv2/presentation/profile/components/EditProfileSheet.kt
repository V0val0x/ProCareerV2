package com.example.procareerv2.presentation.profile.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.procareerv2.domain.model.Interest
import com.example.procareerv2.domain.model.Skill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileSheet(
    isSkillsMode: Boolean,
    skills: List<Skill>,
    interests: List<Interest>,
    onAddSkill: (String, Int) -> Unit,
    onAddInterest: (String) -> Unit,
    onDeleteSkill: (Int) -> Unit,
    onDeleteInterest: (Int) -> Unit,
    onStartEditingSkill: (Skill) -> Unit,
    onStartEditingInterest: (Interest) -> Unit,
    editingSkill: Skill?,
    editingInterest: Interest?,
    onDismiss: () -> Unit
) {
    var newItemName by remember(editingSkill, editingInterest) { 
        mutableStateOf(editingSkill?.name ?: editingInterest?.name ?: "")
    }
    var skillLevel by remember(editingSkill) { 
        mutableStateOf(editingSkill?.proficiencyLevel ?: 50)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isSkillsMode) {
                    if (editingSkill != null) "Изменить навык" else "Редактировать навыки"
                } else {
                    if (editingInterest != null) "Изменить интерес" else "Редактировать интересы"
                },
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(
                onClick = {
                    if (editingSkill != null || editingInterest != null) {
                        newItemName = ""
                        skillLevel = 50
                        onDismiss()
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = if (editingSkill != null || editingInterest != null) "Отменить" else "Закрыть"
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Add new item section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = newItemName,
                onValueChange = { newItemName = it },
                label = { Text(if (isSkillsMode) "Название навыка" else "Название интереса") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (isSkillsMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Уровень владения: $skillLevel%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = skillLevel.toFloat(),
                    onValueChange = { skillLevel = it.toInt() },
                    valueRange = 0f..100f,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (newItemName.isNotBlank()) {
                        if (isSkillsMode) {
                            onAddSkill(newItemName, skillLevel)
                        } else {
                            onAddInterest(newItemName)
                        }
                        newItemName = ""
                        skillLevel = 50
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = newItemName.isNotBlank()
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (editingSkill != null || editingInterest != null) Icons.Default.Done
                        else Icons.Default.Add,
                        contentDescription = if (editingSkill != null || editingInterest != null) "Сохранить" else "Добавить"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (editingSkill != null || editingInterest != null) "Сохранить" else "Добавить"
                    )
                }
            }

            if (editingSkill != null || editingInterest != null) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        newItemName = ""
                        skillLevel = 50
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Отменить")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Отменить")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                if (isSkillsMode) {
                    items(skills) { skill ->
                        if (editingSkill?.id != skill.id) {
                            SkillItem(
                                skill = skill,
                                onDelete = { onDeleteSkill(skill.id) },
                                onClick = { onStartEditingSkill(skill) }
                            )
                        }
                    }
                } else {
                    items(interests) { interest ->
                        if (editingInterest?.id != interest.id) {
                            InterestItem(
                                interest = interest,
                                onDelete = { onDeleteInterest(interest.id) },
                                onClick = { onStartEditingInterest(interest) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SkillItem(
    skill: Skill,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
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
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = skill.proficiencyLevel / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Удалить навык",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InterestItem(
    interest: Interest,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = interest.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Удалить интерес",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
