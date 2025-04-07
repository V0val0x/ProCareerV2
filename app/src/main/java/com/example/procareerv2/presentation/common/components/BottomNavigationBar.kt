package com.example.procareerv2.presentation.common.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ProCareerBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToVacancies: () -> Unit,
    onNavigateToTests: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = {
                onTabSelected(0)
                onNavigateToHome()
            },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Главная") }
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = {
                onTabSelected(1)
                onNavigateToVacancies()
            },
            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            label = { Text("Вакансии") }
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = {
                onTabSelected(2)
                onNavigateToTests()
            },
            icon = { Icon(Icons.Outlined.Edit, contentDescription = "Tests") },
            label = { Text("Тесты") }
        )
        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = {
                onTabSelected(3)
                onNavigateToProfile()
            },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Профиль") }
        )
    }
}