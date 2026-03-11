package com.smarttrip.app.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smarttrip.app.ui.theme.*
import com.smarttrip.app.ui.viewmodel.AuthUiState
import com.smarttrip.app.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onLoginRequired: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.uiState.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    var showLogoutConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthUiState.Unauthenticated) onLoginRequired()
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Gradient header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Blue900, Blue700)))
                .padding(top = 40.dp, bottom = 32.dp, start = 24.dp, end = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (user != null) {
                        val initials = listOf(user!!.firstName, user!!.lastName)
                            .mapNotNull { it.firstOrNull()?.uppercase() }
                            .joinToString("")
                        Text(
                            initials.ifEmpty { "?" },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    } else {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(40.dp), tint = Color.White)
                    }
                }
                Spacer(Modifier.height(12.dp))
                if (user != null) {
                    Text(
                        "${user!!.firstName} ${user!!.lastName}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(user!!.email, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.75f))
                    if (user!!.isVerified) {
                        Spacer(Modifier.height(10.dp))
                        Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.15f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.VerifiedUser, null, tint = Emerald500, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Email vérifié", style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Info rows
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "INFORMATIONS DU COMPTE",
                style = MaterialTheme.typography.labelSmall,
                color = Slate500,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (user != null) {
                ProfileRow(Icons.Default.Person, "Prénom", user!!.firstName)
                HorizontalDivider(color = Slate200)
                ProfileRow(Icons.Default.Person, "Nom", user!!.lastName)
                HorizontalDivider(color = Slate200)
                ProfileRow(Icons.Default.Email, "Email", user!!.email)
                if (user!!.createdAt != null) {
                    HorizontalDivider(color = Slate200)
                    val date = try { user!!.createdAt!!.take(10) } catch (e: Exception) { user!!.createdAt!! }
                    ProfileRow(Icons.Default.CalendarToday, "Membre depuis", date)
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            Spacer(Modifier.height(32.dp))
            OutlinedButton(
                onClick = { showLogoutConfirm = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose500),
                border = BorderStroke(1.dp, Rose500)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Se déconnecter", fontWeight = FontWeight.SemiBold)
            }
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Se déconnecter ?") },
            text = { Text("Vous devrez vous reconnecter pour accéder à vos favoris et à votre historique.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirm = false
                    viewModel.logout()
                    onLogout()
                }) {
                    Text("Déconnexion", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Annuler") }
            }
        )
    }
}

@Composable
private fun ProfileRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Blue600, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Slate500)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Slate900)
        }
    }
}
