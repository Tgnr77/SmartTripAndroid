package com.smarttrip.app.ui.screens.favorites

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
import com.smarttrip.app.data.remote.models.FavoriteDto
import com.smarttrip.app.ui.theme.*
import com.smarttrip.app.ui.viewmodel.AuthUiState
import com.smarttrip.app.ui.viewmodel.AuthViewModel
import com.smarttrip.app.ui.viewmodel.FavoritesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onLoginRequired: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthUiState.Authenticated -> viewModel.loadFavorites()
            is AuthUiState.Unauthenticated -> onLoginRequired()
            else -> Unit
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Mes favoris", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        when {
            loading -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(4) { ShimmerFavoriteCard() }
            }
            error != null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("⚠️", fontSize = 40.sp)
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.loadFavorites() }) { Text("Réessayer") }
                }
            }
            favorites.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("💙", fontSize = 56.sp)
                    Text("Aucun favori", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Ajoutez des vols depuis les résultats", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(favorites, key = { it.id ?: 0 }) { fav ->
                    FavoriteCard(fav, onDelete = { viewModel.deleteFavorite(fav.id?.toString() ?: "") })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteCard(favorite: FavoriteDto, onDelete: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }

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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(favorite.originCode, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Slate900)
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.padding(horizontal = 8.dp), tint = Blue600)
                        Text(favorite.destinationCode, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Slate900)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(favorite.departureDate, style = MaterialTheme.typography.bodySmall, color = Slate500)
                    if (favorite.returnDate != null) {
                        Text("Retour : ${favorite.returnDate}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    }
                    if (favorite.airlineName != null) {
                        Spacer(Modifier.height(4.dp))
                        Surface(shape = RoundedCornerShape(6.dp), color = Slate100) {
                            Text(favorite.airlineName, style = MaterialTheme.typography.labelSmall, color = Slate700, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Blue50) {
                        Text(
                            "${favorite.price.toInt()} ${favorite.currency}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Blue600,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    if (favorite.aiScore != null) {
                        Spacer(Modifier.height(4.dp))
                        Text("IA: ${favorite.aiScore}/100", style = MaterialTheme.typography.labelSmall, color = Slate500)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = Slate200)
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val stops = favorite.stops
                    Surface(shape = RoundedCornerShape(6.dp), color = if (stops == 0) Blue50 else MaterialTheme.colorScheme.surfaceVariant) {
                        Text(
                            if (stops == 0) "Direct" else "$stops escale${if (stops > 1) "s" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (stops == 0) Blue600 else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(shape = RoundedCornerShape(6.dp), color = Slate100) {
                        Text(
                            favorite.cabinClass.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate700,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                IconButton(onClick = { showConfirm = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Rose500, modifier = Modifier.size(18.dp))
                }
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Supprimer ce favori ?") },
            text = { Text("Cette action est irréversible.") },
            confirmButton = {
                TextButton(onClick = { showConfirm = false; onDelete() }) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Annuler") }
            }
        )
    }
}
// ─── Shimmer skeleton carte favori ───────────────────────────────────────────
@Composable
fun ShimmerFavoriteCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Slate200)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.width(140.dp).height(18.dp).background(Slate100, RoundedCornerShape(4.dp)))
                Box(modifier = Modifier.width(100.dp).height(12.dp).background(Slate100, RoundedCornerShape(4.dp)))
                Box(modifier = Modifier.width(80.dp).height(12.dp).background(Slate100, RoundedCornerShape(4.dp)))
            }
            Box(modifier = Modifier.width(60.dp).height(28.dp).background(Slate100, RoundedCornerShape(8.dp)))
        }
    }
}