package com.smarttrip.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrip.app.data.remote.models.*
import com.smarttrip.app.data.repository.ApiResult
import com.smarttrip.app.data.repository.FlightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val flightRepository: FlightRepository
) : ViewModel() {

    private val _allFlights = MutableStateFlow<List<FlightDto>>(emptyList())

    private val _sortBy = MutableStateFlow("best")
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    val flights: StateFlow<List<FlightDto>> = combine(_allFlights, _sortBy) { list, sort ->
        when (sort) {
            "price"    -> list.sortedBy { it.price }
            "duration" -> list.sortedBy { it.outbound.duration }
            else       -> list.sortedByDescending { it.aiScore ?: 0 }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun searchFlights(
        origin: String,
        destination: String,
        departureDate: String,
        returnDate: String? = null,
        adults: Int = 1,
        cabinClass: String = "economy",
        nonStop: Boolean = false
    ) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (val result = flightRepository.searchFlights(
                origin, destination, departureDate, returnDate, adults, cabinClass, nonStop
            )) {
                is ApiResult.Success -> _allFlights.value = result.data
                is ApiResult.Error   -> _error.value = result.message
            }
            _loading.value = false
        }
    }

    fun setSortBy(sort: String) {
        _sortBy.value = sort
    }

    fun toggleFavorite(flight: FlightDto) {
        viewModelScope.launch {
            if (flight.isFavorite && flight.favoriteId != null) {
                flightRepository.deleteFavorite(flight.favoriteId)
                _allFlights.value = _allFlights.value.map {
                    if (it.outbound.departureAirport == flight.outbound.departureAirport &&
                        it.price == flight.price
                    ) it.copy(isFavorite = false, favoriteId = null) else it
                }
            } else {
                val request = AddFavoriteRequest(
                    priceAmount = flight.price,
                    priceCurrency = flight.currency,
                    originCode = flight.outbound.departureAirport,
                    destinationCode = flight.outbound.arrivalAirport,
                    departureDatetime = flight.outbound.departureTime,
                    arrivalDatetime = flight.outbound.arrivalTime,
                    durationMinutes = flight.outbound.duration,
                    stops = flight.outbound.stops,
                    airlineName = flight.outbound.airline,
                    flightNumber = flight.outbound.flightNumber
                )
                when (val r = flightRepository.addFavorite(request)) {
                    is ApiResult.Success -> {
                        _allFlights.value = _allFlights.value.map {
                            if (it.outbound.departureAirport == flight.outbound.departureAirport &&
                                it.price == flight.price
                            ) it.copy(isFavorite = true, favoriteId = r.data) else it
                        }
                    }
                    is ApiResult.Error -> _error.value = r.message
                }
            }
        }
    }
}
