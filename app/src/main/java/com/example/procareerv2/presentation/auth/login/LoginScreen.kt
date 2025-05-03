package com.example.procareerv2.presentation.auth.login

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.procareerv2.R
import com.example.procareerv2.presentation.common.components.ProCareerButton
import com.example.procareerv2.presentation.common.components.ProCareerTextField
import android.content.Context
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.procareerv2.util.NetworkUtils

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val showNoInternetDialog = remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordMessage by remember { mutableStateOf("") }

    LaunchedEffect(key1 = true) @androidx.annotation.RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            showNoInternetDialog.value = true
        }
    }

    // Диалог отсутствия интернета
    if (showNoInternetDialog.value) {
        AlertDialog(
            onDismissRequest = { showNoInternetDialog.value = false },
            title = { Text("Нет подключения к интернету") },
            text = { Text("Для входа в приложение требуется подключение к интернету. Пожалуйста, проверьте ваше соединение и попробуйте снова.") },
            confirmButton = {
                Button(onClick = { showNoInternetDialog.value = false }) {
                    Text("OK")
                }
            }
        )
    }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Фиксированная верхняя часть - одинаковая для обоих экранов
        Spacer(modifier = Modifier.height(48.dp)) // Фиксированный отступ сверху

        // Логотип - фиксированный размер и отступы
        Image(
            painter = painterResource(id = R.drawable.logo2),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(160.dp)
                .padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(40.dp)) // Фиксированный отступ

        // Вкладки - фиксированное положение
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Text(
                        "Войти",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = {
                    selectedTabIndex = 1
                    onNavigateToRegister()
                },
                text = {
                    Text(
                        "Регистрация",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                }
            )
        }

        // Изменяемая часть - содержимое экрана входа
        Spacer(modifier = Modifier.height(32.dp))

        // Email field
        ProCareerTextField(
            value = uiState.email,
            onValueChange = { viewModel.onEmailChanged(it) },
            label = "Email",
            leadingIcon = Icons.Default.Email,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            isError = uiState.emailError != null,
            errorMessage = uiState.emailError,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password field
        ProCareerTextField(
            value = uiState.password,
            onValueChange = { viewModel.onPasswordChanged(it) },
            label = "Пароль",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            isError = uiState.passwordError != null,
            errorMessage = uiState.passwordError,
            modifier = Modifier.fillMaxWidth()
        )

        // Forgot password
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Забыли пароль?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    if (uiState.email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(uiState.email).matches()) {
                        forgotPasswordMessage = "Новый пароль отправлен на ${uiState.email}"
                    } else {
                        forgotPasswordMessage = "Такой почты не найдено"
                    }
                    showForgotPasswordDialog = true
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Login button
        ProCareerButton(
            text = "Войти",
            onClick = { viewModel.login() },
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        if (showForgotPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showForgotPasswordDialog = false },
                title = { Text("Восстановление пароля") },
                text = { Text(forgotPasswordMessage) },
                confirmButton = {
                    Button(onClick = { showForgotPasswordDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }

    }
}