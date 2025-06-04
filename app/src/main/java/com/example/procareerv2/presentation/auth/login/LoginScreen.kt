package com.example.procareerv2.presentation.auth.login

import android.util.Log
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.procareerv2.util.NetworkUtils
import kotlinx.coroutines.launch

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
    var navigationAttempted by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Log.d("LoginScreen", "Composing LoginScreen with: isLoggedIn=${uiState.isLoggedIn}, email=${uiState.email}")
    
    DisposableEffect(key1 = true) {
        Log.d("LoginScreen", "LoginScreen composed")
        onDispose {
            Log.d("LoginScreen", "LoginScreen disposed")
        }
    }

    LaunchedEffect(key1 = true) @androidx.annotation.RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            showNoInternetDialog.value = true
        }
    }
    
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn && !navigationAttempted) {
            Log.d("LoginScreen", "*** NAVIGATION TRIGGERED FROM LAUNCHED EFFECT ***")
            navigationAttempted = true
            onLoginSuccess()
        }
    }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp)) 

        Image(
            painter = painterResource(id = R.drawable.logo2),
            contentDescription = "App Logo",
            modifier = Modifier.size(240.dp)
        )

        Spacer(modifier = Modifier.height(32.dp)) 

        TabRow(
            selectedTabIndex = selectedTabIndex,
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
                onClick = { onNavigateToRegister() },
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

        Column(modifier = Modifier.padding(top = 32.dp)) {
            ProCareerTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChanged,
                label = "Email",
                leadingIcon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                isError = uiState.emailError != null,
                errorMessage = uiState.emailError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProCareerTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChanged,
                label = "Пароль",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                isError = uiState.passwordError != null,
                errorMessage = uiState.passwordError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = uiState.rememberMe,
                        onCheckedChange = viewModel::onRememberMeChanged
                    )
                    Text(
                        text = "Запомнить меня",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = "Забыли пароль?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        showForgotPasswordDialog = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            ProCareerButton(
                text = "Войти",
                onClick = {
                    coroutineScope.launch {
                        Log.d("LoginScreen", "Login button clicked, starting login")
                        val result = viewModel.loginDirect(uiState.email, uiState.password)
                        
                        result.onSuccess { 
                            Log.d("LoginScreen", "*** DIRECT NAVIGATION AFTER SUCCESSFUL LOGIN ***")
                            onLoginSuccess() 
                        }
                    }
                },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Сброс пароля") },
            text = {
                Column {
                    Text("Введите ваш email для сброса пароля")
                    Spacer(modifier = Modifier.height(8.dp))
                    ProCareerTextField(
                        value = forgotPasswordMessage,
                        onValueChange = { forgotPasswordMessage = it },
                        label = "Email",
                        leadingIcon = Icons.Default.Email,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // TODO: Implement password reset logic
                        showForgotPasswordDialog = false
                        forgotPasswordMessage = ""
                    }
                ) {
                    Text("Отправить")
                }
            },
            dismissButton = {
                Button(onClick = { showForgotPasswordDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}