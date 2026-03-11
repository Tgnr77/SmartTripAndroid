package com.smarttrip.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrip.app.data.remote.models.TrendingDestinationDto
import com.smarttrip.app.data.repository.ApiResult
import com.smarttrip.app.data.repository.FlightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val flightRepository: FlightRepository
) : ViewModel() {

    private val _trendingDestinations = MutableStateFlow<List<TrendingDestinationDto>>(emptyList())
    val trendingDestinations: StateFlow<List<TrendingDestinationDto>> = _trendingDestinations.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        loadTrendingDestinations()
    }

    fun loadTrendingDestinations() {
        viewModelScope.launch {
            _loading.value = true
            when (val result = flightRepository.getPopularDestinations()) {
                is ApiResult.Success -> _trendingDestinations.value = result.data
                is ApiResult.Error -> _trendingDestinations.value = emptyList()
            }
            _loading.value = false
        }
    }
}
