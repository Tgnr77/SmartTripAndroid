package com.smarttrip.app.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smarttrip.app.data.local.Airport
import com.smarttrip.app.data.local.searchAirports
import com.smarttrip.app.data.remote.models.TrendingDestinationDto
import com.smarttrip.app.ui.language.AppStrings
import com.smarttrip.app.ui.language.LanguageManager
import com.smarttrip.app.ui.language.LanguageToggleButton
import com.smarttrip.app.ui.theme.*
import com.smarttrip.app.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSearch: (String) -> Unit,
    onInspiration: () -> Unit = {},
    prefillDestCode: String = "",
    prefillDestName: String = "",
    viewModel: HomeViewModel = hiltViewModel()
) {
    var origin by remember { mutableStateOf("") }
    var originCode by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf(prefillDestName) }
    var destinationCode by remember { mutableStateOf(prefillDestCode) }

    // Apply prefill from inspiration navigation
    LaunchedEffect(prefillDestCode, prefillDestName) {
        if (prefillDestCode.isNotBlank()) {
            destinationCode = prefillDestCode
            destination = prefillDestName.ifBlank { prefillDestCode }
        }
    }
    var departureDate by remember { mutableStateOf("") }
    var returnDate by remember { mutableStateOf("") }
    var passengers by remember { mutableStateOf(1) }
    var cabinClass by remember { mutableStateOf("economy") }
    var tripType by remember { mutableStateOf("roundtrip") }
    var directOnly by remember { mutableStateOf(false) }

    var originSuggestions by remember { mutableStateOf(listOf<Airport>()) }
    var destinationSuggestions by remember { mutableStateOf(listOf<Airport>()) }
    var showOriginDropdown by remember { mutableStateOf(false) }
    var showDestinationDropdown by remember { mutableStateOf(false) }

    val trendingDestinations by viewModel.trendingDestinations.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val focusManager = LocalFocusManager.current

    val canSearch = originCode.isNotBlank() && destinationCode.isNotBlank() && departureDate.isNotBlank()

    val language by LanguageManager.language.collectAsState()
    val strings = AppStrings.forLanguage(language)

    var showDepartureDatePicker by remember { mutableStateOf(false) }
    var showReturnDatePicker by remember { mutableStateOf(false) }
    val departureDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    val returnDatePickerState = rememberDatePickerState()

    if (showDepartureDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDepartureDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = departureDatePickerState.selectedDateMillis
                    if (millis != null) {
                        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                        cal.timeInMillis = millis
                        departureDate = "%04d-%02d-%02d".format(
                            cal.get(java.util.Calendar.YEAR),
                            cal.get(java.util.Calendar.MONTH) + 1,
                            cal.get(java.util.Calendar.DAY_OF_MONTH)
                        )
                    }
                    showDepartureDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDepartureDatePicker = false }) { Text("Annuler") }
            }
        ) {
            DatePicker(state = departureDatePickerState, showModeToggle = false)
        }
    }

    if (showReturnDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showReturnDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = returnDatePickerState.selectedDateMillis
                    if (millis != null) {
                        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                        cal.timeInMillis = millis
                        returnDate = "%04d-%02d-%02d".format(
                            cal.get(java.util.Calendar.YEAR),
                            cal.get(java.util.Calendar.MONTH) + 1,
                            cal.get(java.util.Calendar.DAY_OF_MONTH)
                        )
                    }
                    showReturnDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showReturnDatePicker = false }) { Text("Annuler") }
            }
        ) {
            DatePicker(state = returnDatePickerState, showModeToggle = false)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // ─── Hero banner ─────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Primary600, Primary900)))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)
                ) {
                    Text(
                        "✈ SmartTrip",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        strings.homeHeroTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        strings.homeHeroSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                    )
                }
                LanguageToggleButton(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 16.dp)
                )
            }
        }

        // ─── Formulaire recherche ─────────────────────────────────────────
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-16).dp),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Type de voyage
                    val tripTypes = listOf(
                        "roundtrip" to "Aller-retour",
                        "oneway" to "Aller simple",
                        "multicity" to "Multi-villes"
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        tripTypes.forEachIndexed { index, (type, label) ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = tripTypes.size),
                                onClick = { tripType = type },
                                selected = tripType == type,
                                icon = {}
                            ) {
                                Text(label, maxLines = 1, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // ─── Champ départ avec autocomplete ──────────────────
                    Column {
                        OutlinedTextField(
                            value = origin,
                            onValueChange = {
                                origin = it
                                originCode = ""
                                originSuggestions = searchAirports(it)
                                showOriginDropdown = originSuggestions.isNotEmpty()
                            },
                            label = { Text("Ville ou aéroport de départ") },
                            leadingIcon = { Icon(Icons.Default.FlightTakeoff, null) },
                            trailingIcon = {
                                if (origin.isNotEmpty()) {
                                    IconButton(onClick = {
                                        origin = ""; originCode = ""; showOriginDropdown = false
                                    }) {
                                        Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { if (!it.isFocused) showOriginDropdown = false },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            isError = origin.isNotBlank() && originCode.isBlank()
                        )
                        if (showOriginDropdown) {
                            AirportDropdown(
                                suggestions = originSuggestions,
                                onSelect = { airport ->
                                    origin = "${airport.city} (${airport.code})"
                                    originCode = airport.code
                                    showOriginDropdown = false
                                    focusManager.clearFocus()
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Bouton swap
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        FilledTonalIconButton(onClick = {
                            val tmpLabel = origin; val tmpCode = originCode
                            origin = destination; originCode = destinationCode
                            destination = tmpLabel; destinationCode = tmpCode
                        }) { Icon(Icons.Default.SwapVert, contentDescription = "Inverser") }
                    }

                    // ─── Champ destination avec autocomplete ──────────────
                    Column {
                        OutlinedTextField(
                            value = destination,
                            onValueChange = {
                                destination = it
                                destinationCode = ""
                                destinationSuggestions = searchAirports(it)
                                showDestinationDropdown = destinationSuggestions.isNotEmpty()
                            },
                            label = { Text("Ville ou aéroport de destination") },
                            leadingIcon = { Icon(Icons.Default.FlightLand, null) },
                            trailingIcon = {
                                if (destination.isNotEmpty()) {
                                    IconButton(onClick = {
                                        destination = ""; destinationCode = ""; showDestinationDropdown = false
                                    }) {
                                        Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { if (!it.isFocused) showDestinationDropdown = false },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            isError = destination.isNotBlank() && destinationCode.isBlank()
                        )
                        if (showDestinationDropdown) {
                            AirportDropdown(
                                suggestions = destinationSuggestions,
                                onSelect = { airport ->
                                    destination = "${airport.city} (${airport.code})"
                                    destinationCode = airport.code
                                    showDestinationDropdown = false
                                    focusManager.clearFocus()
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // ─── Dates ────────────────────────────────────────────
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DateSelectionCard(
                            label = "Départ",
                            dateValue = departureDate,
                            modifier = Modifier.weight(1f),
                            onClick = { showDepartureDatePicker = true },
                            onClear = { departureDate = "" }
                        )
                        if (tripType == "roundtrip") {
                            DateSelectionCard(
                                label = "Retour",
                                dateValue = returnDate,
                                modifier = Modifier.weight(1f),
                                onClick = { showReturnDatePicker = true },
                                onClear = { returnDate = "" }
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // ─── Passagers + Classe ───────────────────────────────
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(4.dp))
                            FilledTonalIconButton(
                                onClick = { if (passengers > 1) passengers-- },
                                modifier = Modifier.size(32.dp)
                            ) { Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp)) }
                            Text(
                                "$passengers",
                                modifier = Modifier.padding(horizontal = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            FilledTonalIconButton(
                                onClick = { if (passengers < 9) passengers++ },
                                modifier = Modifier.size(32.dp)
                            ) { Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp)) }
                            Spacer(Modifier.width(4.dp))
                            Text(
                                if (passengers > 1) "passagers" else "passager",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        var expandedClass by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(
                                onClick = { expandedClass = true },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    when (cabinClass) {
                                        "economy" -> "Éco"
                                        "business" -> "Business"
                                        "first" -> "Première"
                                        else -> cabinClass
                                    },
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
                            }
                            DropdownMenu(expanded = expandedClass, onDismissRequest = { expandedClass = false }) {
                                listOf("economy" to "Économique", "business" to "Business", "first" to "Première classe")
                                    .forEach { (key, label) ->
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            onClick = { cabinClass = key; expandedClass = false },
                                            leadingIcon = {
                                                if (cabinClass == key) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                                            }
                                        )
                                    }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { directOnly = !directOnly }
                    ) {
                        Checkbox(checked = directOnly, onCheckedChange = { directOnly = it })
                        Text("Vols directs uniquement", style = MaterialTheme.typography.bodyMedium)
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            val params = buildString {
                                append("origin=$originCode")
                                append("&destination=$destinationCode")
                                append("&departureDate=$departureDate")
                                if (tripType == "roundtrip" && returnDate.isNotBlank()) append("&returnDate=$returnDate")
                                append("&passengers=$passengers")
                                append("&class=$cabinClass")
                                append("&nonStop=$directOnly")
                                append("&tripType=$tripType")
                            }
                            onSearch(params)
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = canSearch,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Search, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Rechercher des vols", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    if (!canSearch && (origin.isNotBlank() || destination.isNotBlank())) {
                        Text(
                            "Sélectionnez aéroport dans la liste et une date",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // ─── Bannière Mode Inspiration ────────────────────────────────────
        item {
            Card(
                onClick = onInspiration,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-8).dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(0.dp),
                colors = CardDefaults.cardColors(containerColor = Blue50),
                border = BorderStroke(1.dp, Blue100)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✨", fontSize = 22.sp)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Mode Inspiration",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Blue900
                        )
                        Text(
                            "L'IA choisit votre destination selon vos envies",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        null,
                        tint = Blue600,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // ─── Destinations tendance ────────────────────────────────────────
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Destinations tendance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (loading) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(3) { ShimmerCard() }
                    }
                } else if (trendingDestinations.isEmpty()) {
                    Text(
                        "Aucune destination disponible pour le moment",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(trendingDestinations.filter { !it.code.isNullOrBlank() }) { dest ->
                            TrendingCard(dest) {
                                destination = "${dest.city ?: ""} (${dest.code ?: ""})"
                                destinationCode = dest.code ?: ""
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Date Selection Card ──────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelectionCard(
    label: String,
    dateValue: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onClear: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 56.dp)
                .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.DateRange,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    if (dateValue.isEmpty()) "Sélectionner" else dateValue,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (dateValue.isEmpty()) FontWeight.Normal else FontWeight.Medium,
                    color = if (dateValue.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
            if (dateValue.isNotEmpty()) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

// ─── Dropdown suggestions aéroports ──────────────────────────────────────────
@Composable
fun AirportDropdown(suggestions: List<Airport>, onSelect: (Airport) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column {
            suggestions.forEachIndexed { index, airport ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(airport) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            airport.code,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            airport.city,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            airport.country,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (index < suggestions.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

// ─── Trending Card ────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendingCard(dest: TrendingDestinationDto, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(160.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Slate200)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Brush.linearGradient(listOf(Blue600, Blue800))),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        dest.code ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text("✈", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(dest.city ?: "Destination", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(dest.country ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                if (dest.averagePrice != null) {
                    Spacer(Modifier.height(6.dp))
                    Surface(shape = RoundedCornerShape(6.dp), color = Blue50) {
                        Text(
                            "~${dest.averagePrice.toInt()} €",
                            style = MaterialTheme.typography.labelMedium,
                            color = Blue600,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─── Shimmer placeholder ──────────────────────────────────────────────────────
@Composable
fun ShimmerCard() {
    Card(modifier = Modifier.width(150.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Column(modifier = Modifier.padding(10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(14.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(12.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                )
            }
        }
    }
}
