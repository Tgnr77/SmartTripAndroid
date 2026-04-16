package com.smarttrip.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.smarttrip.app.data.local.TokenDataStore
import com.smarttrip.app.data.remote.models.InspirationDestinationDto
import com.smarttrip.app.data.remote.models.InspirationRequest
import com.smarttrip.app.data.repository.ApiResult
import com.smarttrip.app.data.repository.FlightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class InspirationUiState {
    object Idle : InspirationUiState()
    object Loading : InspirationUiState()
    data class Success(val destinations: List<InspirationDestinationDto>) : InspirationUiState()
    data class Error(val message: String) : InspirationUiState()
}

@HiltViewModel
class InspirationViewModel @Inject constructor(
    private val flightRepository: FlightRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<InspirationUiState>(InspirationUiState.Idle)
    val uiState: StateFlow<InspirationUiState> = _uiState

    // ─── Filtres météo ────────────────────────────────────────────────────
    private val _budget      = MutableStateFlow("")
    val budget: StateFlow<String> = _budget
    fun setBudget(v: String) { _budget.value = v }

    private val _weather     = MutableStateFlow("")
    val weather: StateFlow<String> = _weather
    fun setWeather(v: String) { _weather.value = v }

    private val _temperature = MutableStateFlow("")
    val temperature: StateFlow<String> = _temperature
    fun setTemperature(v: String) { _temperature.value = v }

    private val _humidity    = MutableStateFlow("")
    val humidity: StateFlow<String> = _humidity
    fun setHumidity(v: String) { _humidity.value = v }

    private val _wind        = MutableStateFlow("")
    val wind: StateFlow<String> = _wind
    fun setWind(v: String) { _wind.value = v }

    // ─── Filtre continent (client-side) ───────────────────────────────────
    private val _continent = MutableStateFlow("")
    val continent: StateFlow<String> = _continent
    fun setContinent(v: String) { _continent.value = v; applyLocalFilters() }

    // ─── Destinations déjà visitées ─────────────────────────────────────
    private val _visitedCodes = MutableStateFlow<Set<String>>(emptySet())
    val visitedCodes: StateFlow<Set<String>> = _visitedCodes

    private val _visitedDestinations = MutableStateFlow<List<InspirationDestinationDto>>(emptyList())
    val visitedDestinations: StateFlow<List<InspirationDestinationDto>> = _visitedDestinations

    fun markVisited(dest: InspirationDestinationDto) {
        val code = dest.code ?: return
        if (code !in _visitedCodes.value) {
            _visitedCodes.value = _visitedCodes.value + code
            _visitedDestinations.value = _visitedDestinations.value + dest
            applyLocalFilters()
            saveVisitedToDataStore()
        }
    }

    fun unmarkVisited(code: String) {
        _visitedCodes.value = _visitedCodes.value - code
        _visitedDestinations.value = _visitedDestinations.value.filter { it.code != code }
        applyLocalFilters()
        saveVisitedToDataStore()
    }

    private fun loadVisitedFromDataStore() {
        viewModelScope.launch {
            val json = tokenDataStore.loadVisitedJson() ?: return@launch
            val type = object : TypeToken<List<InspirationDestinationDto>>() {}.type
            val list: List<InspirationDestinationDto> = gson.fromJson(json, type) ?: emptyList()
            _visitedDestinations.value = list
            _visitedCodes.value = list.mapNotNull { it.code }.toSet()
            applyLocalFilters()
        }
    }

    private fun saveVisitedToDataStore() {
        viewModelScope.launch {
            tokenDataStore.saveVisitedJson(gson.toJson(_visitedDestinations.value))
        }
    }

    // ─── Codes trending (pour badge sur les cartes) ───────────────────────
    private val _trendingCodes = MutableStateFlow<Set<String>>(emptySet())
    val trendingCodes: StateFlow<Set<String>> = _trendingCodes

    // ─── Résultats bruts avant filtres locaux ────────────────────────────
    private var rawResults: List<InspirationDestinationDto> = emptyList()

    private val _activities  = MutableStateFlow(setOf<String>())
    val activities: StateFlow<Set<String>> = _activities

    private val _favoriteCodes = MutableStateFlow<Set<String>>(emptySet())
    val favoriteCodes: StateFlow<Set<String>> = _favoriteCodes

    private val _recentCodes = MutableStateFlow<Set<String>>(emptySet())
    val recentCodes: StateFlow<Set<String>> = _recentCodes

    private val gson = Gson()

    init {
        loadFavoritesAndHistory()
        loadTrendingCodes()
        loadVisitedFromDataStore()
    }

    private fun loadFavoritesAndHistory() {
        viewModelScope.launch {
            tokenDataStore.awaitReady()
            if (tokenDataStore.cachedToken == null) return@launch
            when (val r = flightRepository.getFavorites()) {
                is ApiResult.Success -> _favoriteCodes.value = r.data.map { it.destinationCode }.toSet()
                else -> Unit
            }
        }
        viewModelScope.launch {
            tokenDataStore.awaitReady()
            if (tokenDataStore.cachedToken == null) return@launch
            when (val r = flightRepository.getSearchHistory()) {
                is ApiResult.Success -> _recentCodes.value = r.data.map { it.destinationCode }.toSet()
                else -> Unit
            }
        }
    }

    private fun loadTrendingCodes() {
        viewModelScope.launch {
            when (val r = flightRepository.getPopularDestinations()) {
                is ApiResult.Success -> _trendingCodes.value = r.data.mapNotNull { it.code }.toSet()
                else -> Unit
            }
        }
    }

    /** Applique les filtres continent + visité sur rawResults */
    private fun applyLocalFilters() {
        val visited = _visitedCodes.value
        val continentFilter = _continent.value
        var filtered = rawResults.filter { !it.code.isNullOrBlank() && it.code !in visited }
        if (continentFilter.isNotEmpty()) {
            val codes = CONTINENT_CODES[continentFilter] ?: emptySet()
            filtered = filtered.filter { it.code in codes }
        }
        _uiState.value = when {
            rawResults.isEmpty()  -> InspirationUiState.Idle
            filtered.isEmpty()    -> InspirationUiState.Error("Aucune destination pour ces critères")
            else                  -> InspirationUiState.Success(filtered)
        }
    }

    fun toggleActivity(activity: String) {
        val current = _activities.value.toMutableSet()
        if (current.contains(activity)) current.remove(activity) else current.add(activity)
        _activities.value = current
    }

    fun getInspiration(origin: String, departureDate: String) {
        viewModelScope.launch {
            _uiState.value = InspirationUiState.Loading
            val request = InspirationRequest(
                origin = origin,
                departureDate = departureDate,
                weather = _weather.value,
                temperature = _temperature.value,
                humidity = _humidity.value,
                wind = _wind.value,
                budget = _budget.value,
                activities = _activities.value.toList()
            )
            when (val result = flightRepository.getInspiration(request)) {
                is ApiResult.Success -> {
                    rawResults = result.data.filter { !it.code.isNullOrBlank() }
                    if (rawResults.isEmpty()) {
                        _uiState.value = InspirationUiState.Error("Aucune destination trouvée pour ces critères")
                    } else {
                        applyLocalFilters()
                    }
                }
                is ApiResult.Error -> _uiState.value = InspirationUiState.Error(result.message)
            }
        }
    }

    /** Mode Surprise Tendances */
    fun surpriseTrending() {
        viewModelScope.launch {
            _uiState.value = InspirationUiState.Loading
            when (val result = flightRepository.getSurpriseTrending()) {
                is ApiResult.Success -> {
                    rawResults = result.data.filter { !it.code.isNullOrBlank() }
                    if (rawResults.isEmpty()) {
                        _uiState.value = InspirationUiState.Error("Aucune destination tendance trouvée")
                    } else {
                        applyLocalFilters()
                    }
                }
                is ApiResult.Error -> _uiState.value = InspirationUiState.Error(result.message)
            }
        }
    }

    /** Mode Surprise Aléatoire */
    fun surpriseRandom(origin: String = "", departureDate: String = "") {
        viewModelScope.launch {
            _weather.value = ""; _temperature.value = ""; _humidity.value = ""
            _wind.value = ""; _budget.value = ""
            _uiState.value = InspirationUiState.Loading
            val request = InspirationRequest(origin = origin, departureDate = departureDate)
            when (val result = flightRepository.getInspiration(request)) {
                is ApiResult.Success -> {
                    rawResults = result.data.filter { !it.code.isNullOrBlank() }
                    if (rawResults.isEmpty()) {
                        _uiState.value = InspirationUiState.Error("Aucune destination trouvée")
                    } else {
                        applyLocalFilters()
                    }
                }
                is ApiResult.Error -> _uiState.value = InspirationUiState.Error(result.message)
            }
        }
    }

    fun reset() {
        rawResults = emptyList()
        _uiState.value = InspirationUiState.Idle
    }

    companion object {
        val CONTINENT_CODES = mapOf(
            "europe"   to setOf("CDG","ORY","LHR","LGW","STN","MAD","BCN","FCO","CIA","MXP","LIN","TXL","MUC","FRA","AMS","ATH","LIS","GVA","VIE","BRU","PRG","WAW","BUD","ARN","OSL","CPH","DUB","ZAG","IST","SAW","NCE","LYS","MRS","EDI","MAN","HEL","OPO","ZRH","VCE","FLR","NAP","BOD","TLS","TUN","CMN","RAK"),
            "asia"     to setOf("HND","NRT","KIX","PEK","PKX","PVG","SHA","HKG","ICN","BKK","HKT","SIN","KUL","CGK","DPS","HAN","DEL","BOM","DXB","AUH","DOH","TLV"),
            "americas" to setOf("JFK","EWR","LGA","LAX","BUR","MIA","FLL","SFO","ORD","MDW","LAS","YYZ","YUL","YVR","MEX","CUN","GIG","GRU","CGH","EZE","AEP","LIM","BOG","SCL"),
            "africa"   to setOf("CAI","CPT","NBO","JNB","LOS","ADD","CMN","RAK","TUN"),
            "oceanie"  to setOf("SYD","MEL","BNE","AKL","WLG","DPS")
        )
    }
}
