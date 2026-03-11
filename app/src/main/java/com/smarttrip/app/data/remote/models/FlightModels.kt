package com.smarttrip.app.data.remote.models

import com.google.gson.annotations.SerializedName

// ───── FLIGHT SEARCH ────────────────────────────────────────────────────────

data class FlightSearchRequest(
    val origin: String,
    val destination: String,
    val departureDate: String,
    val returnDate: String? = null,
    val adults: Int = 1,
    val cabinClass: String = "economy",
    val nonStop: Boolean = false
)

// Backend retourne { success, data: { flights: [...], count: X } }
data class FlightSearchData(
    val flights: List<FlightDto> = emptyList(),
    val count: Int = 0
)

data class FlightSearchResponse(
    val success: Boolean? = null,
    val data: FlightSearchData? = null,
    val message: String? = null
)

data class FlightDto(
    val id: String? = null,
    val price: Double,
    val currency: String = "EUR",
    @SerializedName("aiScore") val aiScore: Int? = null,
    @SerializedName("priceCategory") val priceCategory: String? = null,
    val outbound: FlightSegmentDto,
    val inbound: FlightSegmentDto? = null,
    @SerializedName("bookingLink") val bookingLink: String? = null,
    @SerializedName("isFavorite") val isFavorite: Boolean = false,
    @SerializedName("favoriteId") val favoriteId: String? = null
)

data class FlightSegmentDto(
    @SerializedName("departureAirport") val departureAirport: String,
    @SerializedName("arrivalAirport") val arrivalAirport: String,
    @SerializedName("departureTime") val departureTime: String,
    @SerializedName("arrivalTime") val arrivalTime: String,
    @SerializedName("duration") val duration: Int, // en minutes
    @SerializedName("stops") val stops: Int = 0,
    @SerializedName("airline") val airline: String? = null,
    @SerializedName("flightNumber") val flightNumber: String? = null
)

// ───── FAVORITE ─────────────────────────────────────────────────────────────

data class FavoritesResponse(
    val success: Boolean? = null,
    val favorites: List<FavoriteDto> = emptyList()
)

// Les colonnes DB sont en snake_case, il faut les mapper vers camelCase
data class FavoriteDto(
    val id: Int? = null,   // DB integer primary key
    @SerializedName("price_amount") val price: Double = 0.0,
    @SerializedName("price_currency") val currency: String = "EUR",
    @SerializedName("origin_code") val originCode: String = "",
    @SerializedName("destination_code") val destinationCode: String = "",
    @SerializedName("departure_datetime") val departureDate: String = "",
    @SerializedName("return_date") val returnDate: String? = null,
    @SerializedName("airline_name") val airlineName: String? = null,
    @SerializedName("duration_minutes") val durationMinutes: Int? = null,
    val stops: Int = 0,
    @SerializedName("cabin_class") val cabinClass: String = "economy",
    val bookingLink: String? = null,
    val aiScore: Int? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

// Champs attendus par le backend (POST /api/favorites)
data class AddFavoriteRequest(
    val airlineCode: String? = null,
    val airlineName: String? = null,
    val flightNumber: String? = null,
    val originCode: String,
    val originCity: String? = null,
    val destinationCode: String,
    val destinationCity: String? = null,
    val departureDatetime: String,
    val arrivalDatetime: String? = null,
    val durationMinutes: Int? = null,
    val stops: Int = 0,
    val cabinClass: String = "economy",
    val priceAmount: Double,
    val priceCurrency: String = "EUR"
)

// Backend retourne { message, favorite: { ...FavoriteDto } }
data class AddFavoriteResponse(
    val message: String? = null,
    val favorite: FavoriteDto? = null
)

// ───── SEARCH HISTORY ────────────────────────────────────────────────────────

data class SearchHistoryResponse(
    val success: Boolean? = null,
    val history: List<SearchHistoryDto> = emptyList(),
    val total: Int = 0
)

data class SearchHistoryDto(
    val id: String,
    @SerializedName("origin_code") val originCode: String,
    @SerializedName("destination_code") val destinationCode: String,
    @SerializedName("departure_date") val departureDate: String,
    @SerializedName("return_date") val returnDate: String? = null,
    @SerializedName("adults") val passengers: Int = 1,
    @SerializedName("travel_class") val cabinClass: String = "economy",
    @SerializedName("searched_at") val createdAt: String? = null,
    // Champs optionnels présents dans l'historique
    @SerializedName("origin_city") val originCity: String? = null,
    @SerializedName("destination_city") val destinationCity: String? = null
)

// ───── DESTINATIONS TENDANCE ────────────────────────────────────────────────

data class PopularDestinationsResponse(
    val success: Boolean? = null,
    val destinations: List<TrendingDestinationDto> = emptyList()
)

// destination_code et destination_city = noms des colonnes de la vue popular_destinations
data class TrendingDestinationDto(
    @SerializedName("destination_code") val code: String? = null,
    @SerializedName("destination_city") val city: String? = null,
    val country: String? = null,
    @SerializedName("average_price") val averagePrice: Double? = null,
    @SerializedName("search_count") val trendScore: Float? = null,
    val trending: Boolean = false
)

// ───── INSPIRATION ───────────────────────────────────────────────────────────

data class InspirationRequest(
    val origin: String = "",
    val departureDate: String = "",
    val weather: String = "",
    val temperature: String = "",
    val humidity: String = "",
    val wind: String = "",
    val budget: String = "",
    val activities: List<String> = emptyList()
)

data class InspirationResponse(
    val success: Boolean? = null,
    val destinations: List<InspirationDestinationDto> = emptyList()
)

data class WeatherInfo(
    val temperature: Double? = null,
    val feelsLike: Double? = null,
    val humidity: Int? = null,
    val description: String? = null,
    val icon: String? = null,
    val windSpeed: Double? = null,
    val weatherType: String? = null
)

data class InspirationDestinationDto(
    val city: String? = null,
    val country: String? = null,
    val code: String? = null,
    val reason: String? = null,
    val weather: WeatherInfo? = null,
    @SerializedName("minPrice") val minPrice: Double? = null
)

// ───── MODÈLES RAW BACKEND (structure exacte du JSON retourné par l'API) ─────
// Le backend Amadeus retourne une structure différente de FlightDto
// Ces modèles "raw" sont utilisés pour désérialiser, puis transformés en FlightDto

data class RawFlightSearchResponse(
    val success: Boolean? = null,
    val data: RawFlightSearchData? = null,
    val message: String? = null
)

data class RawFlightSearchData(
    val flights: List<RawFlightDto> = emptyList()
)

data class RawFlightDto(
    val id: String? = null,
    val price: RawPriceDto,
    val outbound: RawSegmentDto,
    val inbound: RawSegmentDto? = null,
    @SerializedName("validatingAirlineCodes") val airlineCodes: List<String>? = null,
    val aiScore: Int? = null
)

data class RawPriceDto(
    val total: Double = 0.0,
    val currency: String = "EUR"
)

data class RawEndpointDto(
    val airport: String = "",
    val time: String = ""
)

data class RawSegmentDto(
    val departure: RawEndpointDto = RawEndpointDto(),
    val arrival: RawEndpointDto = RawEndpointDto(),
    val duration: String = "PT0M",   // format ISO 8601 ex: "PT13H5M"
    val stops: Int = 0
)

// Parse "PT13H5M" → 785 minutes
fun parseIsoDuration(s: String): Int {
    var min = 0
    Regex("""(\d+)H""").find(s)?.also { min += it.groupValues[1].toInt() * 60 }
    Regex("""(\d+)M""").find(s)?.also { min += it.groupValues[1].toInt() }
    return min
}

// Convertit RawFlightDto (backend) → FlightDto (modèle UI existant)
fun RawFlightDto.toFlightDto(): FlightDto {
    val airline = airlineCodes?.firstOrNull()
    return FlightDto(
        id = id,
        price = price.total,
        currency = price.currency,
        aiScore = aiScore,
        outbound = FlightSegmentDto(
            departureAirport = outbound.departure.airport,
            arrivalAirport   = outbound.arrival.airport,
            departureTime    = outbound.departure.time,
            arrivalTime      = outbound.arrival.time,
            duration         = parseIsoDuration(outbound.duration),
            stops            = outbound.stops,
            airline          = airline
        ),
        inbound = inbound?.let { seg ->
            FlightSegmentDto(
                departureAirport = seg.departure.airport,
                arrivalAirport   = seg.arrival.airport,
                departureTime    = seg.departure.time,
                arrivalTime      = seg.arrival.time,
                duration         = parseIsoDuration(seg.duration),
                stops            = seg.stops,
                airline          = airline
            )
        }
    )
}
