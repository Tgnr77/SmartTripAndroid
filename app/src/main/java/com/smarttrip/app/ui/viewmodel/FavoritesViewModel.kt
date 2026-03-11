package com.smarttrip.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrip.app.data.remote.models.FavoriteDto
import com.smarttrip.app.data.repository.ApiResult
import com.smarttrip.app.data.repository.FlightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val flightRepository: FlightRepository
) : ViewModel() {

    private val _favorites = MutableStateFlow<List<FavoriteDto>>(emptyList())
    val favorites: StateFlow<List<FavoriteDto>> = _favorites.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadFavorites() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (val result = flightRepository.getFavorites()) {
                is ApiResult.Success -> _favorites.value = result.data
                is ApiResult.Error   -> _error.value = result.message
            }
            _loading.value = false
        }
    }

    fun deleteFavorite(id: String) {
        viewModelScope.launch {
            when (val result = flightRepository.deleteFavorite(id)) {
                is ApiResult.Success -> _favorites.value = _favorites.value.filter { it.id?.toString() != id }
                is ApiResult.Error   -> _error.value = result.message
            }
        }
    }
}


@HiltViewModel
class SearchHistoryViewModel @Inject constructor(
    private val flightRepository: FlightRepository
) : ViewModel() {

    private val _history = MutableStateFlow<List<com.smarttrip.app.data.remote.models.SearchHistoryDto>>(emptyList())
    val history: StateFlow<List<com.smarttrip.app.data.remote.models.SearchHistoryDto>> = _history.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadHistory() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (val result = flightRepository.getSearchHistory()) {
                is ApiResult.Success -> _history.value = result.data
                is ApiResult.Error   -> _error.value = result.message
            }
            _loading.value = false
        }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            when (flightRepository.deleteHistoryEntry(id)) {
                is ApiResult.Success -> _history.value = _history.value.filter { it.id != id }
                is ApiResult.Error   -> _error.value = "Erreur"
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            when (flightRepository.clearSearchHistory()) {
                is ApiResult.Success -> _history.value = emptyList()
                is ApiResult.Error   -> _error.value = "Erreur"
            }
        }
    }
}
