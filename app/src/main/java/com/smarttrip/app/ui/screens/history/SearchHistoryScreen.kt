package com.smarttrip.app.ui.screens.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smarttrip.app.data.remote.models.SearchHistoryDto
import com.smarttrip.app.ui.theme.*
import com.smarttrip.app.ui.viewmodel.AuthUiState
import com.smarttrip.app.ui.viewmodel.AuthViewModel
import com.smarttrip.app.ui.viewmodel.SearchHistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchHistoryScreen(
    onLoginRequired: () -> Unit,
    onSearchAgain: (String) -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    viewModel: SearchHistoryViewModel = hiltViewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    val history by viewModel.history.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    var showClearConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthUiState.Authenticated -> viewModel.loadHistory()
            is AuthUiState.Unauthenticated -> onLoginRequired()
            else -> Unit
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Historique", fontWeight = FontWeight.Bold) },
            actions = {
                if (history.isNotEmpty()) {
                    IconButton(onClick = { showClearConfirm = true }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Tout effacer", tint = Rose500)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        when {
            loading -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(5) { ShimmerHistoryCard() }
            }
            error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("⚠️", fontSize = 40.sp)
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.loadHistory() }) { Text("Réessayer") }
                }
            }
            history.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔍", fontSize = 56.sp)
                    Text("Aucune recherche", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Vos recherches apparaîtront ici", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(history, key = { it.id }) { entry ->
                    HistoryCard(
                        entry = entry,
                        onDelete = { viewModel.deleteEntry(entry.id) },
                        onSearchAgain = {
                            val params = buildString {
                                append("origin=${entry.originCode}")
                                append("&destination=${entry.destinationCode}")
                                append("&departureDate=${entry.departureDate}")
                                if (entry.returnDate != null) append("&returnDate=${entry.returnDate}")
                                append("&passengers=${entry.passengers}")
                                append("&class=${entry.cabinClass}")
                            }
                            onSearchAgain(params)
                        }
                    )
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Effacer tout l'historique ?") },
            text = { Text("Toutes vos recherches seront supprimées.") },
            confirmButton = {
                TextButton(onClick = { showClearConfirm = false; viewModel.clearAll() }) {
                    Text("Effacer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Annuler") }
            }
        )
    }
}

@Composable
fun HistoryCard(
    entry: SearchHistoryDto,
    onDelete: () -> Unit,
    onSearchAgain: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(entry.originCode, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Slate900)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.padding(horizontal = 6.dp).size(16.dp), tint = Blue600)
                    Text(entry.destinationCode, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Slate900)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Supprimer", tint = Slate500, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(shape = RoundedCornerShape(6.dp), color = Slate100) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Icon(Icons.Default.DateRange, null, modifier = Modifier.size(12.dp), tint = Slate500)
                        Spacer(Modifier.width(4.dp))
                        Text(entry.departureDate, style = MaterialTheme.typography.labelSmall, color = Slate700)
                    }
                }
                if (entry.returnDate != null) {
                    Surface(shape = RoundedCornerShape(6.dp), color = Slate100) {
                        Text("↩ ${entry.returnDate}", style = MaterialTheme.typography.labelSmall, color = Slate700, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                }
                Surface(shape = RoundedCornerShape(6.dp), color = Slate100) {
                    Text("${entry.passengers} pax", style = MaterialTheme.typography.labelSmall, color = Slate700, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
                Surface(shape = RoundedCornerShape(6.dp), color = Slate100) {
                    Text(entry.cabinClass.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall, color = Slate700, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onSearchAgain,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue600)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Relancer la recherche", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
// ─── Shimmer skeleton carte historique ───────────────────────────────────────
@Composable
fun ShimmerHistoryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(modifier = Modifier.width(160.dp).height(16.dp).background(Slate100, RoundedCornerShape(4.dp)))
                Box(modifier = Modifier.width(24.dp).height(16.dp).background(Slate100, RoundedCornerShape(4.dp)))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.width(80.dp).height(24.dp).background(Slate100, RoundedCornerShape(6.dp)))
                Box(modifier = Modifier.width(50.dp).height(24.dp).background(Slate100, RoundedCornerShape(6.dp)))
                Box(modifier = Modifier.width(60.dp).height(24.dp).background(Slate100, RoundedCornerShape(6.dp)))
            }
            Box(modifier = Modifier.fillMaxWidth().height(36.dp).background(Slate100, RoundedCornerShape(10.dp)))
        }
    }
}