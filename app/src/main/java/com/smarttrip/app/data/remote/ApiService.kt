package com.smarttrip.app.data.remote

import com.smarttrip.app.data.remote.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ─── AUTH ──────────────────────────────────────────────────────────────

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): Response<AuthResponse>

    @POST("api/auth/resend-verification")
    suspend fun resendVerification(@Body body: Map<String, String>): Response<GenericResponse>

    @POST("api/auth/request-password-reset")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<GenericResponse>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<GenericResponse>

    @GET("api/auth/profile")
    suspend fun getMe(): Response<ProfileResponse>

    @DELETE("api/users/account")
    suspend fun deleteAccount(): Response<GenericResponse>

    // ─── VOLS ──────────────────────────────────────────────────────────────

    // Backend: POST /api/flights/search avec body JSON
    @POST("api/flights/search")
    suspend fun searchFlights(@Body request: FlightSearchRequest): Response<RawFlightSearchResponse>

    @GET("api/search/popular-destinations")
    suspend fun getPopularDestinations(): Response<PopularDestinationsResponse>

    // ─── INSPIRATION ───────────────────────────────────────────────────────

    @POST("api/inspiration")
    suspend fun getInspiration(@Body request: InspirationRequest): Response<InspirationResponse>

    @GET("api/inspiration/surprise-trending")
    suspend fun getSurpriseTrending(): Response<InspirationResponse>

    // ─── FAVORIS ───────────────────────────────────────────────────────────

    @GET("api/favorites")
    suspend fun getFavorites(): Response<FavoritesResponse>

    @POST("api/favorites")
    suspend fun addFavorite(@Body request: AddFavoriteRequest): Response<AddFavoriteResponse>

    @DELETE("api/favorites/{id}")
    suspend fun deleteFavorite(@Path("id") id: String): Response<GenericResponse>

    // ─── HISTORIQUE ────────────────────────────────────────────────────────

    @GET("api/search/history")
    suspend fun getSearchHistory(): Response<SearchHistoryResponse>

    @DELETE("api/search/history/{id}")
    suspend fun deleteHistoryEntry(@Path("id") id: String): Response<GenericResponse>

    @DELETE("api/search/history")
    suspend fun clearSearchHistory(): Response<GenericResponse>
}
