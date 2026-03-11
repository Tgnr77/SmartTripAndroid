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

// ─── Mapping codes IATA → noms compagnies ────────────────────────────────────
val AIRLINE_NAMES = mapOf(
    "AF" to "Air France", "BA" to "British Airways", "LH" to "Lufthansa",
    "KL" to "KLM", "IB" to "Iberia", "AZ" to "ITA Airways", "LX" to "Swiss",
    "TK" to "Turkish Airlines", "EK" to "Emirates", "QR" to "Qatar Airways",
    "EY" to "Etihad", "SQ" to "Singapore Airlines", "CX" to "Cathay Pacific",
    "NH" to "ANA", "JL" to "Japan Airlines", "KE" to "Korean Air",
    "OZ" to "Asiana", "CA" to "Air China", "MU" to "China Eastern",
    "CZ" to "China Southern", "UA" to "United Airlines", "AA" to "American Airlines",
    "DL" to "Delta", "WN" to "Southwest", "AC" to "Air Canada",
    "QF" to "Qantas", "NZ" to "Air New Zealand", "LA" to "LATAM",
    "G3" to "GOL", "AV" to "Avianca", "CM" to "Copa", "AM" to "Aeroméxico",
    "VY" to "Vueling", "FR" to "Ryanair", "U2" to "easyJet", "W6" to "Wizz Air",
    "LS" to "Jet2", "EW" to "Eurowings", "XK" to "Air Corsica",
    "HV" to "Transavia", "TO" to "Transavia France",
    "TP" to "TAP Portugal", "SK" to "SAS", "AY" to "Finnair",
    "OS" to "Austrian", "SN" to "Brussels Airlines", "LO" to "LOT Polish",
    "OK" to "Czech Airlines", "RO" to "TAROM", "BT" to "airBaltic",
    "FZ" to "flydubai", "WY" to "Oman Air", "GF" to "Gulf Air",
    "SV" to "Saudi Arabian Airlines", "ET" to "Ethiopian Airlines",
    "KQ" to "Kenya Airways", "SA" to "South African Airways",
    "MS" to "EgyptAir", "RJ" to "Royal Jordanian",
    "AI" to "Air India", "6E" to "IndiGo", "IX" to "Air India Express",
    "SL" to "Thai Lion Air", "FD" to "Thai AirAsia", "TG" to "Thai Airways",
    "MH" to "Malaysia Airlines", "GA" to "Garuda Indonesia", "PR" to "Philippine Airlines",
    "VN" to "Vietnam Airlines", "VJ" to "VietJet", "BR" to "EVA Air",
    "CI" to "China Airlines", "OD" to "Batik Air", "QZ" to "AirAsia",
    "D7" to "AirAsia X", "AK" to "AirAsia"
)

// Construit un lien de réservation vers le site de la compagnie avec pré-remplissage
fun buildBookingUrl(
    airlineCode: String?,
    origin: String,
    destination: String,
    departureDate: String,   // YYYY-MM-DD
    returnDate: String? = null,
    adults: Int = 1,
    cabinClass: String = "economy"
): String {
    // Format date compagnie : certaines attendent YYYYMMDD, d'autres YYYY-MM-DD
    val dateCompact = departureDate.replace("-", "")
    val retDateCompact = returnDate?.replace("-", "") ?: ""
    val tripType = if (returnDate != null) "R" else "O"

    return when (airlineCode?.uppercase()) {
        "AF" -> {
            val classCode = when (cabinClass) { "business" -> "C"; "first" -> "F"; else -> "M" }
            val addReturn = if (returnDate != null) "&returnDate=${retDateCompact}" else ""
            "https://wwws.airfrance.fr/search/offers?pax=ADT:$adults&bookingFlow=REWARD&origin=$origin&destination=$destination&outwardDate=$dateCompact${addReturn}&cabin=$classCode&lang=fr"
        }
        "KL" -> {
            val addReturn = if (returnDate != null) "&returnDate=${retDateCompact}" else ""
            "https://www.klm.com/search/offers?pax=ADT:$adults&origin=$origin&destination=$destination&outwardDate=$dateCompact${addReturn}&cabin=$cabinClass"
        }
        "LH" -> {
            val classCode = when (cabinClass) { "business" -> "C"; "first" -> "F"; else -> "Y" }
            "https://www.lufthansa.com/fr/fr/deeplink?dep=$origin&dst=$destination&date=$dateCompact&adults=$adults&cabin=$classCode"
        }
        "BA" -> {
            val addReturn = if (returnDate != null) "&returnDate=${returnDate}" else ""
            "https://www.britishairways.com/travel/redeem/execclub/_gf/en_gb?departing=$origin&arriving=$destination&departDate=${departureDate}${addReturn}&adult=$adults&cabin=$cabinClass"
        }
        "EK" -> "https://book.emirates.com/booking/b2c/public/shoppingcart.faces?journeyType=$tripType&from=$origin&to=$destination&depDate=$dateCompact&numOfAdults=$adults&cabinClass=${cabinClass.uppercase()}"
        "QR" -> "https://booking.qatarairways.com/nsp/views/main.action?selectTrip=$tripType&fromStation=$origin&toStation=$destination&departingMon=$dateCompact&paxType=ADT&adults=$adults"
        "TK" -> "https://www.turkishairlines.com/en-fr/flights/find-flights/?origin=$origin&destination=$destination&departureDate=$dateCompact&adult=$adults&cabin=$cabinClass"
        else -> {
            // URL Google Flights générique avec pré-remplissage universel
            val addReturn = if (returnDate != null) "/$returnDate" else ""
            "https://www.google.com/travel/flights/search?tfs=CBwQAhoeEgoyMDI1LTA1LTIxagcIARIDQ0RHcgcIARIDSE5EGgF-bGF5b3V0LnVpX2ZsaWdodF9zZWFyY2g%3D&q=flights+from+$origin+to+$destination+$departureDate${if (returnDate != null) "+return+$returnDate" else ""}&hl=fr"
        }
    }
}

// Parse "PT13H5M" → 785 minutes
fun parseIsoDuration(s: String): Int {
    var min = 0
    Regex("""(\d+)H""").find(s)?.also { min += it.groupValues[1].toInt() * 60 }
    Regex("""(\d+)M""").find(s)?.also { min += it.groupValues[1].toInt() }
    return min
}

// Convertit RawFlightDto (backend) → FlightDto (modèle UI existant)
fun RawFlightDto.toFlightDto(
    origin: String = "",
    destination: String = "",
    departureDate: String = "",
    returnDate: String? = null,
    adults: Int = 1,
    cabinClass: String = "economy"
): FlightDto {
    val airlineCode = airlineCodes?.firstOrNull()
    val airlineName = airlineCode?.let { AIRLINE_NAMES[it] } ?: airlineCode
    val bookingLink = buildBookingUrl(airlineCode, origin, destination, departureDate, returnDate, adults, cabinClass)
    return FlightDto(
        id = id,
        price = price.total,
        currency = price.currency,
        aiScore = aiScore,
        bookingLink = bookingLink,
        outbound = FlightSegmentDto(
            departureAirport = outbound.departure.airport,
            arrivalAirport   = outbound.arrival.airport,
            departureTime    = outbound.departure.time,
            arrivalTime      = outbound.arrival.time,
            duration         = parseIsoDuration(outbound.duration),
            stops            = outbound.stops,
            airline          = airlineName,
            flightNumber     = airlineCode
        ),
        inbound = inbound?.let { seg ->
            FlightSegmentDto(
                departureAirport = seg.departure.airport,
                arrivalAirport   = seg.arrival.airport,
                departureTime    = seg.departure.time,
                arrivalTime      = seg.arrival.time,
                duration         = parseIsoDuration(seg.duration),
                stops            = seg.stops,
                airline          = airlineName,
                flightNumber     = airlineCode
            )
        }
    )
}
