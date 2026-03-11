package com.smarttrip.app.ui.screens.search

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smarttrip.app.data.remote.models.FlightDto
import com.smarttrip.app.ui.theme.*
import com.smarttrip.app.ui.viewmodel.SearchViewModel
import com.smarttrip.app.ui.language.AppStrings
import com.smarttrip.app.ui.language.LanguageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(
    args: Bundle?,
    onBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val origin = args?.getString("origin") ?: ""
    val destination = args?.getString("destination") ?: ""
    val departureDate = args?.getString("departureDate") ?: ""
    val returnDate = args?.getString("returnDate") ?: ""
    val passengers = args?.getInt("passengers", 1) ?: 1
    val cabinClass = args?.getString("class") ?: "economy"
    val nonStop = args?.getBoolean("nonStop", false) ?: false

    val flights by viewModel.flights.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    val language by LanguageManager.language.collectAsState()
    val strings = AppStrings.forLanguage(language)

    LaunchedEffect(origin, destination, departureDate) {
        if (origin.isNotBlank() && destination.isNotBlank() && departureDate.isNotBlank()) {
            viewModel.searchFlights(
                origin = origin,
                destination = destination,
                departureDate = departureDate,
                returnDate = returnDate.ifBlank { null },
                adults = passengers,
                cabinClass = cabinClass,
                nonStop = nonStop
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Accent strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Brush.horizontalGradient(listOf(Blue600, Blue400)))
        )
        TopAppBar(
            title = {
                Column {
                    Text("$origin → $destination", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("$departureDate  •  $passengers pax  •  $cabinClass", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        // Sort chips
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("best" to strings.sortBest, "price" to strings.sortPrice, "duration" to strings.sortDuration)
                .forEach { (key, label) ->
                    FilterChip(
                        selected = sortBy == key,
                        onClick = { viewModel.setSortBy(key) },
                        label = { Text(label, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Blue600,
                            selectedLabelColor = Color.White
                        )
                    )
                }
        }

        when {
            loading -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(5) { ShimmerFlightCard() }
                }
            }
            error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("⚠️", fontSize = 48.sp)
                        Text(error!!, color = MaterialTheme.colorScheme.error)
                        Button(onClick = {
                            viewModel.searchFlights(origin, destination, departureDate, returnDate.ifBlank { null }, passengers, cabinClass, nonStop)
                        }) { Text(strings.btnRetry) }
                    }
                }
            }
            flights.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("✈", fontSize = 56.sp)
                        Text(strings.noFlightsFound, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(strings.noFlightsSubtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "${flights.size} ${if (flights.size > 1) strings.flightWordPlural else strings.flightWord} ${if (flights.size > 1) strings.foundWordPlural else strings.foundWord}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(flights) { flight ->
                        FlightCard(
                            flight = flight,
                            onToggleFavorite = { viewModel.toggleFavorite(flight) }
                        )
                    }
                }
            }
        }
    }
}

// ─── Shimmer skeleton carte vol ───────────────────────────────────────────────
@Composable
fun ShimmerFlightCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(modifier = Modifier.width(120.dp).height(16.dp).background(Slate100, RoundedCornerShape(4.dp)))
                Box(modifier = Modifier.width(60.dp).height(24.dp).background(Slate100, RoundedCornerShape(8.dp)))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(40.dp).height(18.dp).background(Slate100, RoundedCornerShape(4.dp)))
                Box(modifier = Modifier.width(60.dp).height(12.dp).background(Slate100, RoundedCornerShape(4.dp)))
                Box(modifier = Modifier.width(40.dp).height(18.dp).background(Slate100, RoundedCornerShape(4.dp)))
            }
            Box(modifier = Modifier.fillMaxWidth().height(36.dp).background(Slate100, RoundedCornerShape(8.dp)))
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightCard(
    flight: FlightDto,
    onToggleFavorite: () -> Unit
) {
    val language by LanguageManager.language.collectAsState()
    val strings = AppStrings.forLanguage(language)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Score IA + Favori
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                if (flight.aiScore != null) {
                    Surface(shape = RoundedCornerShape(6.dp), color = Blue50) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Stars, null, tint = Blue600, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("${flight.aiScore}/100", style = MaterialTheme.typography.labelSmall, color = Blue600, fontWeight = FontWeight.Bold)
                        }
                    }
                } else { Spacer(Modifier.width(1.dp)) }
                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (flight.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (flight.isFavorite) Rose500 else Slate300,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            // Trajet
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(flight.outbound.departureAirport, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Slate900)
                    Text(flight.outbound.departureTime.take(16), style = MaterialTheme.typography.bodySmall, color = Slate500)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val h = flight.outbound.duration / 60
                    val m = flight.outbound.duration % 60
                    Text("${h}h${m.toString().padStart(2, '0')}", style = MaterialTheme.typography.labelSmall, color = Slate500)
                    Icon(Icons.Default.Flight, contentDescription = null, tint = Blue600, modifier = Modifier.size(18.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (flight.outbound.stops == 0) Blue50 else MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            if (flight.outbound.stops == 0) strings.labelDirect else "${flight.outbound.stops} ${if (flight.outbound.stops > 1) strings.labelStops else strings.labelStop}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (flight.outbound.stops == 0) Blue600 else MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(flight.outbound.arrivalAirport, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Slate900)
                    Text(flight.outbound.arrivalTime.take(16), style = MaterialTheme.typography.bodySmall, color = Slate500)
                }
            }

            // Compagnie
            if (flight.outbound.airline != null) {
                Spacer(Modifier.height(6.dp))
                Surface(shape = RoundedCornerShape(6.dp), color = Slate100) {
                    Text(
                        flight.outbound.airline,
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate700,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Slate200)

            // Prix + réserver
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "${flight.price.toInt()} ${flight.currency}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Blue600
                    )
                    Text(strings.perPerson, style = MaterialTheme.typography.labelSmall, color = Slate500)
                }
                if (flight.bookingLink != null) {
                    val context = LocalContext.current
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(flight.bookingLink))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(strings.btnBook, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
