package com.smarttrip.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    // ─── Filtre states (encapsulés — setters exposés pour la UI) ─────────
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

    private val _activities  = MutableStateFlow(setOf<String>())
    val activities: StateFlow<Set<String>> = _activities

    private val _favoriteCodes = MutableStateFlow<Set<String>>(emptySet())
    val favoriteCodes: StateFlow<Set<String>> = _favoriteCodes

    private val _recentCodes = MutableStateFlow<Set<String>>(emptySet())
    val recentCodes: StateFlow<Set<String>> = _recentCodes

    init { loadFavoritesAndHistory() }

    /** Charge les favoris et l'historique UNIQUEMENT si un token est disponible (utilisateur connecté). */
    private fun loadFavoritesAndHistory() {
        viewModelScope.launch {
            tokenDataStore.awaitReady()
            if (tokenDataStore.cachedToken == null) return@launch // invité, pas d'appel API
            when (val r = flightRepository.getFavorites()) {
                is ApiResult.Success -> _favoriteCodes.value = r.data.map { it.destinationCode }.toSet()
                else -> Unit
            }
        }
        viewModelScope.launch {
            tokenDataStore.awaitReady()
            if (tokenDataStore.cachedToken == null) return@launch // invité, pas d'appel API
            when (val r = flightRepository.getSearchHistory()) {
                is ApiResult.Success -> _recentCodes.value = r.data.map { it.destinationCode }.toSet()
                else -> Unit
            }
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
                    // Le backend gère maintenant tous les filtres (météo, température, humidité, vent, budget)
                    // On conserve uniquement un filtrage de sécurité côté client sur les destinations sans code IATA
                    val filtered = result.data.filter { !it.code.isNullOrBlank() }
                    if (filtered.isEmpty()) {
                        _uiState.value = InspirationUiState.Error("Aucune destination trouvée pour ces critères")
                    } else {
                        _uiState.value = InspirationUiState.Success(filtered)
                    }
                }
                is ApiResult.Error -> _uiState.value = InspirationUiState.Error(result.message)
            }
        }
    }

    /** Mode Surprise Tendances : destinations basées sur les recherches les plus fréquentes */
    fun surpriseTrending() {
        viewModelScope.launch {
            _uiState.value = InspirationUiState.Loading
            when (val result = flightRepository.getSurpriseTrending()) {
                is ApiResult.Success -> {
                    _uiState.value = if (result.data.isEmpty())
                        InspirationUiState.Error("Aucune destination tendance trouvée")
                    else InspirationUiState.Success(result.data)
                }
                is ApiResult.Error -> _uiState.value = InspirationUiState.Error(result.message)
            }
        }
    }

    /** Mode Surprise Aléatoire : destination complètement aléatoire */
    fun surpriseRandom(origin: String = "", departureDate: String = "") {
        viewModelScope.launch {
            _weather.value = ""; _temperature.value = ""; _humidity.value = ""
            _wind.value = ""; _budget.value = ""
            _uiState.value = InspirationUiState.Loading
            val request = InspirationRequest(origin = origin, departureDate = departureDate)
            when (val result = flightRepository.getInspiration(request)) {
                is ApiResult.Success -> {
                    _uiState.value = if (result.data.isEmpty())
                        InspirationUiState.Error("Aucune destination trouvée")
                    else InspirationUiState.Success(result.data)
                }
                is ApiResult.Error -> _uiState.value = InspirationUiState.Error(result.message)
            }
        }
    }

    fun reset() {
        _uiState.value = InspirationUiState.Idle
    }
}
