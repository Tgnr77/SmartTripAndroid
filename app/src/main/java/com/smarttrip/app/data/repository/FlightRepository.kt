package com.smarttrip.app.data.repository

import com.smarttrip.app.data.remote.ApiService
import com.smarttrip.app.data.remote.models.*
import javax.inject.Inject
import javax.inject.Singleton

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
}

@Singleton
class FlightRepository @Inject constructor(
    private val api: ApiService
) {
    // ─── RECHERCHE VOLS ───────────────────────────────────────────────────

    suspend fun searchFlights(
        origin: String,
        destination: String,
        departureDate: String,
        returnDate: String? = null,
        adults: Int = 1,
        cabinClass: String = "economy",
        nonStop: Boolean = false
    ): ApiResult<List<FlightDto>> {
        return try {
            val response = api.searchFlights(
                FlightSearchRequest(origin, destination, departureDate, returnDate, adults, cabinClass, nonStop)
            )
            if (response.isSuccessful) {
                val body = response.body()!!
                // Transformation raw backend JSON → FlightDto (modèle UI)
                val flights = body.data?.flights?.map {
                    it.toFlightDto(origin, destination, departureDate, returnDate, adults, cabinClass)
                } ?: emptyList()
                ApiResult.Success(flights)
            } else {
                val errMsg = try {
                    org.json.JSONObject(response.errorBody()?.string() ?: "")
                        .optString("message", "Erreur lors de la recherche")
                } catch (e: Exception) { "Erreur lors de la recherche" }
                ApiResult.Error(errMsg)
            }
        } catch (e: Exception) {
            ApiResult.Error("Impossible de se connecter au serveur")
        }
    }

    // ─── DESTINATIONS POPULAIRES ──────────────────────────────────────────

    suspend fun getPopularDestinations(): ApiResult<List<TrendingDestinationDto>> {
        return try {
            val response = api.getPopularDestinations()
            if (response.isSuccessful) {
                val body = response.body()!!
                ApiResult.Success(body.destinations)
            } else {
                ApiResult.Error("Erreur lors du chargement")
            }
        } catch (e: Exception) {
            ApiResult.Error("Impossible de se connecter au serveur")
        }
    }

    // ─── INSPIRATION ──────────────────────────────────────────────────────

    suspend fun getInspiration(request: InspirationRequest): ApiResult<List<InspirationDestinationDto>> {
        return try {
            val response = api.getInspiration(request)
            if (response.isSuccessful) {
                val body = response.body()!!
                ApiResult.Success(body.destinations)
            } else {
                ApiResult.Error("Erreur inspiration")
            }
        } catch (e: Exception) {
            ApiResult.Error("Impossible de se connecter au serveur")
        }
    }

    suspend fun getSurpriseTrending(): ApiResult<List<InspirationDestinationDto>> {
        return try {
            val response = api.getSurpriseTrending()
            if (response.isSuccessful) {
                ApiResult.Success(response.body()?.destinations ?: emptyList())
            } else {
                ApiResult.Error("Erreur tendances")
            }
        } catch (e: Exception) {
            ApiResult.Error("Impossible de se connecter au serveur")
        }
    }

    // ─── FAVORIS ──────────────────────────────────────────────────────────

    suspend fun getFavorites(): ApiResult<List<FavoriteDto>> {
        return try {
            val response = api.getFavorites()
            if (response.isSuccessful) {
                ApiResult.Success(response.body()!!.favorites)
            } else {
                val errMsg = try {
                    org.json.JSONObject(response.errorBody()?.string() ?: "")
                        .optString("message", "Erreur ${response.code()}")
                } catch (e: Exception) { "Erreur ${response.code()}" }
                ApiResult.Error(errMsg)
            }
        } catch (e: Exception) {
            ApiResult.Error("Réseau : ${e.localizedMessage ?: "Impossible de se connecter"}")
        }
    }

    suspend fun addFavorite(request: AddFavoriteRequest): ApiResult<String> {
        return try {
            val response = api.addFavorite(request)
            if (response.isSuccessful) {
                ApiResult.Success(response.body()?.favorite?.id ?: "")
            } else {
                val errMsg = try {
                    org.json.JSONObject(response.errorBody()?.string() ?: "")
                        .optString("message", "Erreur ${response.code()}")
                } catch (e: Exception) { "Erreur ${response.code()}" }
                ApiResult.Error(errMsg)
            }
        } catch (e: Exception) {
            ApiResult.Error("Réseau : ${e.localizedMessage ?: "Impossible d'ajouter"}")
        }
    }

    suspend fun deleteFavorite(id: String): ApiResult<Unit> {
        return try {
            val response = api.deleteFavorite(id)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Erreur lors de la suppression")
            }
        } catch (e: Exception) {
            ApiResult.Error("Impossible de se connecter au serveur")
        }
    }

    // ─── HISTORIQUE ───────────────────────────────────────────────────────

    suspend fun getSearchHistory(): ApiResult<List<SearchHistoryDto>> {
        return try {
            val response = api.getSearchHistory()
            if (response.isSuccessful) {
                ApiResult.Success(response.body()!!.history)
            } else {
                ApiResult.Error("Erreur lors du chargement de l'historique")
            }
        } catch (e: Exception) {
            ApiResult.Error("Impossible de se connecter au serveur")
        }
    }

    suspend fun deleteHistoryEntry(id: String): ApiResult<Unit> {
        return try {
            val response = api.deleteHistoryEntry(id)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Erreur lors de la suppression")
            }
        } catch (e: Exception) {
            ApiResult.Error("Impossible de se connecter au serveur")
        }
    }

    suspend fun clearSearchHistory(): ApiResult<Unit> {
        return try {
            val response = api.clearSearchHistory()
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Erreur lors de l'effacement")
            }
        } catch (e: Exception) {
            ApiResult.Error("Impossible de se connecter au serveur")
        }
    }
}
