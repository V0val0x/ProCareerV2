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
import androidx.compose.runtime.collectAsState
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

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    // Получаем состояние авторизации
    val authState by viewModel.authState.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(120.dp)
            )
            
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp),
                color = Color.White
            )
        }
    }
    
    // Обрабатываем состояние авторизации
    LaunchedEffect(authState) {
        when (authState) {
            AuthState.FirstLaunch -> {
                delay(1000) // Задержка для показа сплэш-экрана
                onNavigateToOnboarding()
            }
            AuthState.Authenticated -> {
                delay(1000) // Задержка для показа сплэш-экрана
                onNavigateToHome()
            }
            AuthState.Unauthenticated -> {
                delay(1000) // Задержка для показа сплэш-экрана
                onNavigateToLogin()
            }
            AuthState.Loading -> {
                // Продолжаем показывать загрузчик
            }
        }
    }
}