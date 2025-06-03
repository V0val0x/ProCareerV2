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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileSheet(
    interests: List<Interest>,
    onAddInterest: (String) -> Unit,
    onDeleteInterest: (String) -> Unit,
    onStartEditingInterest: (Interest) -> Unit,
    editingInterest: Interest?,
    onDismiss: () -> Unit
) {
    var newItemName by remember(editingInterest) { 
        mutableStateOf(editingInterest?.name ?: "")
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
                text = if (editingInterest != null) "Изменить интерес" else "Редактировать интересы",
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(
                onClick = {
                    if (editingInterest != null) {
                        newItemName = ""
                        onDismiss()
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = if (editingInterest != null) "Отменить" else "Закрыть"
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
                label = { Text("Название интереса") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = if (editingInterest != null) {
                    {
                        if (newItemName.isNotBlank()) {
                            onAddInterest(newItemName)
                            newItemName = ""
                            onDismiss()
                        }
                    }
                } else {
                    {
                        if (newItemName.isNotBlank()) {
                            onAddInterest(newItemName)
                            newItemName = ""
                        }
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
                        if (editingInterest != null) Icons.Default.Done else Icons.Default.Add,
                        contentDescription = if (editingInterest != null) "Сохранить" else "Добавить"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (editingInterest != null) "Сохранить" else "Добавить"
                    )
                }
            }

            if (editingInterest != null) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        newItemName = ""
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
                items(interests) { interest ->
                    if (editingInterest?.id != interest.id) {
                        InterestItem(
                            interest = interest,
                            onDelete = { onDeleteInterest(interest.name) },
                            onClick = { onStartEditingInterest(interest) }
                        )
                    }
                }
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
