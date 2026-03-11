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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyEmailScreen(
    email: String,
    onVerified: () -> Unit,
    onBack: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    var code by remember { mutableStateOf("") }
    var resendEnabled by remember { mutableStateOf(true) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.EmailVerified) onVerified()
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = Slate700)
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
            Text("Vérifiez votre email", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Slate900)
            Spacer(Modifier.height(8.dp))
            Text(
                "Un code à 6 chiffres a été envoyé à\n$email",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate500,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))

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
                label = { Text("Code de vérification") },
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
                    Text("Vérifier", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
            TextButton(
                onClick = { viewModel.resendVerification(email); resendEnabled = false },
                enabled = resendEnabled
            ) {
                Text("Renvoyer le code", color = if (resendEnabled) Blue600 else Slate500)
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = Slate700)
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
            Text("Mot de passe oublié", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Slate900)
            Spacer(Modifier.height(8.dp))
            Text(
                "Entrez votre email pour recevoir un lien de réinitialisation",
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
                        Text("Email envoyé ! Vérifiez votre boîte mail.", style = MaterialTheme.typography.bodySmall, color = Slate900)
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
                    label = { Text("Email") },
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
                        Text("Envoyer le lien", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = Slate700)
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
            Text("Nouveau mot de passe", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Slate900)
            Spacer(Modifier.height(8.dp))
            Text(
                "Choisissez un mot de passe sécurisé",
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
                label = { Text("Nouveau mot de passe") },
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
                label = { Text("Confirmer le mot de passe") },
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
                        errorMsg = "Les mots de passe ne correspondent pas"
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
                    Text("Réinitialiser", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
