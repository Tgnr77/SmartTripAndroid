package com.smarttrip.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val flightRepository: FlightRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<InspirationUiState>(InspirationUiState.Idle)
    val uiState: StateFlow<InspirationUiState> = _uiState

    val budget      = MutableStateFlow("")
    val weather     = MutableStateFlow("")
    val temperature = MutableStateFlow("")
    val humidity    = MutableStateFlow("")
    val wind        = MutableStateFlow("")
    val activities  = MutableStateFlow(setOf<String>())

    private val _favoriteCodes = MutableStateFlow<Set<String>>(emptySet())
    val favoriteCodes: StateFlow<Set<String>> = _favoriteCodes

    private val _recentCodes = MutableStateFlow<Set<String>>(emptySet())
    val recentCodes: StateFlow<Set<String>> = _recentCodes

    init { loadFavoritesAndHistory() }

    private fun loadFavoritesAndHistory() {
        viewModelScope.launch {
            when (val r = flightRepository.getFavorites()) {
                is ApiResult.Success -> _favoriteCodes.value = r.data.map { it.destinationCode }.toSet()
                else -> Unit
            }
        }
        viewModelScope.launch {
            when (val r = flightRepository.getSearchHistory()) {
                is ApiResult.Success -> _recentCodes.value = r.data.map { it.destinationCode }.toSet()
                else -> Unit
            }
        }
    }

    fun toggleActivity(activity: String) {
        val current = activities.value.toMutableSet()
        if (current.contains(activity)) current.remove(activity) else current.add(activity)
        activities.value = current
    }

    fun getInspiration(origin: String, departureDate: String) {
        viewModelScope.launch {
            _uiState.value = InspirationUiState.Loading
            val request = InspirationRequest(
                origin = origin,
                departureDate = departureDate,
                weather = weather.value,
                temperature = temperature.value,
                humidity = humidity.value,
                wind = wind.value,
                budget = budget.value,
                activities = activities.value.toList()
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

    /** Mode Surprise : réinitialise tous les filtres et lance une recherche aléatoire */
    fun surprise(origin: String = "", departureDate: String = "") {
        viewModelScope.launch {
            weather.value = ""; temperature.value = ""; humidity.value = ""
            wind.value = ""; budget.value = ""
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
