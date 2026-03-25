package com.smarttrip.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smarttrip.app.ui.theme.*
import com.smarttrip.app.ui.viewmodel.AuthUiState
import com.smarttrip.app.ui.viewmodel.AuthViewModel
import com.smarttrip.app.ui.language.AppStrings
import com.smarttrip.app.ui.language.LanguageManager
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyEmailScreen(
    email: String,
    onVerified: () -> Unit,
    onExpired: () -> Unit,
    onBack: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    var code by remember { mutableStateOf("") }
    var timeLeft by remember { mutableStateOf(600) }   // 10 min
    var resendCooldown by remember { mutableStateOf(0) }
    var showExpiredDialog by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val language by LanguageManager.language.collectAsState()
    val strings = AppStrings.forLanguage(language)

    // Navigation on success
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.EmailVerified) onVerified()
    }

    // 10-minute countdown
    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
        // Timer expired: delete account then show dialog
        viewModel.deleteAccount {}
        showExpiredDialog = true
    }

    // 60s resend cooldown
    LaunchedEffect(resendCooldown) {
        if (resendCooldown > 0) {
            delay(1000L)
            resendCooldown--
        }
    }

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val timerText = "%02d:%02d".format(minutes, seconds)
    val isTimerCritical = timeLeft <= 60

    // Expiry dialog
    if (showExpiredDialog) {
        AlertDialog(
            onDismissRequest = {},
            icon = { Text("⌛", fontSize = 32.sp) },
            title = { Text(strings.verifyExpiredTitle, fontWeight = FontWeight.Bold) },
            text = { Text(strings.verifyExpiredText, textAlign = TextAlign.Center) },
            confirmButton = {
                Button(
                    onClick = onExpired,
                    colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                ) { Text(strings.verifyExpiredBtn, fontWeight = FontWeight.SemiBold) }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Brush.horizontalGradient(listOf(Blue600, Blue400)))
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back, tint = Slate700)
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp).padding(top = 8.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Brush.linearGradient(listOf(Blue600, Blue400)), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("✉️", fontSize = 34.sp)
            }
            Spacer(Modifier.height(20.dp))
            Text(strings.verifyEmailTitle, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Slate900)
            Spacer(Modifier.height(8.dp))
            Text(
                strings.verifyEmailSubtitle.format(email),
                style = MaterialTheme.typography.bodyMedium,
                color = Slate500,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            // Timer display
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isTimerCritical) MaterialTheme.colorScheme.errorContainer
                        else Blue600.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        tint = if (isTimerCritical) MaterialTheme.colorScheme.error else Blue600,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${strings.verifyTimerLabel} ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isTimerCritical) MaterialTheme.colorScheme.error else Blue600
                    )
                    Text(
                        timerText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isTimerCritical) MaterialTheme.colorScheme.error else Blue600
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            if (uiState is AuthUiState.Error) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        (uiState as AuthUiState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = code,
                onValueChange = { if (it.length <= 6) code = it },
                label = { Text(strings.verifyCodeLabel) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("123456") },
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { viewModel.verifyEmail(email, code) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = code.length == 6 && uiState !is AuthUiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = Blue600)
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text(strings.btnVerify, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
            TextButton(
                onClick = { viewModel.resendVerification(email); resendCooldown = 60 },
                enabled = resendCooldown == 0
            ) {
                val resendText = if (resendCooldown > 0)
                    "${strings.verifyTimerLabel} ${resendCooldown}s"
                else
                    strings.btnResendCode
                Text(resendText, color = if (resendCooldown == 0) Blue600 else Slate500)
            }
        }
    }
}


@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val language by LanguageManager.language.collectAsState()
    val strings = AppStrings.forLanguage(language)

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Brush.horizontalGradient(listOf(Blue600, Blue400)))
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back, tint = Slate700)
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp).padding(top = 8.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Brush.linearGradient(listOf(Blue600, Blue400)), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🔑", fontSize = 34.sp)
            }
            Spacer(Modifier.height(20.dp))
            Text(strings.forgotPasswordTitle, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Slate900)
            Spacer(Modifier.height(8.dp))
            Text(
                strings.forgotPasswordSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Slate500,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))

            if (sent) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Emerald500.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("✅", fontSize = 18.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(strings.forgotPasswordSuccess, style = MaterialTheme.typography.bodySmall, color = Slate900)
                    }
                }
            } else {
                if (errorMsg.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(errorMsg, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(16.dp))
                }
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(strings.fieldEmail) },
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = Slate500) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        loading = true
                        viewModel.forgotPassword(email) { success, msg ->
                            loading = false
                            if (success) sent = true else errorMsg = msg
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = email.isNotBlank() && !loading,
                    colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                ) {
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Text(strings.btnSendLink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    token: String,
    onReset: () -> Unit,
    onBack: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val language by LanguageManager.language.collectAsState()
    val strings = AppStrings.forLanguage(language)

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Brush.horizontalGradient(listOf(Blue600, Blue400)))
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back, tint = Slate700)
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp).padding(top = 8.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Brush.linearGradient(listOf(Blue600, Blue400)), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🔒", fontSize = 34.sp)
            }
            Spacer(Modifier.height(20.dp))
            Text(strings.resetPasswordTitle, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Slate900)
            Spacer(Modifier.height(8.dp))
            Text(
                strings.resetPasswordSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Slate500,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))

            if (errorMsg.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(errorMsg, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(strings.fieldNewPassword) },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = Slate500) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it },
                label = { Text(strings.fieldConfirmPassword) },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = Slate500) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (password != confirm) {
                        errorMsg = strings.passwordMismatch
                        return@Button
                    }
                    loading = true
                    viewModel.resetPassword(token, password) { success, msg ->
                        loading = false
                        if (success) onReset() else errorMsg = msg
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = password.isNotBlank() && confirm.isNotBlank() && !loading,
                colors = ButtonDefaults.buttonColors(containerColor = Blue600)
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                        Text(strings.btnResetPassword, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
