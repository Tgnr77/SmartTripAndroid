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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.smarttrip.app.data.remote.models.FlightDto
import com.smarttrip.app.data.remote.models.FlightSegmentDto
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
                            passengers = passengers,
                            returnDate = returnDate.ifBlank { null },
                            cabinClass = cabinClass,
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
    passengers: Int = 1,
    returnDate: String? = null,
    cabinClass: String = "economy",
    onToggleFavorite: () -> Unit
) {
    val language by LanguageManager.language.collectAsState()
    val strings = AppStrings.forLanguage(language)
    val context = LocalContext.current

    // Code IATA de la compagnie (stocké dans flightNumber après notre mapping)
    val airlineCode = flight.outbound.flightNumber
    val airlineName = flight.outbound.airline ?: airlineCode ?: ""
    val logoUrl = airlineCode?.let {
        "https://www.gstatic.com/flights/airline_logos/70px/${it.uppercase()}.png"
    }

    fun formatTime(dt: String): String {
        // "2026-05-21T09:30:00" ou "2026-05-21T09:30" → "09:30"
        return dt.substringAfter('T').take(5)
    }
    fun formatDate(dt: String): String {
        // "2026-05-21T09:30:00" → "21/05"
        val d = dt.substringBefore('T')
        val parts = d.split("-")
        return if (parts.size == 3) "${parts[2]}/${parts[1]}" else d
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Ligne 1 : Logo + Compagnie + Score AI + Favori ──────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Logo compagnie
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate100),
                        contentAlignment = Alignment.Center
                    ) {
                        if (logoUrl != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(logoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = airlineName,
                                modifier = Modifier.size(28.dp),
                                contentScale = ContentScale.Fit,
                                error = null
                            )
                        } else {
                            Icon(Icons.Default.Flight, contentDescription = null,
                                tint = Blue600, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            airlineName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (airlineCode != null) {
                            Text(
                                airlineCode,
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate500
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (flight.aiScore != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when {
                                flight.aiScore >= 70 -> Color(0xFFDCFCE7)
                                flight.aiScore >= 45 -> Blue50
                                else -> Color(0xFFFFF3CD)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Stars, null,
                                    tint = when {
                                        flight.aiScore >= 70 -> Color(0xFF16A34A)
                                        flight.aiScore >= 45 -> Blue600
                                        else -> Color(0xFFD97706)
                                    },
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    "${flight.aiScore}/100",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        flight.aiScore >= 70 -> Color(0xFF16A34A)
                                        flight.aiScore >= 45 -> Blue600
                                        else -> Color(0xFFD97706)
                                    }
                                )
                            }
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                        Icon(
                            if (flight.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (flight.isFavorite) Rose500 else Slate300,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Ligne 2 : Trajet aller ──────────────────────────────────────
            FlightSegmentRow(segment = flight.outbound, strings = strings)

            // Trajet retour s'il existe
            if (flight.inbound != null) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = Slate100,
                    thickness = 1.dp
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                    Icon(Icons.Default.FlightLand, null, tint = Slate500, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Retour", style = MaterialTheme.typography.labelSmall, color = Slate500)
                }
                FlightSegmentRow(segment = flight.inbound, strings = strings)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Slate200)

            // ── Ligne 3 : Prix + Réserver ────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "${flight.price.toInt()} ${flight.currency}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Blue600
                    )
                    Text(
                        "${strings.perPerson} · $passengers pax",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500
                    )
                }
                if (flight.bookingLink != null) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(flight.bookingLink))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(strings.btnBook, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ─── Segment de vol (aller ou retour) ────────────────────────────────────────
@Composable
private fun FlightSegmentRow(segment: FlightSegmentDto, strings: AppStrings) {
    fun formatTime(dt: String) = dt.substringAfter('T').take(5).ifBlank { dt.take(5) }
    fun formatDate(dt: String): String {
        val d = dt.substringBefore('T')
        val p = d.split("-")
        return if (p.size == 3) "${p[2]}/${p[1]}" else d
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Départ
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                formatTime(segment.departureTime),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Text(
                segment.departureAirport,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Slate700
            )
            Text(
                formatDate(segment.departureTime),
                style = MaterialTheme.typography.labelSmall,
                color = Slate400
            )
        }

        // Centre : durée + icône + stops
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            val h = segment.duration / 60
            val m = segment.duration % 60
            Text(
                "${h}h${m.toString().padStart(2, '0')}",
                style = MaterialTheme.typography.labelSmall,
                color = Slate500
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(width = 24.dp, height = 1.dp)
                    .background(Slate300))
                Icon(
                    Icons.Default.Flight,
                    contentDescription = null,
                    tint = Blue600,
                    modifier = Modifier.size(16.dp)
                )
                Box(modifier = Modifier.size(width = 24.dp, height = 1.dp)
                    .background(Slate300))
            }
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (segment.stops == 0) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.errorContainer
            ) {
                Text(
                    if (segment.stops == 0) strings.labelDirect
                    else "${segment.stops} ${if (segment.stops > 1) strings.labelStops else strings.labelStop}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (segment.stops == 0) Color(0xFF16A34A) else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Arrivée
        Column(horizontalAlignment = Alignment.End) {
            Text(
                formatTime(segment.arrivalTime),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Text(
                segment.arrivalAirport,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Slate700
            )
            Text(
                formatDate(segment.arrivalTime),
                style = MaterialTheme.typography.labelSmall,
                color = Slate400
            )
        }
    }
}
