package com.example.procareerv2.presentation.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.procareerv2.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = true) {
        // Добавляем таймаут для проверки логина
        val result = withTimeoutOrNull(3000) { // 3 секунды таймаут
            try {
                if (viewModel.isFirstLaunch()) {
                    onNavigateToOnboarding()
                } else {
                    val isLoggedIn = viewModel.isLoggedIn()
                    if (isLoggedIn) {
                        onNavigateToHome()
                    } else {
                        onNavigateToLogin()
                    }
                }
                true
            } catch (e: Exception) {
                false
            }
        }

        // Если таймаут истек или произошла ошибка, переходим на экран логина
        if (result != true) {
            delay(1000) // Небольшая задержка для отображения сплэш-экрана
            onNavigateToLogin()
        }

        isLoading = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(120.dp)
            )

            // App name
            Text(
                text = "ProКарьеру",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Tagline
            Text(
                text = "Твой карьерный путь начинается здесь...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )

            // Loading indicator
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(top = 32.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}