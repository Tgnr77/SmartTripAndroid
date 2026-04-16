package com.smarttrip.app.ui.screens.inspiration

import android.app.DatePickerDialog
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
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
import kotlinx.coroutines.launch
import com.smarttrip.app.data.local.Airport
import com.smarttrip.app.data.local.searchAirports
import com.smarttrip.app.data.remote.models.InspirationDestinationDto
import com.smarttrip.app.ui.screens.home.AirportDropdown
import com.smarttrip.app.ui.theme.Accent400
import com.smarttrip.app.ui.theme.Primary600
import com.smarttrip.app.ui.theme.Primary900
import com.smarttrip.app.ui.viewmodel.InspirationUiState
import com.smarttrip.app.ui.viewmodel.InspirationViewModel
import com.smarttrip.app.ui.language.AppStrings
import com.smarttrip.app.ui.language.LanguageManager
import java.util.Calendar

// ─── Chip option data ─────────────────────────────────────────────────────────
private data class Option(val key: String, val label: String)

private val WEATHER_OPTIONS = listOf(
    Option("", "Peu importe"), Option("sunny", "☀️ Ensoleillé"), Option("cloudy", "⛅ Nuageux"),
    Option("rainy", "🌧️ Pluie"), Option("snowy", "❄️ Neige"), Option("stormy", "⚡ Orageux")
)
private val TEMP_OPTIONS = listOf(
    Option("", "Peu importe"), Option("tropical", "🌴 Tropical >28°"),
    Option("hot", "🔥 Chaud 20-28°"), Option("mild", "🌤️ Doux 12-20°"),
    Option("cool", "🌬️ Frais 5-12°"), Option("cold", "🧊 Froid <5°")
)
private val HUMIDITY_OPTIONS = listOf(
    Option("", "Peu importe"), Option("dry", "🏜️ Sèche <40%"),
    Option("normal", "✅ Normale 40-70%"), Option("humid", "💧 Humide >70%")
)
private val WIND_OPTIONS = listOf(
    Option("", "Peu importe"), Option("calm", "🍃 Calme <10km/h"),
    Option("moderate", "🌀 Modéré 10-30km/h"), Option("windy", "💨 Venteux >30km/h")
)
private val BUDGET_OPTIONS = listOf(
    Option("", "Peu importe"), Option("200", "≤ 200€"),
    Option("500", "≤ 500€"), Option("1000", "≤ 1 000€"),
    Option("2000", "≤ 2 000€")
)

// Gradient colors for the sheet
private val SheetBg    = Color(0xDA0D0D20)
private val AccentBlue  = Color(0xFF6366F1)
private val AccentPink  = Color(0xFFEC4899)
private val AccentAmber = Color(0xFFF59E0B)

// Continent chips
private data class ContinentOption(val key: String, val label: String, val icon: ImageVector)
private val CONTINENT_OPTIONS = listOf(
    ContinentOption("", "Tous", Icons.Default.Language),
    ContinentOption("europe", "Europe", Icons.Default.Public),
    ContinentOption("asia", "Asie", Icons.Default.Explore),
    ContinentOption("americas", "Amériques", Icons.Default.Map),
    ContinentOption("africa", "Afrique", Icons.Default.NaturePeople),
    ContinentOption("oceanie", "Océanie", Icons.Default.Waves),
)

// ─── Multi-airport cities ──────────────────────────────────────────────────────────
private data class AirportOption(val code: String, val name: String)

private val MULTI_AIRPORT_CITIES: Map<String, List<AirportOption>> = mapOf(
    "Paris"         to listOf(AirportOption("CDG", "Paris-Charles de Gaulle"), AirportOption("ORY", "Paris-Orly")),
    "London"        to listOf(AirportOption("LHR", "London Heathrow"), AirportOption("LGW", "London Gatwick"), AirportOption("STN", "London Stansted"), AirportOption("LCY", "London City")),
    "New York"      to listOf(AirportOption("JFK", "New York JFK"), AirportOption("LGA", "New York LaGuardia"), AirportOption("EWR", "Newark Liberty")),
    "Tokyo"         to listOf(AirportOption("NRT", "Tokyo Narita"), AirportOption("HND", "Tokyo Haneda")),
    "Chicago"       to listOf(AirportOption("ORD", "Chicago O'Hare"), AirportOption("MDW", "Chicago Midway")),
    "Los Angeles"   to listOf(AirportOption("LAX", "Los Angeles LAX"), AirportOption("BUR", "Burbank"), AirportOption("LGB", "Long Beach")),
    "Milan"         to listOf(AirportOption("MXP", "Milan Malpensa"), AirportOption("LIN", "Milan Linate"), AirportOption("BGY", "Orio al Serio")),
    "Rome"          to listOf(AirportOption("FCO", "Rome Fiumicino"), AirportOption("CIA", "Rome Ciampino")),
    "Istanbul"      to listOf(AirportOption("IST", "Istanbul Airport"), AirportOption("SAW", "Istanbul Sabiha Gökçen")),
    "Moscow"        to listOf(AirportOption("SVO", "Moscow Sheremetyevo"), AirportOption("DME", "Moscow Domodedovo"), AirportOption("VKO", "Moscow Vnukovo")),
    "São Paulo"     to listOf(AirportOption("GRU", "São Paulo Guarulhos"), AirportOption("CGH", "São Paulo Congonhas")),
    "Buenos Aires"  to listOf(AirportOption("EZE", "Buenos Aires Ezeiza"), AirportOption("AEP", "Buenos Aires Aeroparque")),
    "Shanghai"      to listOf(AirportOption("PVG", "Shanghai Pudong"), AirportOption("SHA", "Shanghai Hongqiao")),
    "Beijing"       to listOf(AirportOption("PEK", "Beijing Capital"), AirportOption("PKX", "Beijing Daxing")),
    "Miami"         to listOf(AirportOption("MIA", "Miami International"), AirportOption("FLL", "Fort Lauderdale"))
)

// City → default airport code (for cities clicked on globe without being in results)
private val CITY_DEFAULT_CODE: Map<String, String> = mapOf(
    "Paris" to "CDG", "London" to "LHR", "Rome" to "FCO", "Madrid" to "MAD",
    "Berlin" to "TXL", "Amsterdam" to "AMS", "Athens" to "ATH", "Lisbon" to "LIS",
    "Geneva" to "GVA", "Vienna" to "VIE", "Brussels" to "BRU", "Prague" to "PRG",
    "Warsaw" to "WAW", "Budapest" to "BUD", "Stockholm" to "ARN", "Oslo" to "OSL",
    "Helsinki" to "HEL", "Copenhagen" to "CPH", "Zurich" to "ZRH", "Barcelona" to "BCN",
    "New York" to "JFK", "Los Angeles" to "LAX", "Chicago" to "ORD", "Miami" to "MIA",
    "San Francisco" to "SFO", "Toronto" to "YYZ", "Mexico City" to "MEX",
    "São Paulo" to "GRU", "Buenos Aires" to "EZE", "Bogota" to "BOG", "Lima" to "LIM",
    "Tokyo" to "NRT", "Beijing" to "PEK", "Shanghai" to "PVG", "Seoul" to "ICN",
    "Hong Kong" to "HKG", "Singapore" to "SIN", "Bangkok" to "BKK", "Kuala Lumpur" to "KUL",
    "Mumbai" to "BOM", "Delhi" to "DEL", "Dubai" to "DXB", "Istanbul" to "IST",
    "Doha" to "DOH", "Abu Dhabi" to "AUH", "Cairo" to "CAI", "Johannesburg" to "JNB",
    "Casablanca" to "CMN", "Nairobi" to "NBO", "Sydney" to "SYD", "Melbourne" to "MEL",
    "Auckland" to "AKL", "Bali" to "DPS", "Moscow" to "SVO", "Milan" to "MXP"
)

// ─── InspirationScreen ────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspirationScreen(
    onNavigateToHome: (destCode: String, destName: String) -> Unit,
    onBack: () -> Unit,
    viewModel: InspirationViewModel = hiltViewModel()
) {
    val uiState       by viewModel.uiState.collectAsState()
    val weather       by viewModel.weather.collectAsState()
    val temperature   by viewModel.temperature.collectAsState()
    val humidity      by viewModel.humidity.collectAsState()
    val wind          by viewModel.wind.collectAsState()
    val budget        by viewModel.budget.collectAsState()
    val continent     by viewModel.continent.collectAsState()
    val visitedCodes  by viewModel.visitedCodes.collectAsState()
    val trendingCodes      by viewModel.trendingCodes.collectAsState()
    val favoriteCodes      by viewModel.favoriteCodes.collectAsState()
    val recentCodes        by viewModel.recentCodes.collectAsState()
    val visitedDestinations by viewModel.visitedDestinations.collectAsState()
    val language      by LanguageManager.language.collectAsState()
    val strings       = AppStrings.forLanguage(language)

    var origin        by remember { mutableStateOf("") }
    var originCode    by remember { mutableStateOf("") }
    var departureDate by remember { mutableStateOf("") }
    var originSuggestions by remember { mutableStateOf(listOf<Airport>()) }
    var showOriginDropdown by remember { mutableStateOf(false) }
    var selectedCity  by remember { mutableStateOf<String?>(null) }
    var destQuery     by remember { mutableStateOf("") }
    var destSuggestions by remember { mutableStateOf(listOf<Airport>()) }
    var showDestDropdown by remember { mutableStateOf(false) }

    // Multi-airport dialog state
    var airportPickerCity by remember { mutableStateOf<String?>(null) }

    val globeController = rememberGlobeController()
    var globeReady    by remember { mutableStateOf(false) }
    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                departureDate = "$year-${(month + 1).toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
            },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).also { it.datePicker.minDate = System.currentTimeMillis() }.show()
    }

    // Navigate to Home for a given city + code, showing airport picker if needed
    fun navigateToDestination(city: String, defaultCode: String) {
        val multiAirports = MULTI_AIRPORT_CITIES[city]
        if (!multiAirports.isNullOrEmpty()) {
            airportPickerCity = city
        } else {
            onNavigateToHome(defaultCode, "$city ($defaultCode)")
        }
    }

    val results = (uiState as? InspirationUiState.Success)?.destinations ?: emptyList()

    LaunchedEffect(uiState, globeReady) {
        if (!globeReady) return@LaunchedEffect
        when (uiState) {
            is InspirationUiState.Success -> {
                globeController.highlightDestinations(results)
                results.firstOrNull()?.city?.let { city ->
                    selectedCity = city
                    destQuery = city
                    globeController.zoomToCity(city)
                }
            }
            is InspirationUiState.Idle -> {
                globeController.resetView()
                selectedCity = null
            }
            else -> Unit
        }
    }
    LaunchedEffect(globeReady, favoriteCodes) { if (globeReady) globeController.setFavoriteCodes(favoriteCodes) }
    LaunchedEffect(globeReady, recentCodes)   { if (globeReady) globeController.setRecentCodes(recentCodes) }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        containerColor = Color.Transparent,
        sheetPeekHeight = 172.dp,
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetContainerColor = SheetBg,
        sheetTonalElevation = 0.dp,
        sheetShadowElevation = 24.dp,
        sheetDragHandle = {
            // Glassmorphism top border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Brush.horizontalGradient(listOf(AccentBlue.copy(alpha = 0.5f), AccentPink.copy(alpha = 0.5f))))
            )
            // Custom drag handle with gradient accent
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(listOf(AccentBlue, AccentPink))
                    )
            )
        },
        sheetContent = {
            when (uiState) {
                is InspirationUiState.Success -> ResultsPanel(
                    results = results,
                    selectedCity = selectedCity,
                    trendingCodes = trendingCodes,
                    visitedCodes = visitedCodes,
                    strings = strings,
                    onSelectCity = { city ->
                        selectedCity = city
                        destQuery = city
                        globeController.zoomToCity(city)
                        coroutineScope.launch {
                            scaffoldState.bottomSheetState.partialExpand()
                        }
                    },
                    onBook = { city, code ->
                        navigateToDestination(city, code)
                    },
                    onMarkVisited = { dest -> viewModel.markVisited(dest) },
                    onUnmarkVisited = { code -> viewModel.unmarkVisited(code) },
                    visitedDestinations = visitedDestinations,
                    onShare = { dest ->
                        val price = dest.minPrice?.let { " · ~${it.toInt()}€" } ?: ""
                        val temp  = dest.weather?.temperature?.let { " · ${it.toInt()}°" } ?: ""
                        val text  = "✈️ ${dest.city}, ${dest.country}$price$temp\n🌍 Découvert avec SmartTrip"
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        context.startActivity(Intent.createChooser(intent, "Partager la destination"))
                    },
                    onReset = {
                        viewModel.reset()
                        globeController.resetView()
                        selectedCity = null
                        destQuery = ""
                    }
                )
                else -> FiltersPanel(
                    weather = weather, temperature = temperature,
                    humidity = humidity, wind = wind,
                    budget = budget, continent = continent,
                    origin = origin, originCode = originCode,
                    departureDate = departureDate,
                    originSuggestions = originSuggestions,
                    showOriginDropdown = showOriginDropdown,
                    uiState = uiState,
                    strings = strings,
                    onWeatherChange = { viewModel.setWeather(it) },
                    onTemperatureChange = { viewModel.setTemperature(it) },
                    onHumidityChange = { viewModel.setHumidity(it) },
                    onWindChange = { viewModel.setWind(it) },
                    onBudgetChange = { viewModel.setBudget(it) },
                    onContinentChange = { viewModel.setContinent(it) },
                    onOriginChange = { v ->
                        origin = v; originCode = ""
                        originSuggestions = searchAirports(v)
                        showOriginDropdown = originSuggestions.isNotEmpty()
                    },
                    onAirportSelect = { airport ->
                        origin = "${airport.city} (${airport.code})"
                        originCode = airport.code
                        showOriginDropdown = false
                    },
                    onDatePick = { showDatePicker() },
                    onSearch = { viewModel.getInspiration(originCode, departureDate) }
                )
            }
        }
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(
                0.0f to Color(0xFF1a3a6e),
                0.55f to Color(0xFF0b1a3e),
                1.0f to Color(0xFF000000)
            ))) {

            // ── Globe (full screen) ──────────────────────────────────────
            GlobeWebView(
                modifier = Modifier.fillMaxSize(),
                controller = globeController,
                onGlobeReady = { globeReady = true },
                onMarkerClick = { city ->
                    selectedCity = city
                    destQuery = city
                    globeController.zoomToCity(city)
                    // Find airport code: check results first, then default map
                    val code = results.find { it.city.equals(city, ignoreCase = true) }?.code
                        ?: CITY_DEFAULT_CODE.entries.find { it.key.equals(city, ignoreCase = true) }?.value
                        ?: city
                    navigateToDestination(city, code)
                }
            )

            // ── Airport picker dialog ──────────────────────────────────────
            airportPickerCity?.let { city ->
                val airports = MULTI_AIRPORT_CITIES[city] ?: emptyList()
                AirportPickerDialog(
                    city = city,
                    airports = airports,
                    strings = strings,
                    onSelect = { option ->
                        onNavigateToHome(option.code, "$city (${option.code})")
                        airportPickerCity = null
                    },
                    onDismiss = { airportPickerCity = null }
                )
            }

            // ── Top gradient + AppBar ────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent)
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour",
                                tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(Modifier.width(2.dp))
                    Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFFBFC5F7), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        strings.inspirationScreenTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.3.sp
                    )
                    Spacer(Modifier.weight(1f))
                    // Boutons Surprise : Tendances + Aléatoire
                    Row(
                        modifier = Modifier.padding(end = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tendances
                        Box(
                            modifier = Modifier
                                .height(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFD97706).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .background(Color(0xFFD97706).copy(alpha = 0.15f))
                                .clickable {
                                    globeController.startSurpriseSpin()
                                    viewModel.surpriseTrending()
                                }
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(13.dp))
                                Text("Tendances", color = Color(0xFFF59E0B), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        // Aléatoire
                        Box(
                            modifier = Modifier
                                .height(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, AccentBlue.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .background(AccentBlue.copy(alpha = 0.15f))
                                .clickable {
                                    globeController.startSurpriseSpin()
                                    viewModel.surpriseRandom(originCode, departureDate)
                                }
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Shuffle, null, tint = AccentBlue, modifier = Modifier.size(13.dp))
                                Text(strings.surpriseBtn, color = AccentBlue, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // ── Loading overlay ──────────────────────────────────────────
            AnimatedVisibility(
                visible = uiState is InspirationUiState.Loading,
                enter = fadeIn(), exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1A1A2E).copy(alpha = 0.95f), Color(0xFF0D0D20).copy(alpha = 0.95f))
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                        .padding(28.dp)
                ) {
                    CircularProgressIndicator(
                        color = AccentPink,
                        modifier = Modifier.size(44.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        strings.loadingAnalysis,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        strings.loadingQuerying,
                        color = Color.White.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// ─── Filters Panel ────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltersPanel(
    weather: String, temperature: String, humidity: String, wind: String,
    budget: String, continent: String,
    origin: String, originCode: String, departureDate: String,
    originSuggestions: List<Airport>, showOriginDropdown: Boolean,
    uiState: InspirationUiState,
    strings: AppStrings,
    onWeatherChange: (String) -> Unit, onTemperatureChange: (String) -> Unit,
    onHumidityChange: (String) -> Unit, onWindChange: (String) -> Unit,
    onBudgetChange: (String) -> Unit, onContinentChange: (String) -> Unit,
    onOriginChange: (String) -> Unit, onAirportSelect: (Airport) -> Unit,
    onDatePick: () -> Unit, onSearch: () -> Unit
) {
    val weatherOptions = listOf(
        Option("", strings.optionAny), Option("sunny", strings.optionSunny),
        Option("cloudy", strings.optionCloudy), Option("rainy", strings.optionRainy),
        Option("snowy", strings.optionSnowy), Option("stormy", strings.optionStormy)
    )
    val tempOptions = listOf(
        Option("", strings.optionAny), Option("tropical", strings.optionTropical),
        Option("hot", strings.optionHot), Option("mild", strings.optionMild),
        Option("cool", strings.optionCool), Option("cold", strings.optionCold)
    )
    val humidityOptions = listOf(
        Option("", strings.optionAny), Option("dry", strings.optionDry),
        Option("normal", strings.optionNormalHumidity), Option("humid", strings.optionHumid)
    )
    val windOptions = listOf(
        Option("", strings.optionAny), Option("calm", strings.optionCalm),
        Option("moderate", strings.optionModerate), Option("windy", strings.optionWindy)
    )
    val budgetOptions = listOf(
        Option("", strings.optionAny), Option("200", "≤ 200€"),
        Option("500", "≤ 500€"), Option("1000", "≤ 1 000€"),
        Option("2000", "≤ 2 000€")
    )
    // Compact peek header — always visible (first ~172dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        // Header row: title + date compact button
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Brush.linearGradient(listOf(AccentBlue, AccentPink)))
            )
            Spacer(Modifier.width(10.dp))
            Text(
                strings.customizeTrip,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.2.sp
            )
            Spacer(Modifier.weight(1f))
            // Date compact button
            Box(
                modifier = Modifier
                    .height(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .clickable { onDatePick() }
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(Icons.Default.CalendarToday, null, tint = AccentBlue, modifier = Modifier.size(12.dp))
                    Text(
                        if (departureDate.isEmpty()) "Date" else departureDate,
                        color = if (departureDate.isEmpty()) Color.White.copy(alpha = 0.4f) else Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        // Continent filter — always visible in peek
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(end = 4.dp)
        ) {
            items(CONTINENT_OPTIONS) { opt ->
                val isSel = continent == opt.key
                Box(
                    modifier = Modifier
                        .height(26.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .border(
                            1.dp,
                            if (isSel) AccentBlue.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.12f),
                            RoundedCornerShape(7.dp)
                        )
                        .background(if (isSel) AccentBlue.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.04f))
                        .clickable { onContinentChange(opt.key) }
                        .padding(horizontal = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(opt.icon, null, tint = if (isSel) AccentBlue else Color.White.copy(alpha = 0.4f), modifier = Modifier.size(11.dp))
                        Text(opt.label, color = if (isSel) Color.White else Color.White.copy(alpha = 0.55f), style = MaterialTheme.typography.labelSmall, fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal)
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        // First chip row visible in peek
        FilterSection(strings.filterWeatherTitle, Icons.Default.WbSunny, weatherOptions, weather, onWeatherChange)
        Spacer(Modifier.height(10.dp))
        // Hint to drag up
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.KeyboardArrowUp, null,
                tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
            Text(
                strings.swipeHint,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.3f)
            )
        }
    }

    // Rest of settings — revealed on drag
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                OutlinedTextField(
                    value = origin,
                    onValueChange = onOriginChange,
                    label = { Text(strings.departureAirportLabel,
                        color = Color.White.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodySmall) },
                    leadingIcon = { Icon(Icons.Default.FlightTakeoff, null,
                        tint = AccentBlue.copy(alpha = 0.8f), modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentBlue, unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        cursorColor = AccentBlue,
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
                    )
                )
                if (showOriginDropdown) {
                    AirportDropdown(suggestions = originSuggestions, onSelect = onAirportSelect)
                }
            }
        }
        item { FilterSection(strings.filterTempTitle, Icons.Default.Thermostat, tempOptions, temperature, onTemperatureChange) }
        item { FilterSection(strings.filterHumidityTitle, Icons.Default.WaterDrop, humidityOptions, humidity, onHumidityChange) }
        item { FilterSection(strings.filterWindTitle, Icons.Default.Air, windOptions, wind, onWindChange) }
        item { FilterSection(strings.filterBudgetTitle, Icons.Default.Euro, budgetOptions, budget, onBudgetChange) }
        if (uiState is InspirationUiState.Error) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF7F1D1D).copy(alpha = 0.5f))
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, null, tint = Color(0xFFFCA5A5), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        uiState.message,
                        color = Color(0xFFFCA5A5),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        item {
            // Gradient search button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (uiState !is InspirationUiState.Loading)
                            Brush.horizontalGradient(listOf(AccentBlue, Color(0xFF8B5CF6), AccentPink))
                        else
                            Brush.horizontalGradient(listOf(AccentBlue.copy(alpha = 0.35f), AccentPink.copy(alpha = 0.35f)))
                    )
                    .clickable(enabled = uiState !is InspirationUiState.Loading) { onSearch() },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        strings.findDestBtn,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}

// ─── Filter section with horizontal chip row ──────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSection(
    title: String,
    icon: ImageVector,
    options: List<Option>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Brush.verticalGradient(listOf(AccentBlue, AccentPink)))
            )
            Spacer(Modifier.width(8.dp))
            Icon(icon, null, tint = AccentBlue.copy(alpha = 0.85f), modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.9f),
                letterSpacing = 0.3.sp
            )
        }
        Spacer(Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            contentPadding = PaddingValues(end = 8.dp)
        ) {
            items(options) { opt ->
                val isSelected = selected == opt.key
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelect(opt.key) },
                    label = {
                        Text(
                            opt.label,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.White.copy(alpha = 0.06f),
                        selectedContainerColor = AccentBlue,
                        labelColor = Color.White.copy(alpha = 0.6f),
                        selectedLabelColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true, selected = isSelected,
                        borderColor = Color.White.copy(alpha = 0.12f),
                        selectedBorderColor = AccentBlue.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
    }
}

// ─── Results Panel ────────────────────────────────────────────────────────────
@Composable
private fun ResultsPanel(
    results: List<InspirationDestinationDto>,
    selectedCity: String?,
    trendingCodes: Set<String>,
    visitedCodes: Set<String>,
    visitedDestinations: List<InspirationDestinationDto>,
    strings: AppStrings,
    onSelectCity: (String) -> Unit,
    onBook: (city: String, code: String) -> Unit,
    onMarkVisited: (InspirationDestinationDto) -> Unit,
    onUnmarkVisited: (String) -> Unit,
    onShare: (dest: InspirationDestinationDto) -> Unit,
    onReset: () -> Unit
) {
    var showVisitedDialog by remember { mutableStateOf(false) }
    Column {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "${results.size} ${if (results.size > 1) strings.destWordPlural else strings.destWord} ${if (results.size > 1) strings.foundDestWordPlural else strings.foundDestWord}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    strings.tapToZoom,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.35f)
                )
            }
            // Bouton "Visités (n)"
            if (visitedDestinations.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF16A34A).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFF4ADE80).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .clickable { showVisitedDialog = true }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4ADE80), modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Visités (${visitedDestinations.size})", color = Color(0xFF4ADE80), style = MaterialTheme.typography.labelMedium)
                    }
                }
                Spacer(Modifier.width(6.dp))
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .clickable { onReset() }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, null, tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(strings.btnRedo, color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // Dialog liste des destinations visitées
        if (showVisitedDialog) {
            AlertDialog(
                onDismissRequest = { showVisitedDialog = false },
                containerColor = Color(0xFF1A1A2E),
                tonalElevation = 0.dp,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4ADE80), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Destinations visitées", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Appuyez sur × pour retirer une destination de la liste.", color = Color.White.copy(alpha = 0.45f), style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(4.dp))
                        visitedDestinations.forEach { dest ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.06f))
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E3A5F)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(dest.code ?: "", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 9.sp)
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(dest.city ?: "", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
                                    Text(dest.country ?: "", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.45f))
                                }
                                IconButton(
                                    onClick = { onUnmarkVisited(dest.code ?: "") },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showVisitedDialog = false }) {
                        Text("Fermer", color = AccentBlue, fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(results.filter { !it.code.isNullOrBlank() }) { index, dest ->
                InspirationDestCard(
                    dest = dest,
                    isSelected = dest.city == selectedCity,
                    isTop = index == 0,
                    isTrending = dest.code in trendingCodes,
                    isVisited = dest.code in visitedCodes,
                    strings = strings,
                    onSelect = { onSelectCity(dest.city ?: "") },
                    onBook = { onBook(dest.city ?: "", dest.code ?: "") },
                    onMarkVisited = { onMarkVisited(dest) },
                    onShare = { onShare(dest) }
                )
            }
        }
    }
}

// ─── Destination card ─────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InspirationDestCard(
    dest: InspirationDestinationDto,
    isSelected: Boolean,
    isTop: Boolean,
    isTrending: Boolean,
    isVisited: Boolean,
    strings: AppStrings,
    onSelect: () -> Unit,
    onBook: () -> Unit,
    onMarkVisited: () -> Unit,
    onShare: () -> Unit
) {
    val borderColor = when {
        isSelected -> AccentPink
        isTop      -> AccentAmber
        else       -> Color.White.copy(alpha = 0.1f)
    }

    val photoQuery = dest.city?.lowercase()?.replace(" ", "+") ?: "travel"
val photoUrl = "https://loremflickr.com/600/220/$photoQuery,landmark,travel"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onSelect() }
    ) {
        Column {
            // ── Photo banner ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                // Background: dark fallback while loading
                Box(Modifier.matchParentSize().background(Color(0xFF1A1A30)))

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = dest.city,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )

                // Bottom gradient overlay so text is readable
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                0.45f to Color.Transparent,
                                1f to Color(0xFF0D0D1A)
                            )
                        )
                )

                // Top-left: IATA badge
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.verticalGradient(
                                if (isSelected) listOf(AccentPink, Color(0xFF9333EA))
                                else if (isTop) listOf(AccentAmber, Color(0xFFEA580C))
                                else listOf(Color(0xFF3730A3), Color(0xFF1E1B4B))
                            )
                        )
                        .align(Alignment.TopStart),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        dest.code ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }

                // Top-right actions: share + temperature
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Share button
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF000000).copy(alpha = 0.45f))
                            .clickable { onShare() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }

                    // Temperature
                    dest.weather?.temperature?.let { temp ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF000000).copy(alpha = 0.45f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "${temp.toInt()}°",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    temp >= 28 -> Color(0xFFEF4444)
                                    temp >= 20 -> Color(0xFFF97316)
                                    temp >= 12 -> Color(0xFF22C55E)
                                    temp >= 5  -> Color(0xFF60A5FA)
                                    else       -> Color(0xFF22D3EE)
                                }
                            )
                        }
                    }
                }

                // Bottom-left: city name + badges
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 10.dp, bottom = 8.dp, end = 60.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isTop) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AccentAmber)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("★ Top", style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 8.sp)
                            }
                        }
                        if (isTrending) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFD97706).copy(alpha = 0.85f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.TrendingUp, null,
                                    tint = Color.White, modifier = Modifier.size(8.dp))
                                Text("Tendance", style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 8.sp)
                            }
                        }
                    }
                    Text(
                        dest.city ?: "Destination",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        dest.country ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }

                // Bottom-right: price
                dest.minPrice?.let { price ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 10.dp, bottom = 10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF16A34A).copy(alpha = 0.85f))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text("~${price.toInt()}€", style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // ── Weather + action buttons ─────────────────────────────────
            val cardBg = when {
                isSelected -> Brush.linearGradient(listOf(Color(0xFF1E1B4B), Color(0xFF2D1B69)))
                isTop      -> Brush.linearGradient(listOf(Color(0xFF1C1A0F), Color(0xFF2D2410)))
                else       -> Brush.linearGradient(listOf(Color(0xFF111128), Color(0xFF0D0D1A)))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBg)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                dest.weather?.let { w ->
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        w.description?.let {
                            Text(it.replaceFirstChar(Char::uppercase),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.65f),
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false))
                        }
                        w.humidity?.let {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Icon(Icons.Default.WaterDrop, null, tint = Color(0xFF60A5FA), modifier = Modifier.size(10.dp))
                                Text("$it%", style = MaterialTheme.typography.labelSmall, color = Color(0xFF60A5FA), maxLines = 1, softWrap = false)
                            }
                        }
                        w.windSpeed?.let {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Icon(Icons.Default.Air, null, tint = Color(0xFF34D399), modifier = Modifier.size(10.dp))
                                Text("${(it * 3.6).toInt()}km/h", style = MaterialTheme.typography.labelSmall, color = Color(0xFF34D399), maxLines = 1, softWrap = false)
                            }
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                }
                // Visité
                if (!isVisited) {
                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .clickable { onMarkVisited() }
                            .padding(horizontal = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.CheckCircleOutline, null, tint = Color.White.copy(alpha = 0.45f), modifier = Modifier.size(12.dp))
                            Text("Visité", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.45f))
                        }
                    }
                    Spacer(Modifier.width(6.dp))
                }
                // Réserver
                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Brush.horizontalGradient(listOf(AccentBlue, Color(0xFF8B5CF6))))
                        .clickable { onBook() }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FlightTakeoff, null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(strings.btnBook, style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun AirportPickerDialog(
    city: String,
    airports: List<AirportOption>,
    strings: AppStrings,
    onSelect: (AirportOption) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A2E),
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.FlightTakeoff,
                    contentDescription = null,
                    tint = Color(0xFF818CF8),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "${strings.airportsOf} $city",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    strings.selectAirportHint,
                    color = Color.White.copy(alpha = 0.55f),
                    style = MaterialTheme.typography.bodySmall
                )
                airports.forEach { option ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.White.copy(alpha = 0.07f), Color.White.copy(alpha = 0.04f))
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .clickable { onSelect(option) }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // IATA code badge
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF6366F1), Color(0xFF4F46E5))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    option.code,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    option.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    option.code,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF818CF8)
                                )
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel, color = Color(0xFF818CF8))
            }
        }
    )
}
